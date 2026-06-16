package com.example.glucosetracker.domain.ml

import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.local.entities.InsulinEntry
import com.example.glucosetracker.data.local.entities.MealEntry

/** Simple deterministic 30-minute glucose predictor based on recent glucose, meals, and insulin. */
class SimpleGlucosePredictor {
    fun predict30Min(
        glucose: List<GlucoseEntry>,
        meals: List<MealEntry>,
        insulin: List<InsulinEntry>,
        nowMillis: Long
    ): PredictedGlucose? {
        val latestGlucose = glucose.maxByOrNull { it.timestamp } ?: return null
        val recentCarbs = meals
            .filter { it.timestamp in (nowMillis - MEAL_WINDOW_MILLIS)..nowMillis }
            .sumOf { it.carbsGrams.toDouble() }
            .toFloat()
        val recentInsulin = insulin
            .filter { it.timestamp in (nowMillis - INSULIN_WINDOW_MILLIS)..nowMillis }
            .sumOf { it.units.toDouble() }
            .toFloat()

        val predictedMmolL = (latestGlucose.glucoseMmolL + recentCarbs * CARB_IMPACT_MMOL_L - recentInsulin * INSULIN_IMPACT_MMOL_L)
            .coerceAtLeast(0f)

        return PredictedGlucose(
            horizonMinutes = PREDICTION_WINDOW_MINUTES,
            predictedMmolL = predictedMmolL,
            predictedMgDl = predictedMmolL * MMOL_L_TO_MG_DL,
            trendLabel = trendLabel(latestGlucose.glucoseMmolL, predictedMmolL),
            confidenceLabel = confidenceLabel(glucose, nowMillis)
        )
    }

    private fun trendLabel(currentMmolL: Float, predictedMmolL: Float): String = when {
        predictedMmolL - currentMmolL >= 1f -> "Rising"
        currentMmolL - predictedMmolL >= 1f -> "Falling"
        else -> "Stable"
    }

    private fun confidenceLabel(glucose: List<GlucoseEntry>, nowMillis: Long): String {
        val latestGlucose = glucose.maxByOrNull { it.timestamp } ?: return "Low"
        val hasRecentGlucose = latestGlucose.timestamp >= nowMillis - RECENT_GLUCOSE_WINDOW_MILLIS
        val hasHistory = glucose.count { it.timestamp >= nowMillis - HISTORY_WINDOW_MILLIS } >= MIN_HISTORY_POINTS
        return if (hasRecentGlucose && hasHistory) "Medium" else "Low"
    }

    private companion object {
        const val PREDICTION_WINDOW_MINUTES = 30
        const val PREDICTION_WINDOW_MILLIS = PREDICTION_WINDOW_MINUTES * 60 * 1000L
        const val MEAL_WINDOW_MILLIS = 2 * 60 * 60 * 1000L
        const val INSULIN_WINDOW_MILLIS = 4 * 60 * 60 * 1000L
        const val CARB_IMPACT_MMOL_L = 0.05f
        const val INSULIN_IMPACT_MMOL_L = 1.5f
        const val MMOL_L_TO_MG_DL = 18.0182f
        const val RECENT_GLUCOSE_WINDOW_MILLIS = 20 * 60 * 1000L
        const val HISTORY_WINDOW_MILLIS = 60 * 60 * 1000L
        const val MIN_HISTORY_POINTS = 2
    }
}