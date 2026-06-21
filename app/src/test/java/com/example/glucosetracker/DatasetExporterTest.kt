package com.example.glucosetracker

import com.example.glucosetracker.data.export.DatasetExporter
import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.local.entities.MealEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DatasetExporterTest {
    @Test
    fun exportCsv_mealRowUsesSelectedTimestamp() {
        val selectedTimestamp = 1_718_188_500_000L
        val meal = MealEntry(
            mealName = "Обед",
            carbsGrams = 45f,
            mealType = MealEntry.TYPE_LUNCH,
            note = "manual time",
            source = DataSourceConfig.SOURCE_MANUAL,
            timestamp = selectedTimestamp
        )

        val csv = DatasetExporter().exportCsv(
            glucose = emptyList(),
            meals = listOf(meal),
            insulin = emptyList()
        )

        val lines = csv.lines()
        assertEquals(2, lines.size)
        assertTrue(lines[1].startsWith("2024-06-12T10:35:00Z,$selectedTimestamp,meal,"))
    }

    @Test
    fun exportJsonl_mealRowUsesSelectedTimestamp() {
        val selectedTimestamp = 1_718_188_500_000L
        val meal = MealEntry(
            mealName = "Перекус",
            carbsGrams = 15f,
            source = DataSourceConfig.SOURCE_MANUAL,
            timestamp = selectedTimestamp
        )

        val jsonl = DatasetExporter().exportJsonl(
            glucose = emptyList(),
            meals = listOf(meal),
            insulin = emptyList()
        )

        assertTrue(jsonl.contains("\"timestamp_iso\":\"2024-06-12T10:35:00Z\""))
        assertTrue(jsonl.contains("\"timestamp_epoch_ms\":$selectedTimestamp"))
        assertTrue(jsonl.contains("\"event_type\":\"meal\""))
    }
}