package com.example.quick_med

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
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
            // 알람이 활성화되어 있으면 노티피케이션 생성 및 소리 울림
            showNotification(context, alarmName)
            // 알람이 활성화되어 있으면 AlarmPopupActivity를 실행
            val popupIntent = Intent(context, AlarmPopupActivity::class.java).apply {
                putExtra("ALARM_NAME", alarmName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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

    private fun showNotification(context: Context, alarmName: String) {
        val channelId = "ALARM_CHANNEL"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 채널 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alarm Notifications", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Channel for Alarm notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(context, AlarmPopupActivity::class.java).apply {
            putExtra("ALARM_NAME", alarmName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Alarm")
            .setContentText(alarmName)
            .setSmallIcon(R.drawable.ic_alarm)  // 적절한 아이콘으로 변경하세요
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .build()

        notificationManager.notify(alarmName.hashCode(), notification)
    }
}
