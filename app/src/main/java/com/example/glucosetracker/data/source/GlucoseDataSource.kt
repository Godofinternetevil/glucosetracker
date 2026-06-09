package com.example.glucosetracker.data.source

import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.local.entities.GlucoseEntry

interface GlucoseDataSource {
    val sourceType: String

    suspend fun fetchHistory(config: DataSourceConfig, count: Int = DEFAULT_HISTORY_LIMIT): List<GlucoseEntry>

    suspend fun fetchIncremental(
        config: DataSourceConfig,
        sinceTimestamp: Long? = null,
        count: Int = DEFAULT_INCREMENTAL_LIMIT
    ): List<GlucoseEntry>

    suspend fun fetchEntries(config: DataSourceConfig, count: Int = DEFAULT_HISTORY_LIMIT): List<GlucoseEntry> {
        return fetchHistory(config, count)
    }

    fun deduplicate(entries: List<GlucoseEntry>): List<GlucoseEntry> {
        return entries
            .distinctBy { it.source to (it.sourceId ?: it.timestamp.toString()) }
            .sortedBy { it.timestamp }
    }

    companion object {
        const val DEFAULT_HISTORY_LIMIT = 500
        const val DEFAULT_INCREMENTAL_LIMIT = 1000
    }
}