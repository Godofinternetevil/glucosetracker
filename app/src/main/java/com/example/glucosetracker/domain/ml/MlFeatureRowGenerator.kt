package com.example.glucosetracker.domain.ml

import java.time.Instant
import java.time.ZoneId
import kotlin.math.abs

/** Builds a 5-minute, uniformly sampled feature series from glucose, meal, and insulin events. */
class MlFeatureRowGenerator(
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val stepMinutes: Long = 5,
    private val maxGlucoseStalenessMinutes: Long = 20
) {
    fun generate(
        events: List<MlEvent>,
        startTimestamp: Long,
        endTimestamp: Long,
        targetLowMgDl: Float,
        targetHighMgDl: Float
    ): List<MlFeatureRow> {
        require(startTimestamp <= endTimestamp) { "startTimestamp must be <= endTimestamp" }
        val glucose = events.filterIsInstance<MlEvent.Glucose>().sortedBy { it.timestamp }
        val meals = events.filterIsInstance<MlEvent.Meal>().sortedBy { it.timestamp }
        val insulin = events.filterIsInstance<MlEvent.Insulin>().sortedBy { it.timestamp }
        val stepMillis = stepMinutes * MILLIS_PER_MINUTE
        val alignedStart = alignUp(startTimestamp, stepMillis)
        val alignedEnd = alignDown(endTimestamp, stepMillis)
        if (alignedStart > alignedEnd) return emptyList()

        return generateSequence(alignedStart) { previous ->
            (previous + stepMillis).takeIf { it <= alignedEnd }
        }.map { timestamp ->
            val current = glucose.latestAtOrBefore(timestamp, maxGlucoseStalenessMinutes)
            val dateTime = Instant.ofEpochMilli(timestamp).atZone(zoneId)
            val target30 = glucose.nearestTo(timestamp + PredictionHorizon.MINUTES_30.minutes * MILLIS_PER_MINUTE, stepMillis / 2)
            val target60 = glucose.nearestTo(timestamp + PredictionHorizon.MINUTES_60.minutes * MILLIS_PER_MINUTE, stepMillis / 2)
            val target120 = glucose.nearestTo(timestamp + PredictionHorizon.MINUTES_120.minutes * MILLIS_PER_MINUTE, stepMillis / 2)

            MlFeatureRow(
                timestamp = timestamp,
                currentGlucoseMmolL = current?.mmolL,
                currentGlucoseMgDl = current?.mgDl,
                glucoseDelta15MinMgDl = glucose.deltaMgDl(timestamp, 15),
                glucoseDelta30MinMgDl = glucose.deltaMgDl(timestamp, 30),
                glucoseDelta60MinMgDl = glucose.deltaMgDl(timestamp, 60),
                carbsLast30Min = meals.sumCarbs(timestamp, 30),
                carbsLast60Min = meals.sumCarbs(timestamp, 60),
                carbsLast120Min = meals.sumCarbs(timestamp, 120),
                insulinLast30Min = insulin.sumUnits(timestamp, 30),
                insulinLast60Min = insulin.sumUnits(timestamp, 60),
                insulinLast180Min = insulin.sumUnits(timestamp, 180),
                hourOfDay = dateTime.hour,
                dayOfWeek = dateTime.dayOfWeek.value,
                targetLowMgDl = targetLowMgDl,
                targetHighMgDl = targetHighMgDl,
                targetGlucose30MinMgDl = target30?.mgDl,
                targetGlucose60MinMgDl = target60?.mgDl,
                targetGlucose120MinMgDl = target120?.mgDl,
                targetClass30Min = target30?.mgDl?.toTargetClass(targetLowMgDl, targetHighMgDl),
                targetClass60Min = target60?.mgDl?.toTargetClass(targetLowMgDl, targetHighMgDl),
                targetClass120Min = target120?.mgDl?.toTargetClass(targetLowMgDl, targetHighMgDl)
            )
        }.toList()
    }

    private fun List<MlEvent.Glucose>.deltaMgDl(timestamp: Long, minutes: Long): Float? {
        val current = latestAtOrBefore(timestamp, maxGlucoseStalenessMinutes) ?: return null
        val previous = latestAtOrBefore(timestamp - minutes * MILLIS_PER_MINUTE, maxGlucoseStalenessMinutes) ?: return null
        return current.mgDl - previous.mgDl
    }

    private fun List<MlEvent.Glucose>.latestAtOrBefore(timestamp: Long, maxAgeMinutes: Long): MlEvent.Glucose? =
        asReversed().firstOrNull { it.timestamp <= timestamp && timestamp - it.timestamp <= maxAgeMinutes * MILLIS_PER_MINUTE }

    private fun List<MlEvent.Glucose>.nearestTo(timestamp: Long, maxDistanceMillis: Long): MlEvent.Glucose? =
        minByOrNull { abs(it.timestamp - timestamp) }?.takeIf { abs(it.timestamp - timestamp) <= maxDistanceMillis }

    private fun List<MlEvent.Meal>.sumCarbs(timestamp: Long, minutes: Long): Float =
        filter { it.timestamp > timestamp - minutes * MILLIS_PER_MINUTE && it.timestamp <= timestamp }.sumOf { it.carbsGrams.toDouble() }.toFloat()

    private fun List<MlEvent.Insulin>.sumUnits(timestamp: Long, minutes: Long): Float =
        filter { it.timestamp > timestamp - minutes * MILLIS_PER_MINUTE && it.timestamp <= timestamp }.sumOf { it.units.toDouble() }.toFloat()

    private fun Float.toTargetClass(low: Float, high: Float): MlFeatureRow.TargetClass = when {
        this < low -> MlFeatureRow.TargetClass.HYPO
        this > high -> MlFeatureRow.TargetClass.HIGH
        else -> MlFeatureRow.TargetClass.TARGET
    }

    private fun alignUp(value: Long, step: Long): Long = if (value % step == 0L) value else ((value / step) + 1) * step
    private fun alignDown(value: Long, step: Long): Long = (value / step) * step

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
    }
}