package com.example.c001apk.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.c001apk.R
import com.example.c001apk.constant.Constants.USER_AGENT
import com.example.c001apk.util.ClipboardUtil.copyText
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
import net.mikaelzero.mojito.impl.DefaultPercentProgress
import net.mikaelzero.mojito.impl.SimpleMojitoViewCallback
import java.io.File


object ImageUtil {

    fun showUserCover(view: ImageView, url: String?) {
        val newUrl = GlideUrl(
            url,
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
            url,
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
            url,
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
            url,
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

    suspend fun saveImageToGallery(ctx: Context, imageUrl: String): Boolean =
        withContext(Dispatchers.IO) {
            var success = false
            val imageDir = Environment.DIRECTORY_PICTURES

            val imagesDir = File(
                Environment.getExternalStoragePublicDirectory(imageDir),
                ctx.getString(R.string.app_name)
            )
            imagesDir.mkdirs()
            val filename = "${System.currentTimeMillis()}.jpg"

            try {
                val file = Glide.with(ctx)
                    .asFile()
                    .load(imageUrl)
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

    @OptIn(DelicateCoroutinesApi::class)
    private fun showSaveImgDialog(context: Context, url: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle("保存图片")
            .setMessage("是否保存图片到本地？")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                GlobalScope.launch {
                    if (saveImageToGallery(context, url)) {
                        ToastUtil.toast("保存成功")
                    } else {
                        ToastUtil.toast("保存失败")
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton("复制图片地址") { _, _ ->
                copyText(context, url)
            }
            .show()
    }

    fun startBigImgView(
        nineGridView: NineGridImageView,
        imageView: ImageView,
        urlList: List<String>,
        position: Int
    ) {
        val imgList: MutableList<String> = ArrayList()
        for (img in urlList) {
            if (img.substring(img.length - 6, img.length) == ".s.jpg")
                imgList.add(img.replace(".s.jpg", ""))
            else
                imgList.add(img)
        }
        Mojito.start(imageView.context) {
            urls(imgList)
            position(position)
            progressLoader {
                DefaultPercentProgress()
            }
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
                        showSaveImgDialog(fragmentActivity, imgList[position])
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
        Mojito.start(context) {
            urls(imgList)
            progressLoader {
                DefaultPercentProgress()
            }
            if (imgList.size > 1) {
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
                        showSaveImgDialog(fragmentActivity, imgList[position])
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
        startBigImgViewSimple(context, listOf(url))
    }

}