package com.example.glucosetracker.domain.ml

import android.content.Context
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.local.entities.InsulinEntry
import com.example.glucosetracker.data.local.entities.MealEntry
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.roundToInt

/** TensorFlow Lite glucose predictor backed by glucose_prediction_model.tflite from assets. */
class GlucosePredictor(context: Context) : AutoCloseable {
    private val interpreter: Interpreter = Interpreter(loadModel(context))

    fun predict(normalizedFeatures: FloatArray): PredictionResult {
        require(normalizedFeatures.isNotEmpty()) { "normalizedFeatures must not be empty" }

        val outputSize = interpreter.getOutputTensor(0).shape().lastOrNull()?.takeIf { it > 0 } ?: 1
        val output = Array(1) { FloatArray(outputSize) }
        interpreter.run(arrayOf(normalizedFeatures), output)

        return output[0].toPredictionResult(currentMmolL = null)
    }

    fun predict30Min(
        glucose: List<GlucoseEntry>,
        meals: List<MealEntry>,
        insulin: List<InsulinEntry>,
        nowMillis: Long
    ): PredictionResult? {
        if (glucose.size < MIN_HISTORY_POINTS) return null

        val latestGlucose = glucose.maxByOrNull { it.timestamp } ?: return null
        val normalizedFeatures = buildNormalizedFeatureVector(glucose, meals, insulin, nowMillis, latestGlucose)
        val result = predict(normalizedFeatures)
        return result.copy(
            horizonMinutes = PREDICTION_WINDOW_MINUTES,
            trendLabel = trendLabel(latestGlucose.glucoseMmolL, result.predictedMmolL),
            confidenceLabel = confidenceLabel(glucose, nowMillis),
            createdAt = nowMillis
        )
    }

    override fun close() {
        interpreter.close()
    }

    private fun buildNormalizedFeatureVector(
        glucose: List<GlucoseEntry>,
        meals: List<MealEntry>,
        insulin: List<InsulinEntry>,
        nowMillis: Long,
        latestGlucose: GlucoseEntry
    ): FloatArray {
        val latestMgDl = latestGlucose.glucoseMgDl.takeIf { it > 0f } ?: latestGlucose.glucoseMmolL * MMOL_L_TO_MG_DL
        return floatArrayOf(
            normalize(latestMgDl, GLUCOSE_MIN_MG_DL, GLUCOSE_MAX_MG_DL),
            normalize(glucose.deltaMgDl(nowMillis, 15), DELTA_MIN_MG_DL, DELTA_MAX_MG_DL),
            normalize(glucose.deltaMgDl(nowMillis, 30), DELTA_MIN_MG_DL, DELTA_MAX_MG_DL),
            normalize(glucose.deltaMgDl(nowMillis, 60), DELTA_MIN_MG_DL, DELTA_MAX_MG_DL),
            normalize(meals.sumRecentCarbs(nowMillis, 30), CARBS_MIN_GRAMS, CARBS_MAX_GRAMS),
            normalize(meals.sumRecentCarbs(nowMillis, 60), CARBS_MIN_GRAMS, CARBS_MAX_GRAMS),
            normalize(meals.sumRecentCarbs(nowMillis, 120), CARBS_MIN_GRAMS, CARBS_MAX_GRAMS),
            normalize(insulin.sumRecentUnits(nowMillis, 30), INSULIN_MIN_UNITS, INSULIN_MAX_UNITS),
            normalize(insulin.sumRecentUnits(nowMillis, 60), INSULIN_MIN_UNITS, INSULIN_MAX_UNITS),
            normalize(insulin.sumRecentUnits(nowMillis, 180), INSULIN_MIN_UNITS, INSULIN_MAX_UNITS)
        )
    }

    private fun FloatArray.toPredictionResult(currentMmolL: Float?): PredictionResult {
        val rawPrediction = firstOrNull() ?: 0f
        val predictedMgDl = rawPrediction.takeIf { it > NORMALIZED_OUTPUT_MAX } ?: denormalize(rawPrediction, GLUCOSE_MIN_MG_DL, GLUCOSE_MAX_MG_DL)
        val predictedMmolL = predictedMgDl / MMOL_L_TO_MG_DL
        val boundedPredictedMmolL = predictedMmolL.coerceAtLeast(0f)
        val boundedPredictedMgDl = predictedMgDl.coerceAtLeast(0f)
        return PredictionResult(
            horizonMinutes = getOrNull(1)?.roundToInt()?.takeIf { it > 0 } ?: PREDICTION_WINDOW_MINUTES,
            predictedMmolL = boundedPredictedMmolL,
            predictedMgDl = boundedPredictedMgDl,
            trendLabel = currentMmolL?.let { trendLabel(it, boundedPredictedMmolL) } ?: "Model",
            confidenceLabel = "Model",
            createdAt = System.currentTimeMillis(),
            riskClass = riskClass(boundedPredictedMmolL),
            isHypoRisk = boundedPredictedMmolL < HYPO_RISK_THRESHOLD_MMOL_L,
            isHyperRisk = boundedPredictedMmolL > HYPER_RISK_THRESHOLD_MMOL_L
        )
    }

