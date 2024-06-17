package com.example.quick_med

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class Med_Quick : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val sharedPreferences: SharedPreferences = getSharedPreferences("QuickMedPrefs", MODE_PRIVATE)
        val isTermsAccepted = sharedPreferences.getBoolean("isTermsAccepted", false)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = if (isTermsAccepted) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, Intro2::class.java)
            }
            startActivity(intent)
            finish()
        }, 3000)
    }
}
