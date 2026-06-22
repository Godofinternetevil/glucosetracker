package com.example.glucosetracker

import com.example.glucosetracker.domain.ml.MlFeatureRow
import com.example.glucosetracker.domain.ml.TrainedGlucosePredictor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TrainedGlucosePredictorTest {
    @Test
    fun train_requiresRowsWithCurrentGlucoseAndTarget120() {
        val predictor = TrainedGlucosePredictor(minTrainingRows = 2)

        val model = predictor.train(
            listOf(
                featureRow(current = 100f, target120 = 130f),
                featureRow(current = null, target120 = 140f),
                featureRow(current = 110f, target120 = null)
            )
        )

        assertNull(model)
    }

    @Test
    fun predict_usesNearestPersonalHistoryRowsFor120MinuteTarget() {
        val predictor = TrainedGlucosePredictor(minTrainingRows = 2, neighborCount = 1)
        val model = predictor.train(
            listOf(
                featureRow(current = 100f, target120 = 115f),
                featureRow(current = 220f, target120 = 260f)
            )
        )

        val prediction = model?.predict(featureRow(current = 102f, target120 = null))

        assertNotNull(prediction)
        assertEquals(120, prediction?.horizonMinutes)
        assertEquals(115f, prediction?.predictedMgDl ?: 0f, 0.001f)
        assertEquals(2, prediction?.trainingRowCount)
    }

    private fun featureRow(current: Float?, target120: Float?): MlFeatureRow = MlFeatureRow(
        timestamp = 0L,
        currentGlucoseMmolL = current?.div(18.0182f),
        currentGlucoseMgDl = current,
        glucoseDelta15MinMgDl = 0f,
        glucoseDelta30MinMgDl = 0f,
        glucoseDelta60MinMgDl = 0f,
        carbsLast30Min = 0f,
        carbsLast60Min = 0f,
        carbsLast120Min = 0f,
        insulinLast30Min = 0f,
        insulinLast60Min = 0f,
        insulinLast180Min = 0f,
        hourOfDay = 12,
        dayOfWeek = 1,
        targetLowMgDl = 70f,
        targetHighMgDl = 180f,
        targetGlucose30MinMgDl = null,
        targetGlucose60MinMgDl = null,
        targetGlucose120MinMgDl = target120,
        targetClass30Min = null,
        targetClass60Min = null,
        targetClass120Min = null
    )
}