package com.example.glucosetracker.domain.ml

/** One uniformly sampled, ML-ready feature row. */
data class MlFeatureRow(
    val timestamp: Long,
    val currentGlucoseMmolL: Float?,
    val currentGlucoseMgDl: Float?,
    val glucoseDelta15MinMgDl: Float?,
    val glucoseDelta30MinMgDl: Float?,
    val glucoseDelta60MinMgDl: Float?,
    val carbsLast30Min: Float,
    val carbsLast60Min: Float,
    val carbsLast120Min: Float,
    val insulinLast30Min: Float,
    val insulinLast60Min: Float,
    val insulinLast180Min: Float,
    val hourOfDay: Int,
    val dayOfWeek: Int,
    val targetLowMgDl: Float,
    val targetHighMgDl: Float,
    val targetGlucose30MinMgDl: Float?,
    val targetGlucose60MinMgDl: Float?,
    val targetGlucose120MinMgDl: Float?,
    val targetClass30Min: TargetClass?,
    val targetClass60Min: TargetClass?,
    val targetClass120Min: TargetClass?
) {
    enum class TargetClass {
        HYPO,
        TARGET,
        HIGH
    }
}