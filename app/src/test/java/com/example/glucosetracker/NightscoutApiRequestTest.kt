package com.example.glucosetracker

import com.example.glucosetracker.data.remote.retrofit.RetrofitClient
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NightscoutApiRequestTest {
    @Test
    fun getGlucoseEntriesBuildsExpectedNightscoutRequest() = runTest {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))
        server.start()

        try {
            RetrofitClient.nightscoutApi(server.url("/api/v1/entries.json?token=link-token").toString())
                .getGlucoseEntries(
                    count = 12,
                    token = "link-token",
                    sinceDateString = "2026-06-20"
                )

            val request = server.takeRequest()
            assertEquals("/api/v1/entries.json", request.requestUrl!!.encodedPath)
            assertEquals("12", request.requestUrl!!.queryParameter("count"))
            assertEquals("link-token", request.requestUrl!!.queryParameter("token"))
            assertEquals("2026-06-20", request.requestUrl!!.queryParameter("find[dateString][\$gte]"))
            assertTrue(request.requestUrl!!.queryParameterNames.contains("find[dateString][\$gte]"))
        } finally {
            server.shutdown()
        }
    }
}