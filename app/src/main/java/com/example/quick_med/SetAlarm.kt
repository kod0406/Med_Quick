package com.example.quick_med

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SetAlarm : AppCompatActivity() {
    private companion object {
        const val REQUEST_CODE = 1 // 요청 코드 정의
    }

    private lateinit var alarmListLayout: LinearLayout // 알람 목록 레이아웃

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 엣지투엣지 활성화
        setContentView(R.layout.activity_set_alarm_0) // 레이아웃 설정

        // 시스템 바 패딩 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 오늘 날짜를 표시해줌
        val dateTextView: TextView = findViewById(R.id.dateTextView)
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("M월 d일 E요일", Locale.KOREAN)
        val dateString = dateFormat.format(calendar.time)
        dateTextView.text = dateString

        alarmListLayout = findViewById(R.id.alarmListLayout) // 알람 목록 레이아웃 초기화

        // 알람 설정 화면으로 이동하는 버튼
        val button: Button = findViewById(R.id.addbutton)
        button.setOnClickListener {
            val intent = Intent(this, SetAlarm_Add::class.java)
            startActivityForResult(intent, REQUEST_CODE) // 알람 설정 화면으로 이동
        }

        // 저장된 알람 불러오기
        loadAlarms()
    }

    // 저장된 알람 불러오기
    private fun loadAlarms() {
        val sharedPreferences = getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE)
        val alarmList = getAlarmList(sharedPreferences)

        for (alarmData in alarmList) {
            val amPm = if (alarmData.hour >= 12) "PM" else "AM"
            val displayHour = if (alarmData.hour > 12) alarmData.hour - 12 else if (alarmData.hour == 0) 12 else alarmData.hour
            val displayMinute = String.format("%02d", alarmData.minute)
            val time = "$amPm $displayHour:$displayMinute"
            addAlarmToList(alarmData, time)
        }
    }

    // 알람 목록 가져오기
    private fun getAlarmList(sharedPreferences: SharedPreferences): MutableList<AlarmData> {
        val json = sharedPreferences.getString("ALARM_LIST", null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<AlarmData>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    // 다른 액티비티에서 결과 받기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val label = data?.getStringExtra("ALARM_NAME")
            val hour = data?.getIntExtra("ALARM_HOUR", -1)
            val minute = data?.getIntExtra("ALARM_MINUTE", -1)
            if (label != null && hour != -1 && minute != -1) {
                val time = String.format("%02d:%02d", hour, minute)
                val alarmData = AlarmData(label, hour, minute)
                addAlarmToList(alarmData, time) // 알람 목록에 추가
            }
        }
    }

    // 알람 목록에 추가
    @SuppressLint("MissingInflatedId")
    private fun addAlarmToList(alarmData: AlarmData, time: String) {
        val alarmView = layoutInflater.inflate(R.layout.alarm_item, alarmListLayout, false)
        val alarmLabelTextView = alarmView.findViewById<TextView>(R.id.alarmLabelTextView)
        val alarmTimeTextView = alarmView.findViewById<TextView>(R.id.alarmTimeTextView)

        alarmLabelTextView.text = alarmData.name
        alarmTimeTextView.text = time

        // 알람 수정 화면 표시
        alarmView.setOnClickListener {
            val dialog = SetAlarmModify().apply {
                arguments = Bundle().apply {
                    putString("ALARM_NAME", alarmData.name)
                    putInt("ALARM_HOUR", alarmData.hour)
                    putInt("ALARM_MINUTE", alarmData.minute)
                }
            }
            dialog.show(supportFragmentManager, "SetAlarmModify")
        }

        alarmListLayout.addView(alarmView, 0) // 알람 목록에 뷰 추가
    }
}
