package com.example.glucosetracker.data.source

import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.remote.mapper.toEntity
import com.example.glucosetracker.data.remote.retrofit.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

abstract class NightscoutLikeGlucoseDataSource(
    override val sourceType: String,
    private val baseUrlProvider: (DataSourceConfig) -> String,
    private val tokenProvider: (DataSourceConfig) -> String
) : GlucoseDataSource {

    override suspend fun fetchHistory(config: DataSourceConfig, count: Int): List<GlucoseEntry> {
        return fetchRemoteEntries(config = config, count = count, sinceTimestamp = null)
    }

    override suspend fun fetchIncremental(
        config: DataSourceConfig,
        sinceTimestamp: Long?,
        count: Int
    ): List<GlucoseEntry> {
        return fetchRemoteEntries(config = config, count = count, sinceTimestamp = sinceTimestamp)
    }

    private suspend fun fetchRemoteEntries(
        config: DataSourceConfig,
        count: Int,
        sinceTimestamp: Long?
    ): List<GlucoseEntry> {
        val baseUrl = baseUrlProvider(config).trim()
        val token = tokenProvider(config).trim().ifBlank { null }
        val sinceDateString = sinceTimestamp?.toNightscoutDateString()
        val entries = RetrofitClient
            .nightscoutApi(baseUrl)
            .getGlucoseEntries(
                count = count,
                token = token,
                apiSecret = token,
                sinceDateString = sinceDateString
            )
            .map { it.toEntity(sourceType) }

        return deduplicate(entries)
    }
}

private fun Long.toNightscoutDateString(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return formatter.format(Date(this))
}