package com.example.glucosetracker.data.source

import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.local.entities.GlucoseEntry

class ManualGlucoseDataSource : GlucoseDataSource {
    override val sourceType: String = DataSourceConfig.SOURCE_MANUAL

    override suspend fun fetchEntries(config: DataSourceConfig, count: Int): List<GlucoseEntry> = emptyList()
}