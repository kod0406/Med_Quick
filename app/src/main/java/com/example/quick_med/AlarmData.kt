package com.example.quick_med

data class AlarmData(
    val name: String,
    val hour: Int,
    val minute: Int,
    var isEnabled: Boolean = true // 기본값을 true로 설정
)