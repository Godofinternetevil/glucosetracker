package com.example.glucosetracker.domain.ml

/** Prediction targets supported by the ML-ready feature generator. */
enum class PredictionHorizon(val minutes: Long) {
    MINUTES_30(30),
    MINUTES_60(60),
    MINUTES_120(120)
}