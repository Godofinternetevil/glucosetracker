package com.example.glucosetracker

import com.example.glucosetracker.domain.ml.MlEvent
import com.example.glucosetracker.domain.ml.MlFeatureRow
import com.example.glucosetracker.domain.ml.MlFeatureRowGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.ZoneOffset

class MlFeatureRowGeneratorTest {
    private val minute = 60_000L

    @Test
    fun generate_buildsFiveMinuteRowsWithWindowFeaturesAndTargets() {
        val start = 0L
        val events = listOf(
            MlEvent.Glucose(start, 5.0f, 90f),
            MlEvent.Glucose(start + 15 * minute, 5.5f, 99f),
            MlEvent.Glucose(start + 30 * minute, 6.0f, 108f),
            MlEvent.Glucose(start + 60 * minute, 10.5f, 189f),
            MlEvent.Meal(start + 5 * minute, 20f),
            MlEvent.Meal(start + 25 * minute, 10f),
            MlEvent.Insulin(start + 10 * minute, 2f)
        )

        val rows = MlFeatureRowGenerator(zoneId = ZoneOffset.UTC).generate(
            events = events,
            startTimestamp = start,
            endTimestamp = start + 60 * minute,
            targetLowMgDl = 70f,
            targetHighMgDl = 180f
        )

        assertEquals(13, rows.size)
        val rowAt30 = rows.first { it.timestamp == start + 30 * minute }
        assertEquals(108f, rowAt30.currentGlucoseMgDl)
        assertEquals(9f, rowAt30.glucoseDelta15MinMgDl)
        assertEquals(18f, rowAt30.glucoseDelta30MinMgDl)
        assertEquals(30f, rowAt30.carbsLast30Min)
        assertEquals(2f, rowAt30.insulinLast30Min)
        assertEquals(0, rowAt30.hourOfDay)
        assertEquals(4, rowAt30.dayOfWeek)
        assertEquals(189f, rowAt30.targetGlucose30MinMgDl)
        assertEquals(MlFeatureRow.TargetClass.HIGH, rowAt30.targetClass30Min)
        assertNotNull(rows.first().targetGlucose30MinMgDl)
    }
}