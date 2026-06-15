package com.example.glucosetracker.data.export

import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.local.entities.InsulinEntry
import com.example.glucosetracker.data.local.entities.MealEntry
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class DatasetExporter {

    enum class Format(val extension: String, val mimeType: String) {
        CSV("csv", "text/csv"),
        JSONL("jsonl", "application/x-ndjson")
    }

    fun exportCsv(
        glucose: List<GlucoseEntry>,
        meals: List<MealEntry>,
        insulin: List<InsulinEntry>
    ): String = buildDatasetRows(glucose, meals, insulin)
        .joinToString(separator = "\n", prefix = CSV_HEADER.joinToString(",") + "\n") { row ->
            CSV_HEADER.map { column -> row[column].orEmpty() }.joinToCsvRow()
        }

    fun exportJsonl(
        glucose: List<GlucoseEntry>,
        meals: List<MealEntry>,
        insulin: List<InsulinEntry>
    ): String = buildDatasetRows(glucose, meals, insulin)
        .joinToString("\n") { row ->
            CSV_HEADER.joinToString(prefix = "{", postfix = "}") { column ->
                "\"${column.escapeJson()}\":${row[column].toJsonValue(column)}"
            }
        }

    private fun buildDatasetRows(
        glucose: List<GlucoseEntry>,
        meals: List<MealEntry>,
        insulin: List<InsulinEntry>
    ): List<Map<String, String?>> {
        val glucoseRows = glucose.map { entry ->
            baseRow(entry.timestamp, EVENT_GLUCOSE, entry.source, note = null) + mapOf(
                "glucose_mmol_l" to entry.glucoseMmolL.toString(),
                "glucose_mg_dl" to entry.glucoseMgDl.toString(),
                "trend_direction" to entry.trendDirection
            )
        }
        val mealRows = meals.map { meal ->
            baseRow(meal.timestamp, EVENT_MEAL, meal.source, meal.note) + mapOf(
                "carbs_g" to meal.carbsGrams.toString(),
                "protein_g" to meal.proteinGrams?.toString(),
                "fat_g" to meal.fatGrams?.toString()
            )
        }
        val insulinRows = insulin.map { dose ->
            baseRow(dose.timestamp, EVENT_INSULIN, dose.source, dose.note) + mapOf(
                "insulin_units" to dose.units.toString(),
                "insulin_type" to dose.insulinType
            )
        }
        return (glucoseRows + mealRows + insulinRows).sortedBy { it["timestamp_epoch_ms"]?.toLongOrNull() ?: Long.MAX_VALUE }
    }

    private fun baseRow(timestamp: Long, eventType: String, source: String, note: String?): Map<String, String?> =
        CSV_HEADER.associateWith { null as String? } + mapOf(
            "timestamp_iso" to DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(timestamp).atOffset(ZoneOffset.UTC)),
            "timestamp_epoch_ms" to timestamp.toString(),
            "event_type" to eventType,
            "source" to source,
            "note" to note
        )

    private fun List<String>.joinToCsvRow(): String = joinToString(",") { value ->
        val escaped = value.replace("\"", "\"\"")
        if (escaped.any { it == ',' || it == '\n' || it == '\r' || it == '"' }) "\"$escaped\"" else escaped
    }

    private fun String?.toJsonValue(column: String): String = if (isNullOrEmpty()) {
        "null"
    } else if (column in NUMERIC_COLUMNS) {
        this
    } else {
        "\"${escapeJson()}\""
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
        const val EVENT_GLUCOSE = "glucose"
        const val EVENT_MEAL = "meal"
        const val EVENT_INSULIN = "insulin"

        private val NUMERIC_COLUMNS = setOf(
            "timestamp_epoch_ms",
            "glucose_mmol_l",
            "glucose_mg_dl",
            "carbs_g",
            "protein_g",
            "fat_g",
            "insulin_units"
        )

        val CSV_HEADER = listOf(
            "timestamp_iso",
            "timestamp_epoch_ms",
            "event_type",
            "glucose_mmol_l",
            "glucose_mg_dl",
            "trend_direction",
            "carbs_g",
            "protein_g",
            "fat_g",
            "insulin_units",
            "insulin_type",
            "source",
            "note"
        )
    }
}