package com.example.stepalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object Alarm {
    fun prefs(c: Context) = c.getSharedPreferences("alarm", Context.MODE_PRIVATE)

    private fun pi(c: Context) = PendingIntent.getBroadcast(
        c, 1, Intent(c, AlarmReceiver::class.java).setAction("RING"),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun schedule(c: Context) {
        val p = prefs(c)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, p.getInt("h", 7)); set(Calendar.MINUTE, p.getInt("m", 0))
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val am = c.getSystemService(AlarmManager::class.java)
        am.setAlarmClock(AlarmManager.AlarmClockInfo(cal.timeInMillis, pi(c)), pi(c))
    }

    fun cancel(c: Context) = c.getSystemService(AlarmManager::class.java).cancel(pi(c))
}
