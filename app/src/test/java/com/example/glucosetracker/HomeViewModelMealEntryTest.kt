package com.example.glucosetracker

import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.local.entities.MealEntry
import com.example.glucosetracker.viewmodel.createMealEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelMealEntryTest {
    @Test
    fun createMealEntry_usesProvidedTimestamp() {
        val selectedTimestamp = 1_718_188_500_000L

        val meal = createMealEntry(
            name = "Обед",
            carbsGrams = 55f,
            proteinGrams = 20f,
            fatGrams = 12f,
            calories = 450,
            mealType = MealEntry.TYPE_LUNCH,
            note = "selected time",
            source = DataSourceConfig.SOURCE_MANUAL,
            timestamp = selectedTimestamp
        )

        assertEquals(selectedTimestamp, meal.timestamp)
        assertEquals("Обед", meal.mealName)
        assertEquals(55f, meal.carbsGrams)
        assertEquals(MealEntry.TYPE_LUNCH, meal.mealType)
    }
}