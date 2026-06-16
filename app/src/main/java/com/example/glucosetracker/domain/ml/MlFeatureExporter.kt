package com.example.glucosetracker.domain.ml

import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.local.entities.InsulinEntry
import com.example.glucosetracker.data.local.entities.MealEntry
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Exports generated feature rows as standalone ML datasets. */
class MlFeatureExporter(
    private val generator: MlFeatureRowGenerator = MlFeatureRowGenerator()
) {
    fun exportCsv(rows: List<MlFeatureRow>): String =
        rows.joinToString(separator = "\n", prefix = HEADER.joinToString(",") + "\n") { row ->
            row.toColumns().joinToCsvRow()
        }

    fun exportJsonl(rows: List<MlFeatureRow>): String = rows.joinToString("\n") { row ->
        HEADER.zip(row.toColumns()).joinToString(prefix = "{", postfix = "}") { (name, value) ->
            "\"${name.escapeJson()}\":${value.toJsonValue(name)}"
        }
    }

    fun exportCsv(
        glucose: List<GlucoseEntry>,
        meals: List<MealEntry>,
        insulin: List<InsulinEntry>,
        startTimestamp: Long,
        endTimestamp: Long,
        targetLowMgDl: Float,
        targetHighMgDl: Float
    ): String = exportCsv(generateRows(glucose, meals, insulin, startTimestamp, endTimestamp, targetLowMgDl, targetHighMgDl))

    fun exportJsonl(
        glucose: List<GlucoseEntry>,
        meals: List<MealEntry>,
        insulin: List<InsulinEntry>,
        startTimestamp: Long,
        endTimestamp: Long,
        targetLowMgDl: Float,
        targetHighMgDl: Float
    ): String = exportJsonl(generateRows(glucose, meals, insulin, startTimestamp, endTimestamp, targetLowMgDl, targetHighMgDl))

    fun generateRows(
        glucose: List<GlucoseEntry>,
        meals: List<MealEntry>,
        insulin: List<InsulinEntry>,
        startTimestamp: Long,
        endTimestamp: Long,
        targetLowMgDl: Float,
        targetHighMgDl: Float
    ): List<MlFeatureRow> = generator.generate(
        events = glucose.map { MlEvent.Glucose(it.timestamp, it.glucoseMmolL, it.glucoseMgDl) } +
                meals.map { MlEvent.Meal(it.timestamp, it.carbsGrams) } +
                insulin.map { MlEvent.Insulin(it.timestamp, it.units) },
        startTimestamp = startTimestamp,
        endTimestamp = endTimestamp,
        targetLowMgDl = targetLowMgDl,
        targetHighMgDl = targetHighMgDl
    )

    private fun MlFeatureRow.toColumns(): List<String?> = listOf(
        DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(timestamp).atOffset(ZoneOffset.UTC)),
        timestamp.toString(),
        currentGlucoseMmolL?.toString(),
        currentGlucoseMgDl?.toString(),
        glucoseDelta15MinMgDl?.toString(),
        glucoseDelta30MinMgDl?.toString(),
        glucoseDelta60MinMgDl?.toString(),
        carbsLast30Min.toString(),
        carbsLast60Min.toString(),
        carbsLast120Min.toString(),
        insulinLast30Min.toString(),
        insulinLast60Min.toString(),
        insulinLast180Min.toString(),
        hourOfDay.toString(),
        dayOfWeek.toString(),
        targetLowMgDl.toString(),
        targetHighMgDl.toString(),
        targetGlucose30MinMgDl?.toString(),
        targetGlucose60MinMgDl?.toString(),
        targetGlucose120MinMgDl?.toString(),
        targetClass30Min?.name?.lowercase(),
        targetClass60Min?.name?.lowercase(),
        targetClass120Min?.name?.lowercase()
    )

    private fun List<String?>.joinToCsvRow(): String = joinToString(",") { value ->
        val escaped = value.orEmpty().replace("\"", "\"\"")
        if (escaped.any { it == ',' || it == '\n' || it == '\r' || it == '"' }) "\"$escaped\"" else escaped
    }

    private fun String?.toJsonValue(column: String): String = when {
        isNullOrEmpty() -> "null"
        column in NUMERIC_COLUMNS -> this
        else -> "\"${escapeJson()}\""
    }

    private fun String.escapeJson(): String = buildString {
        this@escapeJson.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
    }

    companion object {
        val HEADER = listOf(
            "timestamp_iso",
            "timestamp_epoch_ms",
            "current_glucose_mmol_l",
            "current_glucose_mg_dl",
            "glucose_delta_15_min_mg_dl",
            "glucose_delta_30_min_mg_dl",
            "glucose_delta_60_min_mg_dl",
            "carbs_last_30_min_g",
            "carbs_last_60_min_g",
            "carbs_last_120_min_g",
            "insulin_last_30_min_units",
            "insulin_last_60_min_units",
            "insulin_last_180_min_units",
            "hour_of_day",
            "day_of_week",
            "target_low_mg_dl",
            "target_high_mg_dl",
            "target_glucose_30_min_mg_dl",
            "target_glucose_60_min_mg_dl",
            "target_glucose_120_min_mg_dl",
            "target_class_30_min",
            "target_class_60_min",
            "target_class_120_min"
        )

        private val NUMERIC_COLUMNS = HEADER.toSet() - setOf("timestamp_iso", "target_class_30_min", "target_class_60_min", "target_class_120_min")
    }
}