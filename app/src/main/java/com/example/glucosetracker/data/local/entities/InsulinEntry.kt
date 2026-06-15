package com.example.glucosetracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "insulin_entries")
data class InsulinEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val timestamp: Long = System.currentTimeMillis(),

    val insulinType: String,

    val units: Float,

    val note: String = "",

    val source: String = DataSourceConfig.SOURCE_MANUAL
) {
    companion object {
        const val TYPE_RAPID = "rapid"
        const val TYPE_SHORT = "short"
        const val TYPE_LONG = "long"
        const val TYPE_BASAL = "basal"
        const val TYPE_CORRECTION = "correction"

        val SUPPORTED_TYPES = listOf(
            TYPE_RAPID,
            TYPE_SHORT,
            TYPE_LONG,
            TYPE_BASAL,
            TYPE_CORRECTION
        )
    }
}