package com.example.glucosetracker.data.repository

import com.example.glucosetracker.data.remote.mapper.toEntity
import com.example.glucosetracker.data.remote.retrofit.RetrofitClient

class NightscoutRepository {

    suspend fun fetchGlucoseData() =

        RetrofitClient.api
            .getGlucoseEntries()
            .map { dto ->
                dto.toEntity()
            }
}