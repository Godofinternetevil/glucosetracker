package com.example.glucosetracker.data.source

import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.local.entities.GlucoseEntry

interface GlucoseDataSource {
    val sourceType: String

    suspend fun fetchEntries(config: DataSourceConfig, count: Int = 24): List<GlucoseEntry>
}