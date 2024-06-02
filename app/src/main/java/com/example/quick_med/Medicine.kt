package com.example.quick_med

data class Medicine(
    val name: String,
    val description: String = "해당 API에서는 정보를 제공하지 않습니다.",
    val imageUrl: String? = null,
    val dosage: String = "해당 API에서는 정보를 제공하지 않습니다.",
    val sideEffects: String? = "해당 API에서는 정보를 제공하지 않습니다."
)
