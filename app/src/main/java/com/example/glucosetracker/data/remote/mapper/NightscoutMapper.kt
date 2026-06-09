package com.example.glucosetracker.data.remote.mapper

import com.example.glucosetracker.data.local.entities.DataSourceConfig
import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.remote.dto.NightscoutEntryDto
import com.google.gson.Gson

private const val MG_DL_TO_MMOL_L = 18.0182f
private val gson = Gson()

fun NightscoutEntryDto.toEntity(sourceType: String = DataSourceConfig.SOURCE_NIGHTSCOUT): GlucoseEntry {
    val glucoseMgDl = glucose.toFloat()
    return GlucoseEntry(
        glucoseMmolL = glucoseMgDl / MG_DL_TO_MMOL_L,
        glucoseMgDl = glucoseMgDl,
        source = sourceType,
        sourceId = id ?: timestamp.toString(),
        trendDirection = direction,
        rawPayload = gson.toJson(this),
        timestamp = timestamp
    )
}