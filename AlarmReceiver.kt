package com.example.stepalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        if (!Alarm.prefs(c).getBoolean("on", false)) return
        if (i.action == Intent.ACTION_BOOT_COMPLETED) { Alarm.schedule(c); return }
        c.startForegroundService(Intent(c, AlarmService::class.java))
        Alarm.schedule(c) // repeat daily
    }
}
