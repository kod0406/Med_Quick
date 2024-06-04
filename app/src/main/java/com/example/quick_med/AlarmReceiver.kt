package com.example.quick_med

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmName = intent.getStringExtra("ALARM_NAME") ?: return
        val sharedPreferences = context.getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE)
        val alarmList = getAlarmList(sharedPreferences)

        // 알람이 활성화되어 있는지 확인
        val alarmData = alarmList.find { it.name == alarmName }
        if (alarmData?.isEnabled == true) {
            // 알람이 활성화되어 있으면 AlarmPopupActivity를 실행
            val popupIntent = Intent(context, AlarmPopupActivity::class.java).apply {
                putExtra("ALARM_NAME", alarmName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(popupIntent)
        } else {
            Log.d("AlarmReceiver", "Alarm is not enabled, skipping alarm popup.")
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
}
