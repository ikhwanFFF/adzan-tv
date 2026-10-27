package com.adzantv.app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.core.content.ContextCompat
import com.adzantv.app.Prefs.enabled
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : Activity() {
    private lateinit var info: TextView
    private val cities = linkedMapOf(
        "Jakarta" to (-6.2088 to 106.8456), "Bekasi" to (-6.2383 to 106.9756),
        "Bandung" to (-6.9175 to 107.6191), "Semarang" to (-6.9667 to 110.4167),
        "Yogyakarta" to (-7.7956 to 110.3695), "Surabaya" to (-7.2575 to 112.7521),
        "Medan" to (3.5952 to 98.6722), "Palembang" to (-2.9761 to 104.7754),
        "Makassar" to (-5.1477 to 119.4327), "Denpasar" to (-8.6705 to 115.2126)
    )

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL; setPadding(80, 48, 80, 48) }
        root.addView(TextView(this).apply { text = "Adzan TV"; textSize = 36f })
        info = TextView(this).apply { textSize = 22f; setPadding(0, 24, 0, 24) }
        root.addView(info)

        fun btn(t: String, f: () -> Unit) = Button(this).apply { text = t; setOnClickListener { f() } }.also { root.addView(it) }
        val first = btn("Pilih Kota") { pickCity() }
        btn("Aktif / Nonaktif") { enabled = !enabled; Scheduler.scheduleNext(this); refresh() }
        btn("Tes Adzan Sekarang") { ContextCompat.startForegroundService(this, Intent(this, AdzanService::class.java).putExtra("name", "Tes")) }
        btn("Izinkan Alarm Tepat Waktu") {
            if (Build.VERSION.SDK_INT >= 31) startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
        }
        setContentView(ScrollView(this).apply { addView(root) })
        first.requestFocus()

        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        Scheduler.scheduleNext(this)
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun pickCity() {
        val names = cities.keys.toTypedArray()
        AlertDialog.Builder(this).setTitle("Pilih Kota").setItems(names) { _, i ->
            val (la, lo) = cities[names[i]]!!
            Prefs.save(this, names[i], la, lo); Scheduler.scheduleNext(this); refresh()
        }.show()
    }

    private fun refresh() {

    CoroutineScope(Dispatchers.IO).launch {

        val f = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault())

        val list = PrayerTimes.forDate(
            LocalDate.now(),
            Prefs.lat(this@MainActivity),
            Prefs.lon(this@MainActivity)
        ).joinToString("\n") {
            "${it.name.padEnd(8)} ${
                f.format(
                    Instant.ofEpochMilli(it.timeMillis)
                )
            }"
        }

        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val exactOk =
            Build.VERSION.SDK_INT < 31 ||
            am.canScheduleExactAlarms()

        val text =
            "Kota: ${Prefs.city(this@MainActivity)}\n" +
            "Status: ${if (enabled) "AKTIF" else "NONAKTIF"}" +
            (if (!exactOk)
                " (izin alarm tepat waktu belum diberikan)"
             else "") +
            "\n\n$list"

        withContext(Dispatchers.Main) {
            info.text = text
        }
    }
}
}
