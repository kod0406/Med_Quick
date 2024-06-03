package com.example.quick_med

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.prolificinteractive.materialcalendarview.CalendarDay

class MedicineDataStorage(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("MedicineData", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveMedicineData(date: String, medicines: List<MedicineCalendar>) {
        val editor = sharedPreferences.edit()
        val json = gson.toJson(medicines)
        editor.putString(date, json)
        editor.apply()
    }

    fun loadMedicineData(date: String): MutableList<MedicineCalendar> {
        val json = sharedPreferences.getString(date, null)
        val type = object : TypeToken<MutableList<MedicineCalendar>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    fun loadAllMedicineData(): Map<CalendarDay, MutableList<MedicineCalendar>> {
        val allMedicineData = mutableMapOf<CalendarDay, MutableList<MedicineCalendar>>()
        for (dateString in sharedPreferences.all.keys) {
            val dateParts = dateString
                .removePrefix("CalendarDay{")
                .removeSuffix("}")
                .split("-")
                .map { it.toInt() }
            val date = CalendarDay.from(dateParts[0], dateParts[1], dateParts[2])
            val medicinesForDate = loadMedicineData(dateString)
            allMedicineData[date] = medicinesForDate.toMutableList()
        }
        return allMedicineData
    }
}
