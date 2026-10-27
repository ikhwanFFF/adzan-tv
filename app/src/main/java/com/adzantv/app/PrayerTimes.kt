package com.adzantv.app

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.*

data class Prayer(val name: String, val timeMillis: Long)

/** Hitung waktu sholat offline (metode Kemenag RI: Subuh 20°, Isya 18°, Ashar mazhab Syafi'i, ihtiyath +2 menit). */
object PrayerTimes {
    private fun rad(d: Double) = Math.toRadians(d)
    private fun deg(r: Double) = Math.toDegrees(r)
    private fun fix(a: Double, m: Double): Double { val r = a - m * floor(a / m); return r }

    private fun julian(y: Int, m: Int, d: Int): Double {
        var yy = y; var mm = m
        if (mm <= 2) { yy -= 1; mm += 12 }
        val a = floor(yy / 100.0)
        val b = 2 - a + floor(a / 4)
        return floor(365.25 * (yy + 4716)) + floor(30.6001 * (mm + 1)) + d + b - 1524.5
    }

    private fun sun(jd: Double): Pair<Double, Double> { // (deklinasi, equation of time dalam jam)
        val d = jd - 2451545.0
        val g = fix(357.529 + 0.98560028 * d, 360.0)
        val q = fix(280.459 + 0.98564736 * d, 360.0)
        val l = fix(q + 1.915 * sin(rad(g)) + 0.020 * sin(rad(2 * g)), 360.0)
        val e = 23.439 - 0.00000036 * d
        val ra = fix(deg(atan2(cos(rad(e)) * sin(rad(l)), cos(rad(l)))) / 15.0, 24.0)
        val decl = deg(asin(sin(rad(e)) * sin(rad(l))))
        val eqt = q / 15.0 - ra
        return decl to (eqt + if (eqt > 12) -24 else if (eqt < -12) 24 else 0).toDouble()
    }

    fun forDate(date: LocalDate, lat: Double, lon: Double): List<Prayer> {
        val jd = julian(date.year, date.monthValue, date.dayOfMonth) - lon / 360.0
        val (decl, eqt) = sun(jd)
        val noonUtc = 12.0 - lon / 15.0 - eqt // jam UTC

        fun angleTime(angle: Double): Double {
            val x = (-sin(rad(angle)) - sin(rad(decl)) * sin(rad(lat))) / (cos(rad(decl)) * cos(rad(lat)))
            return deg(acos(x.coerceIn(-1.0, 1.0))) / 15.0
        }
        val asrAngle = -deg(atan(1.0 / (1.0 + tan(rad(abs(lat - decl))))))

        val ih = 2.0 / 60.0 // ihtiyath 2 menit
        val times = listOf(
            "Subuh" to noonUtc - angleTime(20.0) + ih,
            "Dzuhur" to noonUtc + ih,
            "Ashar" to noonUtc + angleTime(asrAngle) + ih,
            "Maghrib" to noonUtc + angleTime(0.833) + ih,
            "Isya" to noonUtc + angleTime(18.0) + ih
        )
        val base = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        return times.map { (n, h) -> Prayer(n, base + (h * 3600000.0).toLong()) }
    }

    /** Waktu sholat berikutnya setelah sekarang (hari ini, lalu besok). */
    fun next(lat: Double, lon: Double, now: Long = System.currentTimeMillis()): Prayer {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        for (i in -1..2) {
            forDate(today.plusDays(i.toLong()), lat, lon).firstOrNull { it.timeMillis > now + 1000 }?.let { return it }
        }
        error("tidak ada waktu sholat")
    }
}
