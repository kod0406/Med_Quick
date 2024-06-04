package com.example.quick_med

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SetAlarm : AppCompatActivity() {
    private companion object {
        const val REQUEST_CODE_ADD = 1 // 알람 추가 요청 코드
        const val REQUEST_CODE_MODIFY = 2 // 알람 수정 요청 코드
        private const val REQUEST_CODE_EXACT_ALARM = 3// 정확한 알람 요청 코드
    }
    private lateinit var alarmListLayout: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var alarmList: MutableList<AlarmData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_set_alarm_0)

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
        alarmListLayout = findViewById(R.id.alarmListLayout)
        sharedPreferences = getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE)

        // 알람 설정 화면으로 이동하는 버튼
        val button: Button = findViewById(R.id.addbutton)
        button.setOnClickListener {
            val intent = Intent(this, SetAlarm_Add::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD) // 알람 설정 화면으로 이동
        }

        // 저장된 알람 불러오기
        loadAlarms()

        // 정확한 알람 권한 요청
        checkExactAlarmPermission()
    }

    private fun loadAlarms() {
        alarmList = getAlarmList(sharedPreferences)
        for (i in alarmList.indices) {
            val alarmData = alarmList[i]
            val amPm = if (alarmData.hour >= 12) "PM" else "AM"
            val displayHour = if (alarmData.hour > 12) alarmData.hour - 12 else if (alarmData.hour == 0) 12 else alarmData.hour
            val displayMinute = String.format("%02d", alarmData.minute)
            val time = "$amPm $displayHour:$displayMinute"
            addAlarmToList(alarmData, time, i)
        }
    }

    private fun getAlarmList(sharedPreferences: SharedPreferences): MutableList<AlarmData> {
        val json = sharedPreferences.getString("ALARM_LIST", null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<AlarmData>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_EXACT_ALARM) {
                alarmList.forEach { alarmData ->
                    if (alarmData.isEnabled) {
                        setAlarm(alarmData)
                    }
                }
            } else {
                val label = data?.getStringExtra("ALARM_NAME")
                val hour = data?.getIntExtra("ALARM_HOUR", -1) ?: -1
                val minute = data?.getIntExtra("ALARM_MINUTE", -1) ?: -1
                val alarmIndex = data?.getIntExtra("ALARM_INDEX", -1) ?: -1
                val isEnabled = data?.getBooleanExtra("ALARM_ENABLED", true) ?: true

                if (label != null && hour != -1 && minute != -1) {
                    val time = String.format("%02d:%02d", hour, minute)
                    val alarmData = AlarmData(label, hour, minute, BooleanArray(7), isEnabled)

                    if (requestCode == REQUEST_CODE_ADD) {
                        addAlarmToList(alarmData, time, alarmList.size)
                        saveAlarm(alarmData)
                    } else if (requestCode == REQUEST_CODE_MODIFY && alarmIndex != -1) {
                        modifyAlarmInList(alarmIndex, alarmData, time)
                        updateAlarm(alarmIndex, alarmData)
                    }
                } else if (requestCode == REQUEST_CODE_MODIFY && alarmIndex != -1) {
                    alarmListLayout.removeViewAt(alarmIndex)
                    removeAlarm(alarmIndex)
                }
            }
        }
    }
    private fun saveAlarm(alarmData: AlarmData) {
        alarmList.add(alarmData)
        val editor = sharedPreferences.edit()
        editor.putString("ALARM_LIST", Gson().toJson(alarmList))
        editor.apply()
    }

    private fun updateAlarm(index: Int, alarmData: AlarmData) {
        alarmList[index] = alarmData
        val editor = sharedPreferences.edit()
        editor.putString("ALARM_LIST", Gson().toJson(alarmList))
        editor.apply()
    }

    private fun removeAlarm(index: Int) {
        alarmList.removeAt(index)
        val editor = sharedPreferences.edit()
        editor.putString("ALARM_LIST", Gson().toJson(alarmList))
        editor.apply()
    }

    @SuppressLint("MissingInflatedId")
    private fun addAlarmToList(alarmData: AlarmData, time: String, index: Int) {
        val alarmView = layoutInflater.inflate(R.layout.alarm_item, alarmListLayout, false)
        val alarmLabelTextView = alarmView.findViewById<TextView>(R.id.alarmLabelTextView)
        val alarmTimeTextView = alarmView.findViewById<TextView>(R.id.alarmTimeTextView)
        val alarmSwitch = alarmView.findViewById<Switch>(R.id.alarmSwitch)

        alarmLabelTextView.text = alarmData.name
        alarmTimeTextView.text = time
        alarmSwitch.isChecked = alarmData.isEnabled

        val alarmDaysTextView = alarmView.findViewById<TextView>(R.id.alarmDaysTextView)
        val daysArray = arrayOf("일", "월", "화", "수", "목", "금", "토")
        val selectedDays = alarmData.daysOfWeek.mapIndexed { index, isSelected ->
            if (isSelected) daysArray[index] else ""
        }.filter { it.isNotEmpty() }
        alarmDaysTextView.text = selectedDays.joinToString(", ")

        alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
            alarmData.isEnabled = isChecked
            updateAlarm(index, alarmData)
            if (isChecked) {
                setAlarm(alarmData)
            } else {
                cancelAlarm(alarmData)
            }
        }

        alarmView.setOnClickListener {
            val intent = Intent(this, SetAlarm_Modify::class.java).apply {
                putExtra("ALARM_NAME", alarmData.name)
                putExtra("ALARM_HOUR", alarmData.hour)
                putExtra("ALARM_MINUTE", alarmData.minute)
                putExtra("ALARM_INDEX", index)
                putExtra("ALARM_ENABLED", alarmData.isEnabled)
            }
            startActivityForResult(intent, REQUEST_CODE_MODIFY)
        }

        alarmListLayout.addView(alarmView, index)
    }

    private fun modifyAlarmInList(index: Int, alarmData: AlarmData, time: String) {
        val alarmView = layoutInflater.inflate(R.layout.alarm_item, alarmListLayout, false)
        val alarmLabelTextView = alarmView.findViewById<TextView>(R.id.alarmLabelTextView)
        val alarmTimeTextView = alarmView.findViewById<TextView>(R.id.alarmTimeTextView)
        val alarmSwitch = alarmView.findViewById<Switch>(R.id.alarmSwitch)

        alarmLabelTextView.text = alarmData.name
        alarmTimeTextView.text = time
        alarmSwitch.isChecked = alarmData.isEnabled

        alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
            alarmData.isEnabled = isChecked
            updateAlarm(index, alarmData)
            if (isChecked) {
                setAlarm(alarmData)
            } else {
                cancelAlarm(alarmData)
            }
        }

        alarmListLayout.removeViewAt(index)
        alarmListLayout.addView(alarmView, index)

        alarmView.setOnClickListener {
            val intent = Intent(this, SetAlarm_Modify::class.java).apply {
                putExtra("ALARM_NAME", alarmData.name)
                putExtra("ALARM_HOUR", alarmData.hour)
                putExtra("ALARM_MINUTE", alarmData.minute)
                putExtra("ALARM_INDEX", index)
                putExtra("ALARM_ENABLED", alarmData.isEnabled)
            }
            startActivityForResult(intent, REQUEST_CODE_MODIFY)
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, REQUEST_CODE_EXACT_ALARM)
            }
        }
    }


    private fun setAlarm(alarmData: AlarmData) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {
            checkExactAlarmPermission()
            return
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("ALARM_NAME", alarmData.name)
            action = "com.example.quick_med.ALARM_ACTION"
        }

        if (alarmData.daysOfWeek.contains(true)) {
            alarmData.daysOfWeek.forEachIndexed { index, isEnabled ->
                if (isEnabled) {
                    val dayOfWeekCalendar = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, index + 1)
                        set(Calendar.HOUR_OF_DAY, alarmData.hour)
                        set(Calendar.MINUTE, alarmData.minute)
                        set(Calendar.SECOND, 0)
                        if (before(Calendar.getInstance())) {
                            add(Calendar.DATE, 7)
                        }
                    }
                    val repeatingIntent = PendingIntent.getBroadcast(
                        this,
                        alarmData.hashCode() + index,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        dayOfWeekCalendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY * 7,
                        repeatingIntent
                    )
                    Log.d("SetAlarm", "Alarm set for ${dayOfWeekCalendar.time}")
                }
            }
        } else {
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
            Log.d("SetAlarm", "Exact alarm set for ${calendar.time}")
        }
    }
    private fun cancelAlarm(alarmData: AlarmData) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        alarmData.daysOfWeek.forEachIndexed { index, _ ->
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                alarmData.hashCode() + index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }


}
