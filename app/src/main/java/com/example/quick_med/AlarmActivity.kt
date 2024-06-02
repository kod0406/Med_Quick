package com.example.quick_med

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Alarm : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_set_alarm_1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val alarmLabelEditText = findViewById<EditText>(R.id.alarmLabelEditText)
        val alarmTimePicker = findViewById<TimePicker>(R.id.alarmTimePicker)
        val switchEnableAlarm = findViewById<Switch>(R.id.switch_enable_alarm)
        val deleteButton = findViewById<Button>(R.id.deleteButton)
        val saveButton = findViewById<Button>(R.id.saveButton)

        // 알람 삭제 버튼 클릭 이벤트
        deleteButton.setOnClickListener {
            // 알람 삭제 로직 구현
        }

        // 알람 저장 버튼 클릭 이벤트
        saveButton.setOnClickListener {
            // 알람 저장 로직 구현
        }
    }
}
