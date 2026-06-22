package com.example.glucosetracker.domain.ml

import kotlin.math.sqrt

/**
 * In-memory baseline trained on exported ML feature rows.
 *
 * The first MVP uses k-nearest-neighbors over rows that already have current glucose and
 * target_glucose_120_min_mg_dl. It intentionally avoids external ML dependencies so the model can
 * be rebuilt cheaply from personal history when the app opens or the source data changes.
 */
class TrainedGlucosePredictor(
    private val minTrainingRows: Int = MIN_TRAINING_ROWS,
    private val neighborCount: Int = NEIGHBOR_COUNT
) {
    fun train(rows: List<MlFeatureRow>): Model? {
        val trainingRows = rows.mapNotNull { row -> row.toTrainingExampleOrNull() }
        if (trainingRows.size < minTrainingRows) return null
        return Model(trainingRows, neighborCount)
    }

    inner class Model internal constructor(
        private val examples: List<TrainingExample>,
        private val neighborCount: Int
    ) {
        val trainingRowCount: Int = examples.size

        fun predict(row: MlFeatureRow): TrainedPrediction? {
            val features = row.toFeatureVectorOrNull() ?: return null
            val neighbors = examples
                .asSequence()
                .map { example -> Neighbor(example = example, distance = features.distanceTo(example.features)) }
                .sortedBy { it.distance }
                .take(neighborCount)
                .toList()

            if (neighbors.isEmpty()) return null

            var weightedTarget = 0.0
            var totalWeight = 0.0
            neighbors.forEach { neighbor ->
                val weight = 1.0 / (neighbor.distance + DISTANCE_EPSILON)
                weightedTarget += neighbor.example.targetMgDl * weight
                totalWeight += weight
            }
            if (totalWeight == 0.0) return null

            return TrainedPrediction(
                horizonMinutes = PredictionHorizon.MINUTES_120.minutes,

                predictedMgDl = (weightedTarget / totalWeight).toFloat(),
                trainingRowCount = trainingRowCount
            )
        }
    }

    data class TrainedPrediction(
        val horizonMinutes: Int,
        val predictedMgDl: Float,
        val trainingRowCount: Int
    )

    internal data class TrainingExample(
        val features: List<Double>,
        val targetMgDl: Float
    )

    private data class Neighbor(
        val example: TrainingExample,
        val distance: Double
    )

    private fun MlFeatureRow.toTrainingExampleOrNull(): TrainingExample? {
        val current = currentGlucoseMgDl ?: return null
        if (current <= 0f) return null
        val target = targetGlucose120MinMgDl ?: return null
        return TrainingExample(features = toFeatureVectorOrNull() ?: return null, targetMgDl = target)
    }

    private fun MlFeatureRow.toFeatureVectorOrNull(): List<Double>? {
        val current = currentGlucoseMgDl ?: return null
        if (current <= 0f) return null
        return listOf(
            current.toDouble() / 400.0,
            (glucoseDelta15MinMgDl ?: 0f).toDouble() / 100.0,
            (glucoseDelta30MinMgDl ?: 0f).toDouble() / 150.0,
            (glucoseDelta60MinMgDl ?: 0f).toDouble() / 250.0,
            carbsLast30Min.toDouble() / 120.0,
            carbsLast60Min.toDouble() / 160.0,
            carbsLast120Min.toDouble() / 220.0,
            insulinLast30Min.toDouble() / 20.0,
            insulinLast60Min.toDouble() / 25.0,
            insulinLast180Min.toDouble() / 40.0,
            hourOfDay.toDouble() / 23.0,
            dayOfWeek.toDouble() / 7.0,
            targetLowMgDl.toDouble() / 200.0,
            targetHighMgDl.toDouble() / 300.0
        )
    }

    private fun List<Double>.distanceTo(other: List<Double>): Double = sqrt(
        indices.sumOf { index ->
            val diff = this[index] - other[index]
            diff * diff
        }
    )

    private companion object {
        const val MIN_TRAINING_ROWS = 24
        const val NEIGHBOR_COUNT = 8
        const val DISTANCE_EPSILON = 0.0001
    }
}