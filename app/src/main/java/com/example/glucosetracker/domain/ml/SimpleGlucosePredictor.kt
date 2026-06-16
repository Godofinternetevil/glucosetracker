package com.example.glucosetracker.domain.ml

import kotlin.math.abs

/**
 * Baseline glucose forecast for MVP experiments only.
 * This prediction is not a medical recommendation and must not be used for treatment decisions.
 */
class SimpleGlucosePredictor {
    fun predict30Min(
        glucose: List<MlEvent.Glucose>,
        meals: List<MlEvent.Meal>,
        insulin: List<MlEvent.Insulin>,
        now: Long = System.currentTimeMillis()
    ): PredictedGlucose? {
        val recentGlucose = glucose
            .filter { it.timestamp <= now }
            .sortedBy { it.timestamp }

        if (recentGlucose.size < 2) return null

        val current = recentGlucose.last()
        val thirtyMinutesAgo = current.timestamp - HORIZON_MINUTES * MILLIS_PER_MINUTE
        val previous = recentGlucose.minByOrNull { abs(it.timestamp - thirtyMinutesAgo) } ?: return null
        if (previous.timestamp == current.timestamp) return null

        val delta30 = current.mmolL - previous.mmolL
        val carbsLast120Min = meals.sumCarbs(now, CARBS_WINDOW_MINUTES)
        val insulinLast180Min = insulin.sumUnits(now, INSULIN_WINDOW_MINUTES)

        val trendPrediction = current.mmolL + delta30 * TREND_COEFFICIENT
        val carbsEffect = carbsLast120Min / CARBS_RATIO * CARBS_COEFFICIENT
        val insulinEffect = insulinLast180Min * INSULIN_COEFFICIENT
        val predictedMmolL = (trendPrediction + carbsEffect - insulinEffect).coerceIn(MIN_GLUCOSE_MMOL_L, MAX_GLUCOSE_MMOL_L)

        return PredictedGlucose(
            horizonMinutes = HORIZON_MINUTES.toInt(),
            predictedMmolL = predictedMmolL,
            predictedMgDl = predictedMmolL * MMOL_L_TO_MG_DL,
            trendLabel = delta30.toTrendLabel(),
            confidenceLabel = previous.toConfidenceLabel(thirtyMinutesAgo)
        )
    }

    private fun List<MlEvent.Meal>.sumCarbs(timestamp: Long, minutes: Long): Float =
        filter { it.timestamp > timestamp - minutes * MILLIS_PER_MINUTE && it.timestamp <= timestamp }
            .sumOf { it.carbsGrams.toDouble() }
            .toFloat()

    private fun List<MlEvent.Insulin>.sumUnits(timestamp: Long, minutes: Long): Float =
        filter { it.timestamp > timestamp - minutes * MILLIS_PER_MINUTE && it.timestamp <= timestamp }
            .sumOf { it.units.toDouble() }
            .toFloat()

    private fun Float.toTrendLabel(): String = when {
        this > RISING_DELTA_THRESHOLD_MMOL_L -> "rising"
        this < -RISING_DELTA_THRESHOLD_MMOL_L -> "falling"
        else -> "stable"
    }

    private fun MlEvent.Glucose.toConfidenceLabel(targetTimestamp: Long): String =
        if (abs(timestamp - targetTimestamp) <= CONFIDENT_DISTANCE_MINUTES * MILLIS_PER_MINUTE) "medium" else "low"

    private companion object {
        const val HORIZON_MINUTES = 30L
        const val CARBS_WINDOW_MINUTES = 120L
        const val INSULIN_WINDOW_MINUTES = 180L
        const val MILLIS_PER_MINUTE = 60_000L
        const val MMOL_L_TO_MG_DL = 18.0182f
        const val TREND_COEFFICIENT = 0.8f
        const val CARBS_RATIO = 12f
        const val CARBS_COEFFICIENT = 0.3f
        const val INSULIN_COEFFICIENT = 1.5f
        const val MIN_GLUCOSE_MMOL_L = 2.0f
        const val MAX_GLUCOSE_MMOL_L = 25.0f
        const val RISING_DELTA_THRESHOLD_MMOL_L = 0.3f
        const val CONFIDENT_DISTANCE_MINUTES = 10L
    }
}