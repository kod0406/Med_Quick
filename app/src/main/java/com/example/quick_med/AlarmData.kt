package com.example.quick_med

data class AlarmData(
    val name: String,
    val hour: Int,
    val minute: Int,
    val repeatDays: BooleanArray // 예를 들어 요일별 반복 여부를 나타내는 리스트
)
