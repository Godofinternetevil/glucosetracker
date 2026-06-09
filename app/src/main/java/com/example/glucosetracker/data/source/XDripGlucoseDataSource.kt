package com.example.glucosetracker.data.source

import com.example.glucosetracker.data.local.entities.DataSourceConfig

class XDripGlucoseDataSource : NightscoutLikeGlucoseDataSource(
    sourceType = DataSourceConfig.SOURCE_XDRIP_BRIDGE,
    baseUrlProvider = { config -> config.xDripBaseUrl.ifBlank { config.baseUrl } },
    tokenProvider = { config -> config.xDripToken.ifBlank { config.apiSecret } }
)