package com.example.glucosetracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.net.URI

@Entity(tableName = "data_source_config")
data class DataSourceConfig(
    @PrimaryKey
    val id: Int = DEFAULT_ID,
    val sourceType: String = SOURCE_MANUAL,
    val baseUrl: String = "",
    val apiSecret: String = "",
    val nightscoutBaseUrl: String = "",
    val nightscoutToken: String = "",
    val xDripBaseUrl: String = "",
    val xDripToken: String = "",
    val otherApiBaseUrl: String = "",
    val otherApiToken: String = "",
    val sourceUnits: String = UNIT_MMOL_L,
    val autoSyncEnabled: Boolean = true,
    val lastSyncAt: Long? = null,
    val connectionMode: String = CONNECTION_MODE_MANUAL
) {
    fun isRemoteSource(): Boolean = sourceType != SOURCE_MANUAL

    fun activeBaseUrl(): String = when (sourceType) {
        SOURCE_NIGHTSCOUT -> nightscoutBaseUrl.ifBlank { baseUrl }
        SOURCE_XDRIP_BRIDGE -> xDripBaseUrl.ifBlank { baseUrl }
        SOURCE_OTHER_API -> otherApiBaseUrl.ifBlank { baseUrl }
        else -> ""
    }

    fun activeToken(): String = when (sourceType) {
        SOURCE_NIGHTSCOUT -> nightscoutToken.ifBlank { apiSecret }
        SOURCE_XDRIP_BRIDGE -> xDripToken.ifBlank { apiSecret }
        SOURCE_OTHER_API -> otherApiToken.ifBlank { apiSecret }
        else -> ""
    }

    fun normalizedForStorage(): DataSourceConfig {
        val activeUrl = activeBaseUrl().trim()
        val activeToken = activeToken().trim()
        return when (sourceType) {
            SOURCE_NIGHTSCOUT -> copy(
                baseUrl = activeUrl,
                apiSecret = activeToken,
                nightscoutBaseUrl = activeUrl,
                nightscoutToken = activeToken,
                connectionMode = CONNECTION_MODE_NIGHTSCOUT
            )

            SOURCE_XDRIP_BRIDGE -> copy(
                baseUrl = activeUrl,
                apiSecret = activeToken,
                xDripBaseUrl = activeUrl,
                xDripToken = activeToken,
                connectionMode = CONNECTION_MODE_XDRIP_BRIDGE
            )

            SOURCE_OTHER_API -> copy(
                baseUrl = activeUrl,
                apiSecret = activeToken,
                otherApiBaseUrl = activeUrl,
                otherApiToken = activeToken,
                connectionMode = CONNECTION_MODE_OTHER_API
            )

            else -> copy(
                baseUrl = "",
                apiSecret = "",
                connectionMode = CONNECTION_MODE_MANUAL
            )
        }.trimmed()
    }

    fun validationErrors(): List<String> {
        if (!isRemoteSource()) return emptyList()

        val errors = mutableListOf<String>()
        val remoteUrl = activeBaseUrl().trim()
        val remoteToken = activeToken().trim()

        if (!remoteUrl.isValidRemoteUrl()) {
            errors += when (sourceType) {
                SOURCE_NIGHTSCOUT -> "Укажите корректный Nightscout URL или token link, например https://nightscout.example.com или https://nightscout.example.com?token=..."
                SOURCE_XDRIP_BRIDGE -> "Укажите корректный URL xDrip bridge, например https://xdrip.example.com"
                SOURCE_OTHER_API -> "Укажите корректный URL другого API, например https://api.example.com"
                else -> "Укажите корректный URL источника данных"
            }
        }

        if (remoteToken.isBlank() && (sourceType != SOURCE_NIGHTSCOUT || !remoteUrl.containsNightscoutToken())) {
            errors += when (sourceType) {
                SOURCE_NIGHTSCOUT -> "Укажите Access token / API secret для Nightscout или вставьте token link вида https://nightscout.example.com?token=..."
                SOURCE_XDRIP_BRIDGE -> "Укажите token для xDrip bridge"
                SOURCE_OTHER_API -> "Укажите token для другого API"
                else -> "Укажите токен доступа"
            }
        }

        return errors
    }

    private fun trimmed(): DataSourceConfig = copy(
        baseUrl = baseUrl.trim(),
        apiSecret = apiSecret.trim(),
        nightscoutBaseUrl = nightscoutBaseUrl.trim(),
        nightscoutToken = nightscoutToken.trim(),
        xDripBaseUrl = xDripBaseUrl.trim(),
        xDripToken = xDripToken.trim(),
        otherApiBaseUrl = otherApiBaseUrl.trim(),
        otherApiToken = otherApiToken.trim()
    )

    companion object {
        const val DEFAULT_ID = 0
        const val SOURCE_MANUAL = "Manual"
        const val SOURCE_NIGHTSCOUT = "Nightscout"
        const val SOURCE_XDRIP_BRIDGE = "xDrip bridge"
        const val SOURCE_OTHER_API = "Other API"
        const val UNIT_MMOL_L = "mmol/L"
        const val UNIT_MG_DL = "mg/dL"

        const val CONNECTION_MODE_MANUAL = "manual"
        const val CONNECTION_MODE_NIGHTSCOUT = "nightscout"
        const val CONNECTION_MODE_XDRIP_BRIDGE = "xdrip_bridge"
        const val CONNECTION_MODE_OTHER_API = "other_api"
    }
}

private fun String.isValidRemoteUrl(): Boolean {
    val parsed = runCatching { URI(trim()) }.getOrNull() ?: return false
    return parsed.scheme in setOf("http", "https") && !parsed.host.isNullOrBlank()
}

private fun String.containsNightscoutToken(): Boolean {
    val parsed = runCatching { URI(trim()) }.getOrNull() ?: return false
    return parsed.rawQuery?.split('&')?.any { part ->
        part.substringBefore('=') == "token" && part.substringAfter('=', "").isNotBlank()
    } == true
}