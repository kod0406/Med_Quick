package com.example.quick_med

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
            val resultIntent = Intent().apply {
                putExtra("ALARM_INDEX", alarmIndex)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        saveButton.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("ALARM_NAME", alarmLabelEditText.text.toString())
                putExtra("ALARM_HOUR", alarmTimePicker.hour)
                putExtra("ALARM_MINUTE", alarmTimePicker.minute)
                putExtra("ALARM_INDEX", alarmIndex)
                putExtra("ALARM_ENABLED", enableAlarmSwitch.isChecked)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
