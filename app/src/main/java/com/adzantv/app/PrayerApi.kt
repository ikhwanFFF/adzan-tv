package com.adzantv.app

import retrofit2.http.GET
import retrofit2.http.Query

interface PrayerApi {

    @GET("v1/timings")
    suspend fun getPrayerTimes(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 11
    ): PrayerResponse
}
