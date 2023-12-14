package com.example.c001apk.util

import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.Transition
import com.example.c001apk.R
import com.example.c001apk.constant.Constants.USER_AGENT
import com.example.c001apk.util.ClipboardUtil.copyText
import com.example.c001apk.util.FileUtil.Companion.copyFile
import com.example.c001apk.util.FileUtil.Companion.createFileByDeleteOldFile
import com.example.c001apk.view.FileTarget
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.example.c001apk.view.ninegridimageview.indicator.CircleIndexIndicator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.wasabeef.glide.transformations.ColorFilterTransformation
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.ext.mojito
import net.mikaelzero.mojito.impl.DefaultPercentProgress
import net.mikaelzero.mojito.impl.SimpleMojitoViewCallback
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream


object ImageUtil {

    private var filename = ""
    private var type = ""
    private lateinit var imagesDir: File
    private lateinit var imageCheckDir: File

    fun showUserCover(view: ImageView, url: String?) {
        val newUrl = GlideUrl(
            url?.http2https(),
            LazyHeaders.Builder().addHeader("User-Agent", USER_AGENT).build()
        )
        Glide
            .with(view)
            .load(newUrl)
            //.apply(bitmapTransform(BlurTransformation(25, 1)))
            .transform(ColorFilterTransformation(Color.parseColor("#8A000000")))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(withCrossFade(100))
            .skipMemoryCache(false)
            .dontAnimate()
            .into(view)
    }

