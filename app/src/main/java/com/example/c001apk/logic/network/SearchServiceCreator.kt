package com.example.c001apk.logic.network

import com.example.c001apk.util.AddCookiesInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object SearchServiceCreator {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AddCookiesInterceptor())
        .build()

    private const val BASE_URL = "https://api.coolapk.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun <T> create(serviceClass: Class<T>):T = retrofit.create(serviceClass)

    inline fun <reified T> create(): T = create(T::class.java)




}