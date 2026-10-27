package com.adzantv.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.adzantv.app.Prefs.enabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Scheduler {

    fun scheduleNext(c: Context) {

        CoroutineScope(Dispatchers.IO).launch {

            val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val pi = pending(c, "")

            if (!c.enabled) {
                am.cancel(pi)
                return@launch
            }

            val p = PrayerTimes.next(
                Prefs.lat(c),
                Prefs.lon(c)
            )

            val pending = pending(
                c,
                p.name
            )

            val canExact =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                am.canScheduleExactAlarms()

            if (canExact) {
                am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    p.timeMillis,
                    pending
                )
            } else {
                am.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    p.timeMillis,
                    pending
                )
            }
        }
    }

    private fun pending(
        c: Context,
        name: String
    ): PendingIntent {

        val i = Intent(
            c,
            AlarmReceiver::class.java
        ).putExtra("name", name)

        return PendingIntent.getBroadcast(
            c,
            1,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or
           