    fun showIMG(view: ImageView, url: String?) {
        val newUrl = GlideUrl(
            url?.http2https(),
            LazyHeaders.Builder().addHeader("User-Agent", USER_AGENT).build()
        )
        Glide
            .with(view)
            .load(newUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(withCrossFade(100))
            .skipMemoryCache(false)
            .dontAnimate()
            .into(view)
    }

    fun showAvatar(view: ImageView, url: String?) {
        val newUrl = GlideUrl(
            url?.http2https(),
            LazyHeaders.Builder().addHeader("User-Agent", USER_AGENT).build()
        )
        Glide
            .with(view)
            .load(newUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(withCrossFade(100))
            .skipMemoryCache(false)
            .dontAnimate()
            .circleCrop()
            .into(view)
    }

    fun showIMG1(view: ImageView, url: String?) {
        val newUrl = GlideUrl(
            url?.http2https(),
            LazyHeaders.Builder().addHeader("User-Agent", USER_AGENT).build()
        )
        Glide
            .with(view)
            .load(newUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(withCrossFade(100))
            .skipMemoryCache(false)
            .dontAnimate()
            .centerCrop()
            .into(view)
    }

    private suspend fun saveImageToGallery(ctx: Context, imageUrl: String): Boolean =
        withContext(Dispatchers.IO) {
            var success = false

            if (!imagesDir.exists())
                imagesDir.mkdirs()

            try {
                val file = Glide.with(ctx)
                    .asFile()
                    .load(imageUrl.http2https())
                    .submit()
                    .get()
                val image = File(imagesDir, filename)
                file.copyTo(image, overwrite = true)

                // 将图片插入图库
//                MediaStore.Images.Media.insertImage(
//                    ctx.contentResolver,
//                    image.absolutePath,
//                    image.name,
//                    image.name
//                )

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.TITLE, image.name)
                    put(MediaStore.Images.Media.DESCRIPTION, image.name)
                    // 在 Android 10 及更高版本，需要使用 MediaStore.Images.Media.RELATIVE_PATH
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures")
                    }
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") // 根据实际情况设置 MIME 类型
                }
                // 将图像插入 MediaStore，并获取插入后的 URI
                ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                success = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            success
        }

    private fun saveImage(context: Context, url: String, isEnd: Boolean) {
        val index = url.lastIndexOf('/')
        filename = url.substring(index + 1, url.length)
        imagesDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            context.getString(R.string.app_name)
        )
        imageCheckDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "${context.getString(R.string.app_name)}/$filename"
        )
        if (imageCheckDir.exists()) {
            if (isEnd)
                ToastUtil.toast("文件已存在")
        } else {
            downloadPicture(context, url.http2https(), filename, isEnd)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun showSaveImgDialog(context: Context, url: String, urlList: List<String>?) {
        MaterialAlertDialogBuilder(context).apply {
            val items = arrayOf("保存图片", "保存全部图片", "图片分享", "复制图片地址")
            setItems(items) { _: DialogInterface?, position: Int ->
                when (position) {
                    0 -> {
                        GlobalScope.launch {
                            saveImage(context, url, true)
                        }
                    }

                    1 -> {
                        GlobalScope.launch {
                            if (urlList.isNullOrEmpty()) {
                                saveImage(context, url, true)
                            } else {
                                var isEnd = false
                                for (mUrl in urlList) {
                                    if (urlList.indexOf(mUrl) == urlList.size - 1)
                                        isEnd = true
                                    saveImage(context, mUrl, isEnd)
                                }
                            }
                        }
                    }

                    2 -> {
                        GlobalScope.launch {
                            imagesDir = File(context.externalCacheDir, "imageShare")
                            imageCheckDir = File(context.externalCacheDir, "imageShare/$filename")
                            if (imageCheckDir.exists()) {
                                withContext(Dispatchers.Main) {
                                    shareImage(
                                        context, File(
                                            context.externalCacheDir,
                                            "imageShare/$filename",
                                        ), null
                                    )
                                }
                            } else if (saveImageToGallery(context, url)) {
                                withContext(Dispatchers.Main) {
                                    shareImage(
                                        context, File(
                                            context.externalCacheDir,
                                            "imageShare/$filename",
                                        ), null
                                    )
                                }
                            } else {
                                ToastUtil.toast("分享失败")
                            }
                        }
                    }

                    3 -> {
                        copyText(context, url)
                    }
                }
            }
            show()
        }
    }

    fun startBigImgView(
        nineGridView: NineGridImageView,
        imageView: ImageView,
        urlList: List<String>,
        position: Int
    ) {
        val imgList: MutableList<String> = ArrayList()
        for (img in urlList) {
            if (img.endsWith(".s.jpg"))
                imgList.add(img.replace(".s.jpg", "").http2https())
            else if (img.endsWith(".s2x.jpg"))
                imgList.add(img.replace(".s2x.jpg", "").http2https())
            else
                imgList.add(img.http2https())
        }
        Mojito.start(imageView.context) {
            urls(imgList)
            position(position)
            progressLoader {
                DefaultPercentProgress()
            }
            if (imgList.size != 1)
                setIndicator(CircleIndexIndicator())
            views(nineGridView.getImageViews().toTypedArray())
            setOnMojitoListener(object : SimpleMojitoViewCallback() {
                override fun onStartAnim(position: Int) {
                    nineGridView.getImageViewAt(position)?.apply {
                        postDelayed({
                            this.visibility = View.GONE
                        }, 200)
                    }
                }

                override fun onMojitoViewFinish(pagePosition: Int) {
                    nineGridView.getImageViews().forEach {
                        it.visibility = View.VISIBLE
                    }
                }

                override fun onViewPageSelected(position: Int) {
                    nineGridView.getImageViews().forEachIndexed { index, imageView ->
                        if (position == index) {
                            imageView.visibility = View.GONE
                        } else {
                            imageView.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onLongClick(
                    fragmentActivity: FragmentActivity?,
                    view: View,
                    x: Float,
                    y: Float,
                    position: Int
                ) {
                    if (fragmentActivity != null) {
                        showSaveImgDialog(fragmentActivity, imgList[position], imgList)
                    } else {
                        Log.i("Mojito", "fragmentActivity is null, skip save image")
                    }
                }
            })
        }

    }

    fun startBigImgViewSimple(
        context: Context,
        imgList: List<String>
    ) {
        val newList = ArrayList<String>()
        for (url in imgList) {
            newList.add(url.http2https())
        }
        Mojito.start(context) {
            urls(newList)
            progressLoader {
                DefaultPercentProgress()
            }
            if (newList.size > 1) {
                setIndicator(CircleIndexIndicator())
            }
            setOnMojitoListener(object : SimpleMojitoViewCallback() {
                override fun onLongClick(
                    fragmentActivity: FragmentActivity?,
                    view: View,
                    x: Float,
                    y: Float,
                    position: Int
                ) {
                    if (fragmentActivity != null) {
                        showSaveImgDialog(fragmentActivity, newList[position], newList)
                    } else {
                        Log.i("Mojito", "fragmentActivity is null, skip save image")
                    }
                }
            })
        }
    }

    fun startBigImgViewSimple(
        imageView: ImageView,
        url: String
    ) {
        imageView.mojito(url.http2https()) {
            progressLoader {
                DefaultPercentProgress()
            }
            setOnMojitoListener(object : SimpleMojitoViewCallback() {
                override fun onLongClick(
                    fragmentActivity: FragmentActivity?,
                    view: View,
                    x: Float,
                    y: Float,
                    position: Int
                ) {
                    if (fragmentActivity != null) {
                        showSaveImgDialog(fragmentActivity, url.http2https(), null)
                    } else {
                        Log.i("Mojito", "fragmentActivity is null, skip save image")
                    }
                }
            })
        }
    }

    fun startBigImgViewSimple(
        context: Context,
        url: String
    ) {
        startBigImgViewSimple(context, listOf(url.http2https()))
    }

    /*fun showPaletteColor(imageView: ImageView, url: String?) {
        val newUrl = GlideUrl(
            url,
            LazyHeaders.Builder().addHeader("User-Agent", USER_AGENT).build()
        )
        Glide.with(context)
            .asBitmap()
            .load(newUrl)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    Palette.from(resource).generate { palette ->
                        if (palette != null) {
                            val vibrantSwatch = palette.vibrantSwatch
                            val darkVibrantSwatch = palette.darkVibrantSwatch
                            val lightVibrantSwatch = palette.lightVibrantSwatch
                            val mutedSwatch = palette.mutedSwatch
                            val darkMutedSwatch = palette.darkMutedSwatch
                            val lightMutedSwatch = palette.lightMutedSwatch

                            if (darkVibrantSwatch != null) {
                                imageView.setBackgroundColor(darkVibrantSwatch.rgb)
                            }
                        }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }*/


    // 将File 转化为 content://URI
    private fun getFileProvider(context: Context, file: File?): Uri {
        val authority = context.packageName + ".fileprovider"
        return FileProvider.getUriForFile(
            context, authority,
            file!!
        )
    }

    private fun shareImage(context: Context, file: File?, title: String?) {
        val contentUri = getFileProvider(context, file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, contentUri)
        context.startActivity(Intent.createChooser(intent, title))
    }

    private fun shareVideo(context: Context, file: File?, title: String?) {
        val contentUri = getFileProvider(context, file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "video/*"
        intent.putExtra(Intent.EXTRA_STREAM, contentUri)
        context.startActivity(Intent.createChooser(intent, title))
    }

    private fun shareFile(context: Context, file: File?, title: String?) {
        val contentUri = getFileProvider(context, file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_STREAM, contentUri)
        context.startActivity(Intent.createChooser(intent, title))
    }


    private fun downloadPicture(context: Context, url: String?, fileName: String, isEnd: Boolean) {
        val newUrl = GlideUrl(
            url?.http2https(),
            LazyHeaders.Builder().addHeader("User-Agent", USER_AGENT).build()
        )
        Glide.with(context).downloadOnly().load(newUrl).into(object : FileTarget() {
            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                Toast.makeText(context, "download failed", Toast.LENGTH_SHORT).show()
            }

            override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                super.onResourceReady(resource, transition)
                save(context, resource, fileName, isEnd)
            }
        })
    }

    //from BigImageViewPager
    private fun save(context: Context, resource: File, fileName: String, isEnd: Boolean) {
        val mimeType = getImageTypeWithMime(resource.absolutePath)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 大于等于29版本的保存方法
            val resolver = context.contentResolver
            // 设置文件参数到ContentValues中
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            values.put(MediaStore.Images.Media.DESCRIPTION, fileName)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/$mimeType")
            values.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/c001apk/"
            )
            val insertUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            var inputStream: BufferedInputStream? = null
            var os: OutputStream? = null
            try {
                inputStream = BufferedInputStream(FileInputStream(resource.absolutePath))
                os = insertUri?.let { resolver.openOutputStream(it) }
                os?.let {
                    val buffer = ByteArray(1024 * 4)
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } != -1) {
                        os.write(buffer, 0, len)
                    }
                    os.flush()
                }
                if (isEnd)
                    Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                if (isEnd)
                    Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
            } finally {
                try {
                    os?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            // 低于29版本的保存方法
            val path = Environment.getExternalStorageDirectory()
                .toString() + "/c001apk/"
            createFileByDeleteOldFile(path + filename)
            val result = copyFile(resource, path, filename)
            if (result) {
                if (isEnd)
                    Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show()
            } else {
                if (isEnd)
                    Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getImageTypeWithMime(path: String): String {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        var type = options.outMimeType
        Log.d("getImageTypeWithMime", "getImageTypeWithMime: path = $path, type1 = $type")
        // ”image/png”、”image/jpeg”、”image/gif”
        type = if (TextUtils.isEmpty(type)) {
            ""
        } else {
            type.substring(6)
        }
        Log.d("getImageTypeWithMime", "getImageTypeWithMime: path = $path, type2 = $type")
        return type
    }

}