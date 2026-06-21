package com.example.glucosetracker

import com.example.glucosetracker.data.remote.retrofit.NightscoutUrlParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NightscoutUrlParserTest {
    @Test
    fun cleanSiteUrlWithSeparateAccessTokenUsesQueryToken() {
        val parsed = NightscoutUrlParser.parse(
            rawUrl = "https://site.example",
            rawCredential = "header.payload.signature"
        )

        assertEquals("https://site.example/", parsed.baseUrl)
        assertEquals("header.payload.signature", parsed.queryToken)
        assertNull(parsed.apiSecret)
    }

    @Test
    fun siteUrlWithTokenExtractsQueryToken() {
        val parsed = NightscoutUrlParser.parse("https://site.example?token=from-link")

        assertEquals("https://site.example/", parsed.baseUrl)
        assertEquals("from-link", parsed.queryToken)
        assertNull(parsed.apiSecret)
    }

    @Test
    fun urlWithApiV1NormalizesToSiteRoot() {
        val parsed = NightscoutUrlParser.parse("https://site.example/api/v1?token=abc")

        assertEquals("https://site.example/", parsed.baseUrl)
        assertEquals("abc", parsed.queryToken)
    }

    @Test
    fun urlWithEntriesJsonNormalizesToSiteRoot() {
        val parsed = NightscoutUrlParser.parse("https://site.example/api/v1/entries.json?token=abc")

        assertEquals("https://site.example/", parsed.baseUrl)
        assertEquals("abc", parsed.queryToken)
    }

    @Test
    fun apiSecretDoesNotBecomeQueryToken() {
        val parsed = NightscoutUrlParser.parse(
            rawUrl = "https://site.example/api/v1/entries.json",
            rawCredential = "my plain api secret"
        )

        assertEquals("https://site.example/", parsed.baseUrl)
        assertNull(parsed.queryToken)
        assertEquals("760ebfaf532f7c6e9238d924afb00670e9419bd7", parsed.apiSecret)
        assertTrue(parsed.apiSecret!!.matches(Regex("[0-9a-f]{40}")))
    }
}