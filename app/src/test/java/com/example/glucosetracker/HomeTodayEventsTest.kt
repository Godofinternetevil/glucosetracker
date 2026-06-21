package com.example.glucosetracker

import com.example.glucosetracker.data.local.entities.MealEntry
import com.example.glucosetracker.ui.components.TodayEventType
import com.example.glucosetracker.ui.screens.todayEvents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class HomeTodayEventsTest {
    @Test
    fun addedMealWithCurrentTimestampAppearsInTodayEvents() {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val yesterday = now - 24 * 60 * 60 * 1000L
        val mealsList = mutableListOf<MealEntry>()

        mealsList += MealEntry(
            mealName = "Овсянка",
            carbsGrams = 42f,
            proteinGrams = 8f,
            fatGrams = 5f,
            calories = 310,
            mealType = MealEntry.TYPE_BREAKFAST,
            timestamp = now
        )
        mealsList += MealEntry(
            mealName = "Старый ужин",
            carbsGrams = 30f,
            timestamp = yesterday
        )

        val events = todayEvents(
            mealsList = mealsList,
            insulinList = emptyList(),
            nowMillis = now
        )

        assertEquals(1, events.size)
        val event = events.single()
        assertEquals(TodayEventType.Meal, event.type)
        assertEquals("Овсянка", event.title)
        assertEquals(now, event.timestamp)
        assertTrue(event.subtitle.contains("42 г углеводов"))
        assertTrue(event.subtitle.contains("8 г белков"))
        assertTrue(event.subtitle.contains("5 г жиров"))
    }
}