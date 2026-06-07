package com.example.glucosetracker.data.source

import com.example.glucosetracker.data.local.entities.DataSourceConfig

class GlucoseDataSourceFactory(
    private val manualDataSource: GlucoseDataSource = ManualGlucoseDataSource(),
    private val nightscoutDataSource: GlucoseDataSource = NightscoutGlucoseDataSource()
) {
    fun create(config: DataSourceConfig): GlucoseDataSource {
        return when (config.sourceType) {
            DataSourceConfig.SOURCE_NIGHTSCOUT -> nightscoutDataSource
            DataSourceConfig.SOURCE_MANUAL -> manualDataSource
            else -> UnsupportedGlucoseDataSource(config.sourceType)
        }
    }
}