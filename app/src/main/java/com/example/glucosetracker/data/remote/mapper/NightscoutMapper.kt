package com.example.glucosetracker.data.remote.mapper

import com.example.glucosetracker.data.local.entities.GlucoseEntry
import com.example.glucosetracker.data.remote.dto.NightscoutEntryDto

fun NightscoutEntryDto.toEntity(): GlucoseEntry {

    return GlucoseEntry(
        glucoseLevel = glucose.toFloat(),
        timestamp = timestamp
    )
}