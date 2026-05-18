package com.example.glucosetracker.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NightscoutEntryDto(

    @SerializedName("sgv")
    val glucose: Int,

    @SerializedName("date")
    val timestamp: Long,

    @SerializedName("direction")
    val direction: String?
)