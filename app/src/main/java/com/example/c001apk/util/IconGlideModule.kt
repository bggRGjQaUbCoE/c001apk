package com.example.c001apk.util

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.engine.executor.GlideExecutor
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.signature.ObjectKey

@GlideModule
class IconGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(LocalAppIcon::class.java, Drawable::class.java, IconModelLoaderFactory(context))
    }


}

data class LocalAppIcon(
    val packageName: String
)

class IconModelLoaderFactory(private val context: Context) : ModelLoaderFactory<LocalAppIcon, Drawable> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<LocalAppIcon, Drawable> {
        return IconModelLoader(context)
    }

    override fun teardown() {

    }

}

class IconModelLoader(private val context: Context) : ModelLoader<LocalAppIcon, Drawable> {
    override fun buildLoadData(
        model: LocalAppIcon, width: Int, height: Int, options: Options
    ): ModelLoader.LoadData<Drawable> {
        return ModelLoader.LoadData(
            ObjectKey(model), IconDataFetcher(context = context, packageName = model.packageName)
        )
    }

    override fun handles(model: LocalAppIcon) = true

}

class IconDataFetcher(private val context: Context, private val packageName: String) :
    DataFetcher<Drawable> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Drawable>) {
        GlideExecutor.newAnimationExecutor().execute {
            val icon = AppUtils.getIcon(context, packageName)
            callback.onDataReady(icon)
        }
    }

    override fun cleanup() {

    }

    override fun cancel() {

    }

    override fun getDataClass(): Class<Drawable> {
        return Drawable::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }

}