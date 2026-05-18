package com.example.glucosetracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "glucose_entries")
data class GlucoseEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val glucoseLevel: Float,

    val timestamp: Long = System.currentTimeMillis()
)