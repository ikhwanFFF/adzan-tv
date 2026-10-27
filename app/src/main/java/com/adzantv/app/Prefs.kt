package com.adzantv.app

import android.content.Context

object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("adzan", Context.MODE_PRIVATE)
    fun lat(c: Context) = sp(c).getFloat("lat", -6.2088f).toDouble()   // default Jakarta
    fun lon(c: Context) = sp(c).getFloat("lon", 106.8456f).toDouble()
    fun city(c: Context) = sp(c).getString("city", "Jakarta") ?: "Jakarta"
    fun save(c: Context, city: String, lat: Double, lon: Double) =
        sp(c).edit().putString("city", city).putFloat("lat", lat.toFloat()).putFloat("lon", lon.toFloat()).apply()
    var Context.enabled: Boolean
        get() = sp(this).getBoolean("enabled", true)
        set(v) { sp(this).edit().putBoolean("enabled", v).apply() }
}
