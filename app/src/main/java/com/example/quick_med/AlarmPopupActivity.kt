package com.example.quick_med

import android.app.Activity
import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
//import android.widget.TextView

class AlarmPopupActivity : Activity() {

    companion object {
        private lateinit var ringtone: Ringtone
        fun playRingtone(context: Context) {
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(context, alarmUri)
            ringtone.play()
        }

        fun stopRingtone() {
            if (this::ringtone.isInitialized && ringtone.isPlaying) {
                ringtone.stop()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_popup)

        val alarmName = intent.getStringExtra("ALARM_NAME")
        val alarmNameTextView: TextView = findViewById(R.id.alarmNameTextView)
        alarmNameTextView.text = alarmName

        playRingtone(this)

        val stopAlarmButton: Button = findViewById(R.id.stopAlarmButton)
        stopAlarmButton.setOnClickListener {
            stopRingtone()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
    }
}
