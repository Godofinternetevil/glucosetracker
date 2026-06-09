package com.example.glucosetracker.data.source

import com.example.glucosetracker.data.local.entities.DataSourceConfig

class NightscoutGlucoseDataSource : NightscoutLikeGlucoseDataSource(
    sourceType = DataSourceConfig.SOURCE_NIGHTSCOUT,
    baseUrlProvider = { config -> config.nightscoutBaseUrl.ifBlank { config.baseUrl } },
    tokenProvider = { config -> config.nightscoutToken.ifBlank { config.apiSecret } }
)
