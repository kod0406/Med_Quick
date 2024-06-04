package com.example.quick_med

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class SetAlarm_Modify : AppCompatActivity() {

    private lateinit var alarmLabelEditText: EditText
    private lateinit var alarmTimePicker: TimePicker
    private lateinit var enableAlarmSwitch: Switch
    private lateinit var deleteButton: Button
    private lateinit var saveButton: Button

    private var alarmIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_alarm_1)

        alarmLabelEditText = findViewById(R.id.alarmLabelEditText)
        alarmTimePicker = findViewById(R.id.alarmTimePicker)
        enableAlarmSwitch = findViewById(R.id.switch_enable_alarm)
        deleteButton = findViewById(R.id.deleteButton)
        saveButton = findViewById(R.id.saveButton)

        val intent = intent
        alarmLabelEditText.setText(intent.getStringExtra("ALARM_NAME"))
        alarmTimePicker.hour = intent.getIntExtra("ALARM_HOUR", 0)
        alarmTimePicker.minute = intent.getIntExtra("ALARM_MINUTE", 0)
        enableAlarmSwitch.isChecked = intent.getBooleanExtra("ALARM_ENABLED", true)
        alarmIndex = intent.getIntExtra("ALARM_INDEX", -1)

        deleteButton.setOnClickListener {
            cancelAlarm(alarmIndex)
            val resultIntent = Intent().apply {
                putExtra("ALARM_INDEX", alarmIndex)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        saveButton.setOnClickListener {
            val alarmName = alarmLabelEditText.text.toString()
            val hour = alarmTimePicker.hour
            val minute = alarmTimePicker.minute
            val isEnabled = enableAlarmSwitch.isChecked

            val alarmData = AlarmData(alarmName, hour, minute, BooleanArray(7), isEnabled)
            cancelAlarm(alarmIndex) // 기존 알람 취소
            setAlarm(alarmData) // 새로운 알람 설정

            val resultIntent = Intent().apply {
                putExtra("ALARM_NAME", alarmName)
                putExtra("ALARM_HOUR", hour)
                putExtra("ALARM_MINUTE", minute)
                putExtra("ALARM_INDEX", alarmIndex)
                putExtra("ALARM_ENABLED", isEnabled)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun cancelAlarm(index: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            index,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setAlarm(alarmData: AlarmData) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("ALARM_NAME", alarmData.name)
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarmData.hour)
            set(Calendar.MINUTE, alarmData.minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarmData.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }
}


