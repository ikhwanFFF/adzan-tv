package com.adzantv.app

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

data class Prayer(
    val name: String,
    val timeMillis: Long
)

object PrayerTimes {

    fun parseTimeToMillis(
        date: LocalDate,
        time: String
    ): Long {

        val cleanTime = time.take(5)

        val parts = cleanTime.split(":")

        val dateTime = LocalDateTime.of(
            date.year,
            date.monthValue,
            date.dayOfMonth,
            parts[0].toInt(),
            parts[1].toInt()
        )

        return dateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    suspend fun forDate(
        date: LocalDate,
        lat: Double,
        lon: Double
    ): List<Prayer> {

        val response = ApiClient.prayerApi.getPrayerTimes(
            latitude = lat,
            longitude = lon
        )

        val t = response.data.timings

        return listOf(
            Prayer(
                "Subuh",
                parseTimeToMillis(date, t.fajr)
            ),

            Prayer(
                "Dzuhur",
                parseTimeToMillis(date, t.dhuhr)
            ),

            Prayer(
                "Ashar",
                parseTimeToMillis(date, t.asr)
            ),

            Prayer(
                "Maghrib",
                parseTimeToMillis(date, t.maghrib)
            ),

            Prayer(
                "Isya",
                parseTimeToMillis(date, t.isha)
            )
        )
    }

    suspend fun next(
        lat: Double,
        lon: Double,
        now: Long = System.currentTimeMillis()
    ): Prayer {

        val today = LocalDate.now()

        val prayers = forDate(
            today,
            lat,
            lon
        )

        prayers.firstOrNull {
            it.timeMillis > now
        }?.let {
            return it
        }

        val tomorrowPrayers = forDate(
            today.plusDays(1),
            lat,
            lon
        )

        return tomorrowPrayers.first()
    }
}
