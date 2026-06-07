package com.example.glucosetracker.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NightscoutEntryDto(
    @SerializedName("_id")
    val id: String?,

    @SerializedName("sgv")
    val glucose: Int,

    @SerializedName("date")
    val timestamp: Long,

    @SerializedName("dateString")
    val dateString: String?,

    @SerializedName("direction")
    val direction: String?
)