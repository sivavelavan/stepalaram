package com.example.stepalarm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.*
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class RingActivity : AppCompatActivity(), SensorEventListener {
    private val goal = 20
    private var steps = 0
    private lateinit var label: TextView
    private lateinit var sm: SensorManager

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setShowWhenLocked(true); setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val root = FrameLayout(this)
        val img = ImageView(this).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        val f = File(filesDir, "wallpaper.jpg")
        if (f.exists()) img.setImageURI(android.net.Uri.fromFile(f)) else img.setImageDrawable(ColorDrawable(Color.parseColor("#1B1B2F")))
        label = TextView(this).apply {
            textSize = 36f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
            setShadowLayer(12f, 0f, 0f, Color.BLACK)
        }
        root.addView(img, FrameLayout.LayoutParams(-1, -1))
        root.addView(label, FrameLayout.LayoutParams(-1, -2, Gravity.CENTER))
        setContentView(root)
        update()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {} // can't back out
        })

        sm = getSystemService(SENSOR_SERVICE) as SensorManager
        if (Build.VERSION.SDK_INT >= 29 &&
            checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 1)
    }

    private fun update() { label.text = "Walk $goal steps to stop the alarm\n\n$steps / $goal" }

    override fun onResume() {
        super.onResume()
        val s = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (s == null) Toast.makeText(this, "No step sensor on this phone", Toast.LENGTH_LONG).show()
        else sm.registerListener(this, s, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onRequestPermissionsResult(rc: Int, p: Array<out String>, r: IntArray) {
        super.onRequestPermissionsResult(rc, p, r)
        onResume()
    }

    override fun onPause() { super.onPause(); sm.unregisterListener(this) }

    override fun onSensorChanged(e: SensorEvent) {
        steps++; update()
        if (steps >= goal) { stopService(Intent(this, AlarmService::class.java)); finishAndRemoveTask() }
    }
    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
}
