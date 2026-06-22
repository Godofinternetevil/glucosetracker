package com.example.glucosetracker.data.repository

import com.example.glucosetracker.data.local.PredictionDao
import com.example.glucosetracker.data.local.entities.PredictionEntry
import com.example.glucosetracker.domain.ml.PredictionResult

class GlucosePredictionRepository(
    private val dao: PredictionDao
) {

    fun getLatestPredictions(limit: Int = DEFAULT_HISTORY_LIMIT) = dao.getLatestPredictions(limit)

    suspend fun savePrediction(
        result: PredictionResult,
        createdAt: Long = System.currentTimeMillis(),
        modelVersion: String? = null
    ): Long = dao.insertPrediction(result.toEntry(createdAt, modelVersion))

    suspend fun deletePredictionsOlderThan(createdBefore: Long): Int = dao.deletePredictionsOlderThan(createdBefore)

    private fun PredictionResult.toEntry(createdAt: Long, modelVersion: String?): PredictionEntry = PredictionEntry(
        createdAt = createdAt,
        horizonMinutes = horizonMinutes,
        predictedMmolL = predictedMmolL,
        predictedMgDl = predictedMgDl,
        trendLabel = trendLabel,
        confidenceLabel = confidenceLabel,
        modelVersion = modelVersion
    )

    private companion object {
        const val DEFAULT_HISTORY_LIMIT = 50
    }
}