package com.example.glucosetracker.data.remote.retrofit

import com.example.glucosetracker.data.remote.api.NightscoutApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun nightscoutApi(baseUrl: String): NightscoutApi {
        return Retrofit.Builder()
            .baseUrl(NightscoutUrlParser.parse(baseUrl).baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NightscoutApi::class.java)
    }

}