package com.example.c001apk.util

import android.graphics.Color
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import jp.wasabeef.glide.transformations.ColorFilterTransformation


object ImageShowUtil {

    fun showUserCover(view: ImageView, url: String?) {

        Glide
            .with(view)
            .load(url)
            //.apply(bitmapTransform(BlurTransformation(25, 1)))
            .transform(ColorFilterTransformation(Color.parseColor("#8A000000")))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(withCrossFade(100))
            .skipMemoryCache(false)
            .dontAnimate()
            .into(view)
    }

    fun showIMG(view: ImageView, url: String?) {
        Glide
            .with(view)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(withCrossFade(100))
            .skipMemoryCache(false)
            .dontAnimate()
            .into(view)
    }

    fun showAvatar(view: ImageView, url: String?) {
        Glide
            .with(view)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(withCrossFade(100))
            .skipMemoryCache(false)
            .dontAnimate()
            .circleCrop()
            .into(view)
    }

    fun showIMG1(view: ImageView, url: String?) {
        Glide
            .with(view)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(withCrossFade(100))
            .skipMemoryCache(false)
            .dontAnimate()
            .centerCrop()
            .into(view)
    }


}