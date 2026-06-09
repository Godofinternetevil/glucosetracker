package com.example.glucosetracker.data.remote.retrofit

import com.example.glucosetracker.data.remote.api.NightscoutApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URI
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
            .baseUrl(baseUrl.normalizedBaseUrl())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NightscoutApi::class.java)
    }

    private fun String.normalizedBaseUrl(): String {
        val trimmed = trim()
        require(trimmed.isNotBlank()) { "URL источника данных не задан" }

        val parsed = runCatching { URI(trimmed) }.getOrNull()
            ?: error("Введите корректный URL источника данных")
        require(parsed.scheme == "http" || parsed.scheme == "https") {
            "URL источника данных должен начинаться с http:// или https://"
        }
        require(!parsed.host.isNullOrBlank()) {
            "URL источника данных должен содержать домен"
        }

        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }
}