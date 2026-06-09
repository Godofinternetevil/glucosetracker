package com.example.glucosetracker.data.source

import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.local.entities.GlucoseEntry

class UnsupportedGlucoseDataSource(
    override val sourceType: String
) : GlucoseDataSource {
    override suspend fun fetchHistory(config: DataSourceConfig, count: Int): List<GlucoseEntry> {
        error("Источник ${config.sourceType} пока не подключен")
    }

    override suspend fun fetchIncremental(
        config: DataSourceConfig,
        sinceTimestamp: Long?,
        count: Int
    ): List<GlucoseEntry> {
        error("Источник ${config.sourceType} пока не подключен")
    }
}