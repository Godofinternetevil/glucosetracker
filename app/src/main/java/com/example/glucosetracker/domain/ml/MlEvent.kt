package com.example.glucosetracker.domain.ml

/** Normalized domain event consumed by the ML feature generator. */
sealed class MlEvent(open val timestamp: Long) {
    data class Glucose(
        override val timestamp: Long,
        val mmolL: Float,
        val mgDl: Float
    ) : MlEvent(timestamp)

    data class Meal(
        override val timestamp: Long,
        val carbsGrams: Float
    ) : MlEvent(timestamp)

    data class Insulin(
        override val timestamp: Long,
        val units: Float
    ) : MlEvent(timestamp)
}