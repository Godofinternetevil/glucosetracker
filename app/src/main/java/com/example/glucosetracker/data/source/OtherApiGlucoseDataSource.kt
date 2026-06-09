package com.example.glucosetracker.data.source

import com.example.glucosetracker.data.local.entities.DataSourceConfig

class OtherApiGlucoseDataSource : NightscoutLikeGlucoseDataSource(
    sourceType = DataSourceConfig.SOURCE_OTHER_API,
    baseUrlProvider = { config -> config.otherApiBaseUrl.ifBlank { config.baseUrl } },
    tokenProvider = { config -> config.otherApiToken.ifBlank { config.apiSecret } }
)