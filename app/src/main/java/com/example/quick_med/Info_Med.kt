package com.example.quick_med

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso

class Info_Med : AppCompatActivity() {

    private lateinit var medicineDAO: MedicineDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infomed)

        medicineDAO = MedicineDAO(this)

        val name = intent.getStringExtra("name")
        val imageUrl = intent.getStringExtra("imageUrl")
        val effect = intent.getStringExtra("effect")
        val dosage = intent.getStringExtra("dosage")
        val sideEffects = intent.getStringExtra("sideEffects")

        val medicineNameTextView: TextView = findViewById(R.id.medicineName)
        val medicineImageView: ImageView = findViewById(R.id.medicineImage)
        val effectTextView: TextView = findViewById(R.id.effectInfo)
        val dosageTextView: TextView = findViewById(R.id.dosageInfo)
        val sideEffectsTextView: TextView = findViewById(R.id.sideEffectsInfo)

        medicineNameTextView.text = name
        effectTextView.text = effect?.takeIf { it.isNotEmpty() } ?: "해당 API에서는 효과 정보를 제공하지 않습니다."
        dosageTextView.text = dosage?.takeIf { it.isNotEmpty() } ?: "해당 API에서는 복용 방법 정보를 제공하지 않습니다."
        sideEffectsTextView.text = sideEffects?.takeIf { it.isNotEmpty() && it != "null" } ?: "해당 API에서는 부작용 정보를 제공하지 않습니다."

        if (imageUrl != null && imageUrl.isNotEmpty()) {
            Picasso.get().load(imageUrl).placeholder(R.drawable.placeholder_image).into(medicineImageView)
        } else {
            medicineImageView.setImageResource(R.drawable.placeholder_image)
        }

        val yesButton: Button = findViewById(R.id.yesButton)
        yesButton.setOnClickListener {
            if (name != null && !medicineDAO.isMedicineExist(name)) {
                val medicine = Medicine(name, effect ?: "효과 정보가 없습니다.", imageUrl, dosage ?: "복용 방법 정보가 없습니다.", sideEffects ?: "부작용 정보가 없습니다.")
                medicineDAO.addMedicine(medicine)
                Toast.makeText(this, "저장되었습니다", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@Info_Med, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "이미 저장된 약입니다", Toast.LENGTH_SHORT).show()
            }
        }

        val noButton: Button = findViewById(R.id.noButton)
        noButton.setOnClickListener {
            val intent = Intent(this@Info_Med, Search_Med::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
