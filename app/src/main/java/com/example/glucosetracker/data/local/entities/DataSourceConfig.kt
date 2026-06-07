package com.example.glucosetracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_source_config")
data class DataSourceConfig(
    @PrimaryKey
    val id: Int = DEFAULT_ID,
    val sourceType: String = SOURCE_MANUAL,
    val baseUrl: String = "",
    val apiSecret: String = "",
    val sourceUnits: String = UNIT_MMOL_L,
    val autoSyncEnabled: Boolean = true,
    val lastSyncAt: Long? = null
) {
    companion object {
        const val DEFAULT_ID = 0
        const val SOURCE_MANUAL = "Manual"
        const val SOURCE_NIGHTSCOUT = "Nightscout"
        const val SOURCE_OTHER_API = "Other API"
        const val UNIT_MMOL_L = "mmol/L"
        const val UNIT_MG_DL = "mg/dL"
    }
}