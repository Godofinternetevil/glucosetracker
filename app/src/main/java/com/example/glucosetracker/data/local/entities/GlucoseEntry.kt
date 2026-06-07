package com.example.glucosetracker.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "glucose_entries",
    indices = [Index(value = ["source", "sourceId"], unique = true)]
)
data class GlucoseEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val glucoseMmolL: Float,

    val glucoseMgDl: Float,

    val source: String = DataSourceConfig.SOURCE_MANUAL,

    val sourceId: String? = null,

    val trendDirection: String? = null,

    val rawPayload: String? = null,

    val timestamp: Long = System.currentTimeMillis()
)