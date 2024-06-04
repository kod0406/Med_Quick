package com.example.quick_med

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class SetAlarm_Add : AppCompatActivity() {

    private lateinit var spinnerAmPm: Spinner
    private lateinit var numberPickerHour: NumberPicker
    private lateinit var numberPickerMinute: NumberPicker
    private lateinit var editTextAlarmName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_alarm_2) // 알람 추가 (alarm_2)

        // 뷰 초기화
        spinnerAmPm = findViewById(R.id.spinner_am_pm)
        numberPickerHour = findViewById(R.id.numberPicker_hour)
        numberPickerMinute = findViewById(R.id.numberPicker_minute)
        editTextAlarmName = findViewById(R.id.editText_alarm_name)

        // NumberPicker 범위 설정
        numberPickerHour.minValue = 1
        numberPickerHour.maxValue = 12
        numberPickerMinute.minValue = 0
        numberPickerMinute.maxValue = 59

        // 버튼 클릭 리스너 설정
        val buttonCreateAlarm: Button = findViewById(R.id.button_create_alarm)
        val buttonCancelAlarm: Button = findViewById(R.id.button_cancel_alarm)
        buttonCreateAlarm.setOnClickListener { setAlarm() }
        buttonCancelAlarm.setOnClickListener { finish() }
    }

    private fun setAlarm() {
        val amPm = spinnerAmPm.selectedItemPosition // 0은 AM, 1은 PM
        var hour = numberPickerHour.value
        val minute = numberPickerMinute.value
        val alarmName = editTextAlarmName.text.toString()

        // 24시간 형식으로 변환
        if (amPm == 1 && hour != 12) {
            hour += 12
        } else if (amPm == 0 && hour == 12) {
            hour = 0
        }

        // 알람 데이터를 SharedPreferences에 저장
        val sharedPreferences = getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val alarmList = getAlarmList(sharedPreferences)
        alarmList.add(AlarmData(alarmName, hour, minute, true)) // 활성화 상태 저장
        editor.putString("ALARM_LIST", Gson().toJson(alarmList))
        editor.apply()

        // 선택된 시간으로 캘린더 설정
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1) // 현재 시간보다 이전이면 다음 날로 설정
            }
        }

        // 알람 설정
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("ALARM_NAME", alarmName)
            action = "com.example.quick_med.ALARM_ACTION"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarmName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        Toast.makeText(this, "알람이 설정되었습니다", Toast.LENGTH_SHORT).show()

        // 설정한 알람 데이터를 인텐트에 추가하여 반환
        val resultIntent = Intent().apply {
            putExtra("ALARM_NAME", alarmName)
            putExtra("ALARM_HOUR", hour)
            putExtra("ALARM_MINUTE", minute)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
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
}
