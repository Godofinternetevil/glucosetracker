package com.example.glucosetracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prediction_entries")
data class PredictionEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createdAt: Long,
    val horizonMinutes: Int,
    val predictedMmolL: Float,
    val predictedMgDl: Float,
    val trendLabel: String,
    val confidenceLabel: String,
    val modelVersion: String? = null
)