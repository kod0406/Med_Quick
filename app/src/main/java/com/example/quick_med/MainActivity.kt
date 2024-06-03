package com.example.quick_med

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var placeholder: TextView
    private lateinit var placeholder2: TextView
    private lateinit var medicineDAO: MedicineDAO
    private lateinit var interactionAdapter: ArrayAdapter<String>
    private lateinit var interactionListView: ListView
    private lateinit var dateTextView: TextView
    private lateinit var loadingTextView: TextView
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        val buttonAlarm = findViewById<Button>(R.id.button_alarm)
        val buttonCalendar = findViewById<Button>(R.id.button_calendar)
        val buttonMyMed = findViewById<Button>(R.id.button_my_med)
        val buttonSearchMed = findViewById<Button>(R.id.button_search_med)
        val buttonMoreInfo = findViewById<Button>(R.id.button_more_info)
        dateTextView = findViewById(R.id.dateTextView)
        placeholder = findViewById(R.id.placeholder)
        placeholder2 = findViewById(R.id.placeholder2)
        val titleMedicineList = findViewById<TextView>(R.id.title_medicine_list)
        val titleInteractionList = findViewById<TextView>(R.id.title_interaction_list)
        val medicineListView = findViewById<ListView>(R.id.medicineListView)
        interactionListView = findViewById(R.id.interactionListView)
        loadingTextView = findViewById(R.id.loadingTextView)

        // Set up buttons
        buttonAlarm.setOnClickListener {
            val intent = Intent(this, Alarm::class.java)
            startActivity(intent)
        }

        buttonCalendar.setOnClickListener {
            val intent = Intent(this, Calendar::class.java)
            startActivity(intent)
        }

        buttonMyMed.setOnClickListener {
            val intent = Intent(this, My_Med::class.java)
            startActivity(intent)
        }

        buttonSearchMed.setOnClickListener {
            val intent = Intent(this, Search_Med::class.java)
            startActivity(intent)
        }

        // Set up more info button
        buttonMoreInfo.setOnClickListener {
            showInteractionPopup()
        }

        // Initialize ListView and Adapter
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        medicineListView.adapter = adapter

        // Initialize interaction ListView and Adapter for simple info
        interactionAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
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

        // Schedule checkDrugInteractions to run after 3 seconds
        handler.postDelayed({ checkDrugInteractions() }, 3000)
    }

    override fun onResume() {
        super.onResume()
        // Reload data when the activity is resumed
        loadSavedMedicines()
        checkDrugInteractions()
        updateDateTime()

        // Show loading text and hide interaction list initially
        interactionListView.visibility = ListView.GONE
        loadingTextView.visibility = TextView.VISIBLE
        placeholder2.visibility = TextView.GONE

        // Schedule interactionListView and placeholder2 to be shown after 5 seconds
        handler.postDelayed({
            interactionListView.visibility = ListView.VISIBLE
            loadingTextView.visibility = TextView.GONE
            placeholder2.visibility = TextView.VISIBLE
        }, 5000)
    }

    private fun updateDateTime() {
        val dateFormat = SimpleDateFormat("M월 d일 E요일", Locale.KOREA)
        val currentDate = dateFormat.format(Date())
        dateTextView.text = currentDate
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
            placeholder2.visibility = TextView.VISIBLE
        } else {
            placeholder.visibility = TextView.GONE
            placeholder2.visibility = TextView.GONE
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
                    interactionAdapter.add("${interaction.medicine1}과(와)\n${interaction.medicine2}")
                    interactionSet.add(sortedPair)
                }
            }

            if (interactions.isEmpty()) {
                interactionAdapter.add("병용시 문제되는 약품이 없습니다.")
            }

            interactionAdapter.notifyDataSetChanged()
        }
    }

    private fun showInteractionPopup() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("병용금기 약물")

        // Inflate and set the custom view
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.dialog_interaction_list, null)

        val listView = popupView.findViewById<ListView>(R.id.popupListView)
        val popupAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        listView.adapter = popupAdapter

        // Populate popup list with detailed info
        val checker = DrugInteractionChecker(this)
        checker.checkInteractions { interactions, noInteractionMeds ->
            interactions.forEach { interaction ->
                popupAdapter.add("${interaction.medicine1}과(와) ${interaction.medicine2}\n은 같이 드시면 안됩니다.\n사유: ${interaction.reason} 발생 위험이 증가합니다.")
            }

            if (interactions.isEmpty()) {
                popupAdapter.add("병용시 문제되는 약품이 없습니다.")
            }

            popupAdapter.notifyDataSetChanged()
        }

        builder.setView(popupView)
        builder.setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }
}