    private fun List<GlucoseEntry>.deltaMgDl(nowMillis: Long, minutes: Long): Float {
        val current = maxByOrNull { it.timestamp } ?: return 0f
        val previous = filter { it.timestamp <= nowMillis - minutes * MILLIS_PER_MINUTE }.maxByOrNull { it.timestamp } ?: current
        val currentMgDl = current.glucoseMgDl.takeIf { it > 0f } ?: current.glucoseMmolL * MMOL_L_TO_MG_DL
        val previousMgDl = previous.glucoseMgDl.takeIf { it > 0f } ?: previous.glucoseMmolL * MMOL_L_TO_MG_DL
        return currentMgDl - previousMgDl
    }

    private fun List<MealEntry>.sumRecentCarbs(nowMillis: Long, minutes: Long): Float =
        filter { it.timestamp in (nowMillis - minutes * MILLIS_PER_MINUTE)..nowMillis }.sumOf { it.carbsGrams.toDouble() }.toFloat()

    private fun List<InsulinEntry>.sumRecentUnits(nowMillis: Long, minutes: Long): Float =
        filter { it.timestamp in (nowMillis - minutes * MILLIS_PER_MINUTE)..nowMillis }.sumOf { it.units.toDouble() }.toFloat()

    private fun confidenceLabel(glucose: List<GlucoseEntry>, nowMillis: Long): String {
        val latestGlucose = glucose.maxByOrNull { it.timestamp } ?: return "Low"
        val hasRecentGlucose = latestGlucose.timestamp >= nowMillis - RECENT_GLUCOSE_WINDOW_MILLIS
        val hasHistory = glucose.count { it.timestamp >= nowMillis - HISTORY_WINDOW_MILLIS } >= MIN_HISTORY_POINTS
        return if (hasRecentGlucose && hasHistory) "High" else "Medium"
    }

    private fun trendLabel(currentMmolL: Float, predictedMmolL: Float): String = when {
        predictedMmolL - currentMmolL >= 1f -> "Rising"
        currentMmolL - predictedMmolL >= 1f -> "Falling"
        else -> "Stable"
    }

    private fun riskClass(predictedMmolL: Float): String = when {
        predictedMmolL < HYPO_RISK_THRESHOLD_MMOL_L -> "Hypo"
        predictedMmolL > HYPER_RISK_THRESHOLD_MMOL_L -> "Hyper"
        else -> "In range"
    }

    private fun normalize(value: Float, min: Float, max: Float): Float = ((value - min) / (max - min)).coerceIn(0f, 1f)
    private fun denormalize(value: Float, min: Float, max: Float): Float = min + value.coerceIn(0f, 1f) * (max - min)

    private fun loadModel(context: Context): MappedByteBuffer {
        context.assets.openFd(MODEL_ASSET_NAME).use { assetFileDescriptor ->
            FileInputStream(assetFileDescriptor.fileDescriptor).use { inputStream ->
                return inputStream.channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    assetFileDescriptor.startOffset,
                    assetFileDescriptor.declaredLength
                )
            }
        }
    }

    private companion object {
        const val MODEL_ASSET_NAME = "glucose_prediction_model.tflite"
        const val PREDICTION_WINDOW_MINUTES = 30
        const val MILLIS_PER_MINUTE = 60_000L
        const val MMOL_L_TO_MG_DL = 18.0182f
        const val RECENT_GLUCOSE_WINDOW_MILLIS = 20 * MILLIS_PER_MINUTE
        const val HISTORY_WINDOW_MILLIS = 60 * MILLIS_PER_MINUTE
        const val MIN_HISTORY_POINTS = 2
        const val GLUCOSE_MIN_MG_DL = 40f
        const val GLUCOSE_MAX_MG_DL = 400f
        const val DELTA_MIN_MG_DL = -120f
        const val DELTA_MAX_MG_DL = 120f
        const val CARBS_MIN_GRAMS = 0f
        const val CARBS_MAX_GRAMS = 150f
        const val INSULIN_MIN_UNITS = 0f
        const val INSULIN_MAX_UNITS = 25f
        const val NORMALIZED_OUTPUT_MAX = 1.5f
        const val HYPO_RISK_THRESHOLD_MMOL_L = 3.9f
        const val HYPER_RISK_THRESHOLD_MMOL_L = 10.0f
    }
}