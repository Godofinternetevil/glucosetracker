package com.example.glucosetracker.domain.ml

import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.local.entities.InsulinEntry
import com.example.glucosetracker.data.local.entities.MealEntry
import com.example.glucosetracker.data.repository.GlucoseRepository
import kotlinx.coroutines.flow.first

/**
 * Builds normalized TFLite input tensors from the same point-in-time features used by [MlFeatureRowGenerator].
 *
 * Feature order must match the training script exactly:
 * 0 current_glucose_mg_dl, 1 glucose_delta_15_min_mg_dl, 2 glucose_delta_30_min_mg_dl,
 * 3 glucose_delta_60_min_mg_dl, 4 carbs_last_30_min_g, 5 carbs_last_60_min_g,
 * 6 carbs_last_120_min_g, 7 insulin_last_30_min_units, 8 insulin_last_60_min_units,
 * 9 insulin_last_180_min_units, 10 hour_of_day, 11 day_of_week.
 */
class PredictionInputBuilder(
    private val generator: MlFeatureRowGenerator = MlFeatureRowGenerator(),
    private val historyWindowMinutes: Long = HISTORY_WINDOW_MINUTES,
    private val targetLowMgDl: Float = DEFAULT_TARGET_LOW_MG_DL,
    private val targetHighMgDl: Float = DEFAULT_TARGET_HIGH_MG_DL
) {
    data class Result(
        val input: Array<FloatArray>,
        val confidence: Confidence,
        val latestGlucose: GlucoseEntry
    ) {
        val featureVector: FloatArray get() = input.first()
    }

    enum class Confidence {
        HIGH,
        LOW
    }

    suspend fun build(repository: GlucoseRepository, nowMillis: Long = System.currentTimeMillis()): Result? = build(
        glucose = repository.getGlucoseBetween(nowMillis - historyWindowMinutes * MILLIS_PER_MINUTE, nowMillis).first(),
        meals = repository.getMealsBetween(nowMillis - historyWindowMinutes * MILLIS_PER_MINUTE, nowMillis).first(),
        insulin = repository.getInsulinBetween(nowMillis - historyWindowMinutes * MILLIS_PER_MINUTE, nowMillis).first(),
        nowMillis = nowMillis
    )

    fun build(
        glucose: List<GlucoseEntry>,
        meals: List<MealEntry>,
        insulin: List<InsulinEntry>,
        nowMillis: Long = System.currentTimeMillis()
    ): Result? {
        val latestGlucose = glucose.maxByOrNull { it.timestamp } ?: return null
        val rows = generator.generate(
            events = glucose.map { MlEvent.Glucose(it.timestamp, it.glucoseMmolL, it.glucoseMgDl.takeIf { mgDl -> mgDl > 0f } ?: it.glucoseMmolL * MMOL_L_TO_MG_DL) } +
                    meals.map { MlEvent.Meal(it.timestamp, it.carbsGrams) } +
                    insulin.map { MlEvent.Insulin(it.timestamp, it.units) },
            startTimestamp = nowMillis - historyWindowMinutes * MILLIS_PER_MINUTE,
            endTimestamp = nowMillis,
            targetLowMgDl = targetLowMgDl,
            targetHighMgDl = targetHighMgDl
        )
        val row = rows.lastOrNull() ?: return null
        val requiredGlucoseFeatures = listOf(
            row.currentGlucoseMgDl,
            row.glucoseDelta15MinMgDl,
            row.glucoseDelta30MinMgDl,
            row.glucoseDelta60MinMgDl
        )
        if (requiredGlucoseFeatures.any { it == null }) return null

        val recentGlucoseCount = glucose.count { it.timestamp >= nowMillis - MIN_COMPLETE_HISTORY_MINUTES * MILLIS_PER_MINUTE }
        val confidence = if (recentGlucoseCount >= MIN_HISTORY_POINTS) Confidence.HIGH else Confidence.LOW

        return Result(
            input = arrayOf(row.toNormalizedFeatureVector()),
            confidence = confidence,
            latestGlucose = latestGlucose
        )
    }

    private fun MlFeatureRow.toNormalizedFeatureVector(): FloatArray = floatArrayOf(
        normalize(currentGlucoseMgDl.requireFeature(), GLUCOSE_MIN_MG_DL, GLUCOSE_MAX_MG_DL),
        normalize(glucoseDelta15MinMgDl.requireFeature(), DELTA_MIN_MG_DL, DELTA_MAX_MG_DL),
        normalize(glucoseDelta30MinMgDl.requireFeature(), DELTA_MIN_MG_DL, DELTA_MAX_MG_DL),
        normalize(glucoseDelta60MinMgDl.requireFeature(), DELTA_MIN_MG_DL, DELTA_MAX_MG_DL),
        normalize(carbsLast30Min, CARBS_MIN_GRAMS, CARBS_MAX_GRAMS),
        normalize(carbsLast60Min, CARBS_MIN_GRAMS, CARBS_MAX_GRAMS),
        normalize(carbsLast120Min, CARBS_MIN_GRAMS, CARBS_MAX_GRAMS),
        normalize(insulinLast30Min, INSULIN_MIN_UNITS, INSULIN_MAX_UNITS),
        normalize(insulinLast60Min, INSULIN_MIN_UNITS, INSULIN_MAX_UNITS),
        normalize(insulinLast180Min, INSULIN_MIN_UNITS, INSULIN_MAX_UNITS),
        normalize(hourOfDay.toFloat(), HOUR_MIN, HOUR_MAX),
        normalize(dayOfWeek.toFloat(), DAY_OF_WEEK_MIN, DAY_OF_WEEK_MAX)
    )

    private fun Float?.requireFeature(): Float = requireNotNull(this) { "Required prediction feature is missing" }
    private fun normalize(value: Float, min: Float, max: Float): Float = ((value - min) / (max - min)).coerceIn(0f, 1f)

    companion object {
        val FEATURE_NAMES = listOf(
            "current_glucose_mg_dl",
            "glucose_delta_15_min_mg_dl",
            "glucose_delta_30_min_mg_dl",
            "glucose_delta_60_min_mg_dl",
            "carbs_last_30_min_g",
            "carbs_last_60_min_g",
            "carbs_last_120_min_g",
            "insulin_last_30_min_units",
            "insulin_last_60_min_units",
            "insulin_last_180_min_units",
            "hour_of_day",
            "day_of_week"
        )

        const val FEATURE_COUNT = 12
        private const val MILLIS_PER_MINUTE = 60_000L
        private const val HISTORY_WINDOW_MINUTES = 180L
        private const val MIN_COMPLETE_HISTORY_MINUTES = 60L
        private const val MIN_HISTORY_POINTS = 2
        private const val DEFAULT_TARGET_LOW_MG_DL = 70f
        private const val DEFAULT_TARGET_HIGH_MG_DL = 180f
        private const val MMOL_L_TO_MG_DL = 18.0182f
        private const val GLUCOSE_MIN_MG_DL = 40f
        private const val GLUCOSE_MAX_MG_DL = 400f
        private const val DELTA_MIN_MG_DL = -120f
        private const val DELTA_MAX_MG_DL = 120f
        private const val CARBS_MIN_GRAMS = 0f
        private const val CARBS_MAX_GRAMS = 150f
        private const val INSULIN_MIN_UNITS = 0f
        private const val INSULIN_MAX_UNITS = 25f
        private const val HOUR_MIN = 0f
        private const val HOUR_MAX = 23f
        private const val DAY_OF_WEEK_MIN = 1f
        private const val DAY_OF_WEEK_MAX = 7f
    }
}