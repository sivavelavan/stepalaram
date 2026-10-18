package com.example.stepalarm

import android.Manifest
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var timeBtn: Button
    private lateinit var preview: ImageView

    private val pick = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        saveWallpaper(uri); showPreview()
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val p = Alarm.prefs(this)
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 96, 48, 48) }

        timeBtn = Button(this).apply { setOnClickListener { pickTime() } }
        val wallBtn = Button(this).apply { text = "Choose wallpaper"; setOnClickListener { pick.launch("image/*") } }
        preview = ImageView(this).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        val sw = Switch(this).apply {
            text = "Alarm on"; isChecked = p.getBoolean("on", false)
            setOnCheckedChangeListener { _, on ->
                p.edit().putBoolean("on", on).apply()
                if (on) { askPermissions(); Alarm.schedule(this@MainActivity) } else Alarm.cancel(this@MainActivity)
            }
        }
        col.addView(TextView(this).apply { text = "Step Alarm: walk 20 steps to stop it"; textSize = 20f })
        col.addView(timeBtn); col.addView(wallBtn); col.addView(sw)
        col.addView(preview, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(col)
        updateTimeText(); showPreview()
    }

    private fun updateTimeText() {
        val p = Alarm.prefs(this)
        timeBtn.text = "Alarm time: %02d:%02d".format(p.getInt("h", 7), p.getInt("m", 0))
    }

    private fun pickTime() {
        val p = Alarm.prefs(this)
        TimePickerDialog(this, { _, h, m ->
            p.edit().putInt("h", h).putInt("m", m).apply()
            updateTimeText()
            if (p.getBoolean("on", false)) Alarm.schedule(this)
        }, p.getInt("h", 7), p.getInt("m", 0), false).show()
    }

    private fun saveWallpaper(uri: Uri) {
        val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
        contentResolver.openInputStream(uri)?.use { ins ->
            val bmp = BitmapFactory.decodeStream(ins, null, opts) ?: return
            File(filesDir, "wallpaper.jpg").outputStream().use { bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) }
        }
    }

    private fun showPreview() {
        val f = File(filesDir, "wallpaper.jpg")
        if (f.exists()) preview.setImageBitmap(BitmapFactory.decodeFile(f.path))
    }

    private fun askPermissions() {
        val need = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 33) need += Manifest.permission.POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= 29) need += Manifest.permission.ACTIVITY_RECOGNITION
        requestPermissions(need.toTypedArray(), 0)
        if (Build.VERSION.SDK_INT >= 34 &&
            !getSystemService(NotificationManager::class.java).canUseFullScreenIntent())
            startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:$packageName")))
    }
}
