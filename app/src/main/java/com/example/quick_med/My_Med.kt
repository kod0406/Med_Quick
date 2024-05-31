package com.example.quick_med

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class My_Med : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: MyMedicineAdapter
    private lateinit var medicineDAO: MedicineDAO
    private lateinit var emptyView: View
    private lateinit var buttonAddMedicine: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_med)

        listView = findViewById(R.id.list_view)
        emptyView = findViewById(R.id.empty_view)
        buttonAddMedicine = findViewById(R.id.button_add_medicine)
        medicineDAO = MedicineDAO(this)

        // Set empty view for the ListView
        listView.emptyView = emptyView

        // Set button click listener to navigate to Search_Med activity
        buttonAddMedicine.setOnClickListener {
            val intent = Intent(this, Search_Med::class.java)
            startActivity(intent)
        }

        loadSavedMedicines()
    }

    private fun loadSavedMedicines() {
        val savedMedicines = medicineDAO.getAllMedicines().map {
            Medicine(
                name = it.name,
                description = if (it.description.isNullOrEmpty()) "해당 API에서는 정보를 제공하지 않습니다." else it.description,
                imageUrl = it.imageUrl,
                dosage = if (it.dosage.isNullOrEmpty()) "해당 API에서는 정보를 제공하지 않습니다." else it.dosage,
                sideEffects = it.sideEffects?.takeIf { it.isNotEmpty() && it != "null" } ?: "해당 API에서는 정보를 제공하지 않습니다."
            )
        }
        adapter = MyMedicineAdapter(this, savedMedicines)
        listView.adapter = adapter
    }
}
