package com.example.stepalarm

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

class AlarmService : Service() {
    private var player: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("alarm", "Alarm", NotificationManager.IMPORTANCE_HIGH)
        )
        val ring = PendingIntent.getActivity(
            this, 0, Intent(this, RingActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(this, "alarm")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Wake up!").setContentText("Walk 20 steps to stop the alarm")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true).setContentIntent(ring).setFullScreenIntent(ring, true).build()
        ServiceCompat.startForeground(this, 1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)

        if (player == null) {
            player = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
                setDataSource(this@AlarmService, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                isLooping = true; prepare(); start()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() { player?.release(); player = null }
    override fun onBind(i: Intent?): IBinder? = null
}
