package com.example.glucosetracker.data.repository

import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.source.NightscoutGlucoseDataSource

class NightscoutRepository(
    private val dataSource: NightscoutGlucoseDataSource = NightscoutGlucoseDataSource()
) {
    suspend fun fetchGlucoseData(config: DataSourceConfig) = dataSource.fetchHistory(config)
}