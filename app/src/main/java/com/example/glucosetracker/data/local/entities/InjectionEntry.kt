package com.example.glucosetracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "injection_entries")
data class InjectionEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val timestamp: Long = System.currentTimeMillis(),

    val insulinUnits: Float,

    val insulinType: String,

    val injectionType: String,

    val notes: String = ""
)