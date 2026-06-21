package com.example.glucosetracker.data.remote.retrofit

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.security.MessageDigest
import java.util.Locale

data class NightscoutConnection(
    val baseUrl: String,
    val queryToken: String?,
    val apiSecret: String?
)

object NightscoutUrlParser {
    fun parse(rawUrl: String, rawCredential: String? = null): NightscoutConnection {
        val trimmedUrl = rawUrl.trim()
        require(trimmedUrl.isNotBlank()) { "URL Nightscout не задан" }

        val parsed = trimmedUrl.toHttpUrlOrNull()
            ?: error("Введите корректный URL Nightscout: https://site.example или token link https://site.example?token=...")
        require(parsed.scheme == "http" || parsed.scheme == "https") {
            "URL Nightscout должен начинаться с http:// или https://"
        }

        val tokenFromUrl = parsed.queryParameter("token")?.trim()?.ifBlank { null }
        val credential = rawCredential?.trim()?.ifBlank { null }
        val auth = when {
            tokenFromUrl != null -> NightscoutAuth(queryToken = tokenFromUrl, apiSecret = null)
            credential == null -> NightscoutAuth(queryToken = null, apiSecret = null)
            credential.looksLikeAccessToken() -> NightscoutAuth(queryToken = credential, apiSecret = null)
            else -> NightscoutAuth(queryToken = null, apiSecret = credential.toNightscoutApiSecretHeader())
        }

        val rootBaseUrl = parsed.newBuilder()
            .encodedPath("/")
            .query(null)
            .fragment(null)
            .build()
            .toString()

        return NightscoutConnection(
            baseUrl = rootBaseUrl,
            queryToken = auth.queryToken,
            apiSecret = auth.apiSecret
        )
    }

    private data class NightscoutAuth(val queryToken: String?, val apiSecret: String?)
}

private fun String.looksLikeAccessToken(): Boolean = count { it == '.' } >= 2

private fun String.toNightscoutApiSecretHeader(): String {
    val lower = lowercase(Locale.US)
    if (lower.length == 40 && lower.all { it in '0'..'9' || it in 'a'..'f' }) return lower
    val digest = MessageDigest.getInstance("SHA-1").digest(toByteArray(Charsets.UTF_8))
    return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
}