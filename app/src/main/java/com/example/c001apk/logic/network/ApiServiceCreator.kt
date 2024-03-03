package com.example.c001apk.logic.network

import com.example.c001apk.BuildConfig
import com.example.c001apk.util.AddCookiesInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiServiceCreator {
    
    private const val BASE_URL = "https://api.coolapk.com"

    private fun createOkHttpClient(followRedirects: Boolean = true): OkHttpClient =
        OkHttpClient.Builder().apply {
            addInterceptor(AddCookiesInterceptor())
            addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            followRedirects(followRedirects)
        }.build()

    private fun createRetrofit(followRedirects: Boolean): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(createOkHttpClient(followRedirects))
            .build()

    fun <T> create(serviceClass: Class<T>, noRedirect: Boolean = false): T =
        createRetrofit(noRedirect).create(serviceClass)

    inline fun <reified T> create(noRedirect: Boolean = false): T =
        create(T::class.java, noRedirect)
}
