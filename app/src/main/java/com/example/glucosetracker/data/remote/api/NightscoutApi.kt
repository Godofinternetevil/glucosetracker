package com.example.glucosetracker.data.remote.api

import com.example.glucosetracker.data.remote.dto.NightscoutEntryDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NightscoutApi {

    @GET("api/v1/entries.json")
    suspend fun getGlucoseEntries(
        @Query("count") count: Int = 24,
        @Query("token") token: String? = null,
        @Header("api-secret") apiSecret: String? = null,
        @Query("find[dateString][\$gte]") sinceDateString: String? = null
    ): List<NightscoutEntryDto>
}