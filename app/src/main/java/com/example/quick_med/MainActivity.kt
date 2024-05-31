package com.example.quick_med

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var placeholder: TextView
    private lateinit var medicineDAO: MedicineDAO
    private lateinit var interactionAdapter: ExtendedArrayAdapter<String>
    private lateinit var interactionListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonAlarm = findViewById<Button>(R.id.button_alarm)
        buttonAlarm.setOnClickListener {
            val intent = Intent(this, Alarm::class.java)
            startActivity(intent)
        }

        val buttonCalendar = findViewById<Button>(R.id.button_calendar)
        buttonCalendar.setOnClickListener {
            val intent = Intent(this, Calendar::class.java)
            startActivity(intent)
        }

        val buttonMyMed = findViewById<Button>(R.id.button_my_med)
        buttonMyMed.setOnClickListener {
            val intent = Intent(this, My_Med::class.java)
            startActivity(intent)
        }

        val buttonSearchMed = findViewById<Button>(R.id.button_search_med)
        buttonSearchMed.setOnClickListener {
            val intent = Intent(this, Search_Med::class.java)
            startActivity(intent)
        }

        placeholder = findViewById(R.id.placeholder)

        // Initialize ListView and Adapter
        val medicineListView = findViewById<ListView>(R.id.medicineListView)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        medicineListView.adapter = adapter

        // Initialize interaction ListView and Adapter
        interactionListView = findViewById(R.id.interactionListView)
        interactionAdapter = ExtendedArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        interactionListView.adapter = interactionAdapter

        // Initialize MedicineDAO
        medicineDAO = MedicineDAO(this)

        // Load data when the activity is created
        loadSavedMedicines()

        medicineListView.setOnItemClickListener { parent, view, position, id ->
            val medicineName = parent.getItemAtPosition(position) as String
            showDeleteConfirmationDialog(medicineName, position)
        }

        // Check drug interactions initially
        checkDrugInteractions()
    }

    override fun onResume() {
        super.onResume()
        // Reload data when the activity is resumed
        loadSavedMedicines()
        checkDrugInteractions()
    }

    private fun loadSavedMedicines() {
        val medicines = medicineDAO.getAllMedicines().map { it.name }

        // Clear and repopulate the adapter
        adapter.clear()
        adapter.addAll(medicines)
        adapter.notifyDataSetChanged()

        // Show placeholder if the list is empty
        updatePlaceholderVisibility()
    }

    private fun showDeleteConfirmationDialog(medicineName: String, position: Int) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("약 $medicineName 을(를) 삭제하시겠습니까?")
            .setCancelable(false)
            .setPositiveButton("예") { dialog, id -> deleteMedicine(medicineName, position) }
            .setNegativeButton("아니요") { dialog, id -> dialog.cancel() }
        val alert = dialogBuilder.create()
        alert.setTitle("약 삭제")
        alert.show()
    }

    private fun deleteMedicine(medicineName: String, position: Int) {
        medicineDAO.deleteMedicineByName(medicineName)

        // Remove the item from the adapter
        adapter.remove(adapter.getItem(position))
        adapter.notifyDataSetChanged()

        // Show a toast message confirming the deletion
        Toast.makeText(this, "삭제 되었습니다.", Toast.LENGTH_SHORT).show()

        // Update placeholder visibility
        updatePlaceholderVisibility()

        // Check drug interactions again after deletion
        checkDrugInteractions()
    }

    private fun updatePlaceholderVisibility() {
        if (adapter.isEmpty) {
            placeholder.visibility = TextView.VISIBLE
        } else {
            placeholder.visibility = TextView.GONE
        }
    }

    private fun checkDrugInteractions() {
        interactionAdapter.clear()
        val medicines = medicineDAO.getAllMedicines()

        // 중복 확인을 위한 HashSet 생성
        val interactionSet = mutableSetOf<Pair<String, String>>()

        val checker = DrugInteractionChecker(this)
        checker.checkInteractions { interactions, noInteractionMeds ->
            interactions.forEach { interaction ->
                val sortedPair = if (interaction.medicine1 < interaction.medicine2) {
                    Pair(interaction.medicine1, interaction.medicine2)
                } else {
                    Pair(interaction.medicine2, interaction.medicine1)
                }

                if (!interactionSet.contains(sortedPair)) {
                    interactionAdapter.add("${interaction.medicine1}과(와) ${interaction.medicine2}은 같이 드시면 안됩니다. 사유: ${interaction.reason}")
                    interactionSet.add(sortedPair)
                }
            }

            if (interactions.isEmpty()) {
                interactionAdapter.add("문제 없음")
            }

            // 중복된 항목 제거
            val uniqueList = interactionAdapter.getItems().distinct()
            interactionAdapter.clear()
            interactionAdapter.addAll(uniqueList)

            interactionAdapter.notifyDataSetChanged()
        }
    }
}
