package com.example.glucosetracker.domain.ml

data class PredictedGlucose(
    val horizonMinutes: Int,
    val predictedMmolL: Float,
    val predictedMgDl: Float,
    val trendLabel: String,
    val confidenceLabel: String,
    val sourceLabel: String
)