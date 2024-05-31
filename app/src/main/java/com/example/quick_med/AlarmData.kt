package com.example.quick_med

data class AlarmData(
    val name: String,
    val hour: Int,
    val minute: Int,
    val repeatDays: BooleanArray
)
