package com.example.glucosetracker.domain.ml

data class PredictionResult(
    val horizonMinutes: Int,
    val predictedMmolL: Float,
    val predictedMgDl: Float,
    val trendLabel: String,
    val confidenceLabel: String
)