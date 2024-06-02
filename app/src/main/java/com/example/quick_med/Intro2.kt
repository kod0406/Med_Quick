package com.example.quick_med

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Intro2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro2)

        val checkBox = findViewById<CheckBox>(R.id.checkBoxAgree)
        val buttonAgree = findViewById<Button>(R.id.buttonAgree)

        buttonAgree.setOnClickListener {
            if (checkBox.isChecked) {
                val sharedPreferences: SharedPreferences = getSharedPreferences("QuickMedPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("isTermsAccepted", true)
                editor.apply()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // 약관 동의 체크박스를 체크하지 않은 경우의 처리
                Toast.makeText(this, "약관에 동의해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
