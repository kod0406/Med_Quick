package com.example.quick_med

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ListView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay

class Calendar : AppCompatActivity() {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var medicineListView: ListView
    private lateinit var btnAddMedicine: Button
    private lateinit var btnSave: Button
    private lateinit var medicineAdapter: MedicineCalendarAdapter
    private val medicineData = mutableMapOf<CalendarDay, MutableList<MedicineCalendar>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarView = findViewById(R.id.calendarView)
        medicineListView = findViewById(R.id.medicineListView)
        btnAddMedicine = findViewById(R.id.btnAddMedicine)
        btnSave = findViewById(R.id.btnSave)

        medicineAdapter = MedicineCalendarAdapter(this, mutableListOf())
        medicineListView.adapter = medicineAdapter

        btnAddMedicine.setOnClickListener {
            showAddMedicineDialog()
        }

        btnSave.setOnClickListener {
            saveMedicinesForDate()
        }

        calendarView.setOnDateChangedListener { _, date, _ ->
            loadMedicinesForDate(date)
        }

        medicineListView.setOnItemClickListener { _, _, position, _ ->
            showDeleteMedicineDialog(position)
        }

        // Initialize with selected date or today's date
        loadMedicinesForDate(calendarView.selectedDate ?: CalendarDay.today())

        val medicineDataStorage = MedicineDataStorage(this)
        medicineData.putAll(medicineDataStorage.loadAllMedicineData())
        updateCalendar()
    }

    private fun showAddMedicineDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Medicine")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("OK", null) // OnClickListener를 null로 설정합니다.

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        val dialog = builder.create()
        dialog.setOnShowListener {
            val button = (it as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val medicineName = input.text.toString()
                if (medicineName.isEmpty()) {
                    // 이름이 입력되지 않았을 경우, 메시지를 표시합니다.
                    Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show()
                } else {
                    // 이름이 입력되었을 경우, 약품을 추가하고 대화 상자를 닫습니다.
                    val newMedicine = MedicineCalendar(medicineName, false)
                    val updatedList = medicineAdapter.getMedicines().toMutableList()
                    updatedList.add(newMedicine)
                    medicineAdapter.updateMedicines(updatedList)
                    dialog.dismiss() // 'it' 대신 'dialog'를 사용하여 dismiss() 메소드를 호출합니다.
                }
            }
        }
        dialog.show()
    }

    private fun showDeleteMedicineDialog(position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Medicine")
        builder.setMessage("Are you sure you want to delete this medicine?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            val updatedList = medicineAdapter.getMedicines().toMutableList()
            updatedList.removeAt(position)
            medicineAdapter.updateMedicines(updatedList)
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun saveMedicinesForDate() {
        val selectedDate = calendarView.selectedDate ?: return
        val medicines = medicineAdapter.getMedicines()
        if (medicines.isEmpty()) {
            Toast.makeText(this, "저장된 약품이 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            val medicineDataStorage = MedicineDataStorage(this)
            medicineDataStorage.saveMedicineData(selectedDate.toString(), medicines)
            medicineData[selectedDate] = medicines.toMutableList() // medicineData 맵을 업데이트합니다.
            updateCalendar()
        }
    }

    private fun loadMedicinesForDate(date: CalendarDay) {
        val medicineDataStorage = MedicineDataStorage(this)
        val medicinesForDate = medicineDataStorage.loadMedicineData(date.toString())
        medicineAdapter.updateMedicines(medicinesForDate)
        updateCalendar() // 색상 정보를 업데이트합니다.
    }

    private fun updateCalendar() {
        calendarView.removeDecorators()
        medicineData.forEach { (date, medicines) ->
            if (medicines.isNotEmpty()) { // 약품 리스트가 비어 있지 않은 경우에만 색상을 업데이트합니다.
                val color = when (medicines.count { it.isChecked }) {
                    medicines.size -> android.graphics.Color.GREEN
                    in 1 until medicines.size -> android.graphics.Color.YELLOW
                    else -> android.graphics.Color.RED
                }
                calendarView.addDecorator(CustomCircleCalendarDecorator(date, color))
            }
        }
    }
}
