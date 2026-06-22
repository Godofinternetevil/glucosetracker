package com.example.glucosetracker.domain.ml

/** Domain model for a glucose prediction produced by the ML predictor. */
data class PredictionResult(
    val horizonMinutes: Int,
    val predictedMmolL: Float,
    val predictedMgDl: Float,
    val trendLabel: String,
    val confidenceLabel: String,
    val createdAt: Long,
    val riskClass: String,
    val isHypoRisk: Boolean,
    val isHyperRisk: Boolean
)