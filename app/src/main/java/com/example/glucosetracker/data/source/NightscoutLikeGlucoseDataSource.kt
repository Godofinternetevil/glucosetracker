package com.example.glucosetracker.data.source

import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.remote.mapper.toEntity
import com.example.glucosetracker.data.remote.retrofit.NightscoutUrlParser
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
        val rawToken = tokenProvider(config).trim().ifBlank { null }
        val connection = if (sourceType == DataSourceConfig.SOURCE_NIGHTSCOUT) {
            NightscoutUrlParser.parse(
                rawUrl = baseUrlProvider(config),
                rawCredential = rawToken
            )
        } else {
            NightscoutUrlParser.parse(rawUrl = baseUrlProvider(config)).copy(
                queryToken = rawToken,
                apiSecret = rawToken
            )
        }
        val sinceDateString = sinceTimestamp?.toNightscoutDateString()
        val entries = RetrofitClient
            .nightscoutApi(connection.baseUrl)
            .getGlucoseEntries(
                count = count,
                token = connection.queryToken,
                apiSecret = connection.apiSecret,
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