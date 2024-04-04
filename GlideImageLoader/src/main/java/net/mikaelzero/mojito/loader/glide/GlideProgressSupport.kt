package net.mikaelzero.mojito.loader.glide

import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import okhttp3.*
import okio.*
import java.io.IOException
import java.io.InputStream
import java.util.*

object GlideProgressSupport {
    fun init(glide: Glide, okHttpClient: OkHttpClient?) {
        val builder = okHttpClient?.newBuilder() ?: OkHttpClient.Builder()
        builder.addNetworkInterceptor(createInterceptor(DispatchingProgressListener()))
        glide.registry.replace(
            GlideUrl::class.java, InputStream::class.java,
            OkHttpUrlLoader.Factory(builder.build())
        )
    }

    private fun createInterceptor(listener: ResponseProgressListener): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            response.newBuilder()
                .body(response.body?.let {
                    OkHttpProgressResponseBody(request.url, it, listener)
                })
                .build()
        }
    }

    @JvmStatic
    fun forget(url: String) {
        DispatchingProgressListener.forget(url)
    }

    @JvmStatic
    fun expect(url: String, listener: ProgressListener?) {
        DispatchingProgressListener.expect(url, listener)
    }

    interface ProgressListener {
        fun onDownloadStart()
        fun onProgress(progress: Int)
        fun onDownloadFinish()
    }

    private interface ResponseProgressListener {
        fun update(url: HttpUrl, bytesRead: Long, contentLength: Long)
    }

    private class DispatchingProgressListener : ResponseProgressListener {
        companion object {
            private val LISTENERS: MutableMap<String, ProgressListener?> = HashMap()
            private val PROGRESSES: MutableMap<String, Int> = HashMap()

            fun forget(url: String) {
                val key = getRawKey(url)
                LISTENERS.remove(key)
                PROGRESSES.remove(key)
            }

            fun expect(url: String, listener: ProgressListener?) {
                LISTENERS[getRawKey(url)] = listener
            }

            private fun getRawKey(formerKey: String): String {
                return formerKey.split("?")[0]
            }
        }

        override fun update(url: HttpUrl, bytesRead: Long, contentLength: Long) {
            val key = getRawKey(url.toString())
            val listener = LISTENERS[key] ?: return

            val lastProgress = PROGRESSES[key]
            if (lastProgress == null) {
                listener.onDownloadStart()
            }

            if (contentLength <= bytesRead) {
                listener.onDownloadFinish()
                forget(key)
                return
            }

            val progress = (bytesRead.toFloat() / contentLength * 100).toInt()
            if (lastProgress == null || progress != lastProgress) {
                PROGRESSES[key] = progress
                listener.onProgress(progress)
            }
        }
    }

    private class OkHttpProgressResponseBody internal constructor(
        private val mUrl: HttpUrl, private val mResponseBody: ResponseBody?,
        private val mProgressListener: ResponseProgressListener
    ) : ResponseBody() {
        private val mBufferedSource: BufferedSource by lazy {
            mResponseBody?.source()?.let { source(it).buffer() } ?: throw IllegalStateException("ResponseBody can't be null")
        }

        override fun contentType(): MediaType? = mResponseBody?.contentType()

        override fun contentLength(): Long = mResponseBody?.contentLength() ?: -1

        override fun source(): BufferedSource = mBufferedSource

        private fun source(source: Source): Source {
            return object : ForwardingSource(source) {
                private var totalBytesRead = 0L

                @Throws(IOException::class)
                override fun read(sink: Buffer, byteCount: Long): Long {
                    val bytesRead = super.read(sink, byteCount)
                    totalBytesRead += if (bytesRead != -1L) bytesRead else 0

                    mProgressListener.update(mUrl, totalBytesRead, contentLength())
                    return bytesRead
                }
            }
        }
    }
}
