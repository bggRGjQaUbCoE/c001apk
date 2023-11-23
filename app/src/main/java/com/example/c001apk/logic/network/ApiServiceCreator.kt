package com.example.c001apk.logic.network

import com.example.c001apk.BuildConfig
import com.example.c001apk.util.AddCookiesInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ApiServiceCreator {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AddCookiesInterceptor())
        .addInterceptor(HttpLoggingInterceptor().setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE))
        .build()

    private val clientNoRedirect: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AddCookiesInterceptor())
        .addInterceptor(HttpLoggingInterceptor().setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE))
        .followRedirects(false)
        .build()

    private const val BASE_URL = "https://api.coolapk.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    private val retrofitNoRedirect = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(clientNoRedirect)
        .build()

    fun <T> create(noredirect: Boolean, serviceClass: Class<T>): T =
        if (noredirect) retrofitNoRedirect.create(serviceClass) else retrofit.create(serviceClass)

    inline fun <reified T> create(noredirect: Boolean = false): T =
        create(noredirect, T::class.java)



}