package com.example.glucosetracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_entries")
data class MealEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val mealName: String,

    val carbs: Int,

    val timestamp: Long = System.currentTimeMillis()
)