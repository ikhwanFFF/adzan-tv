package com.adzantv.app

import com.google.gson.annotations.SerializedName

data class PrayerResponse(
    val data: PrayerData
)

data class PrayerData(
    val timings: PrayerTimings
)

data class PrayerTimings(

    @SerializedName("Fajr")
    val fajr: String,

    @SerializedName("Dhuhr")
    val dhuhr: String,

    @SerializedName("Asr")
    val asr: String,

    @SerializedName("Maghrib")
    val maghrib: String,

    @SerializedName("Isha")
    val isha: String
)
