package com.example.glucosetracker.data.source

import com.example.glucosetracker.data.local.entities.DataSourceConfig

class GlucoseDataSourceFactory(
    private val manualDataSource: GlucoseDataSource = ManualGlucoseDataSource(),
    private val nightscoutDataSource: GlucoseDataSource = NightscoutGlucoseDataSource(),
    private val xDripDataSource: GlucoseDataSource = XDripGlucoseDataSource(),
    private val otherApiDataSource: GlucoseDataSource = OtherApiGlucoseDataSource()
) {
    fun create(config: DataSourceConfig): GlucoseDataSource {
        return when (config.sourceType) {
            DataSourceConfig.SOURCE_NIGHTSCOUT -> nightscoutDataSource
            DataSourceConfig.SOURCE_XDRIP_BRIDGE -> xDripDataSource
            DataSourceConfig.SOURCE_OTHER_API -> otherApiDataSource
            DataSourceConfig.SOURCE_MANUAL -> manualDataSource
            else -> UnsupportedGlucoseDataSource(config.sourceType)
        }
    }
}