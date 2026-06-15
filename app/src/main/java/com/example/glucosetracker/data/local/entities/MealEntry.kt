package com.example.glucosetracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_entries")
data class MealEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val mealName: String,

    val carbsGrams: Float,

    val proteinGrams: Float? = null,

    val fatGrams: Float? = null,

    val calories: Int? = null,

    val mealType: String = TYPE_SNACK,

    val note: String = "",

    val source: String = DataSourceConfig.SOURCE_MANUAL,

    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_BREAKFAST = "breakfast"
        const val TYPE_LUNCH = "lunch"
        const val TYPE_DINNER = "dinner"
        const val TYPE_SNACK = "snack"

        val SUPPORTED_TYPES = listOf(TYPE_BREAKFAST, TYPE_LUNCH, TYPE_DINNER, TYPE_SNACK)
    }
}