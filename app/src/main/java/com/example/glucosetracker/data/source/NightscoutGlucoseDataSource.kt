package com.example.glucosetracker.data.source

import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.remote.mapper.toEntity
import com.example.glucosetracker.data.remote.retrofit.RetrofitClient

class NightscoutGlucoseDataSource : GlucoseDataSource {
    override val sourceType: String = DataSourceConfig.SOURCE_NIGHTSCOUT

    override suspend fun fetchEntries(config: DataSourceConfig, count: Int): List<GlucoseEntry> {
        val secret = config.apiSecret.trim().ifBlank { null }
        return RetrofitClient
            .nightscoutApi(config.baseUrl)
            .getGlucoseEntries(count = count, token = secret, apiSecret = secret)
            .map { dto -> dto.toEntity() }
    }
}