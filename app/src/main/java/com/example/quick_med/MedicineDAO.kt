package com.example.quick_med

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

class MedicineDAO(context: Context) {

    private val dbHelper = MedicineDatabaseHelper(context)
    private val database = dbHelper.writableDatabase

    fun addMedicine(medicine: Medicine) {
        val values = ContentValues().apply {
            put(MedicineDatabaseHelper.COLUMN_NAME, medicine.name)
            put(MedicineDatabaseHelper.COLUMN_DESCRIPTION, medicine.description)
            put(MedicineDatabaseHelper.COLUMN_IMAGE_URL, medicine.imageUrl)
            put(MedicineDatabaseHelper.COLUMN_DOSAGE, medicine.dosage)
            put(MedicineDatabaseHelper.COLUMN_SIDE_EFFECTS, medicine.sideEffects)
        }
        database.insert(MedicineDatabaseHelper.TABLE_MEDICINE, null, values)
    }

    fun getAllMedicines(): List<Medicine> {
        val medicines = mutableListOf<Medicine>()
        val db = dbHelper.readableDatabase
        val cursor = db.query("medicines", null, null, null, null, null, null)
        with(cursor) {
            while (moveToNext()) {
                val name = getString(getColumnIndexOrThrow("name"))
                val description = getString(getColumnIndexOrThrow("description"))
                val imageUrl = getString(getColumnIndexOrThrow("imageUrl"))
                val dosage = getString(getColumnIndexOrThrow("dosage"))
                val sideEffects = getString(getColumnIndexOrThrow("sideEffects"))
                medicines.add(Medicine(name, description, imageUrl, dosage, sideEffects))
            }
        }
        cursor.close()
        return medicines
    }

    fun getMedicineByName(name: String): Medicine? {
        val cursor: Cursor = database.query(
            MedicineDatabaseHelper.TABLE_MEDICINE,
            null,
            "${MedicineDatabaseHelper.COLUMN_NAME} = ?",
            arrayOf(name),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val description = cursor.getString(cursor.getColumnIndexOrThrow(MedicineDatabaseHelper.COLUMN_DESCRIPTION))
            val imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(MedicineDatabaseHelper.COLUMN_IMAGE_URL))
            val dosage = cursor.getString(cursor.getColumnIndexOrThrow(MedicineDatabaseHelper.COLUMN_DOSAGE))
            val sideEffects = cursor.getString(cursor.getColumnIndexOrThrow(MedicineDatabaseHelper.COLUMN_SIDE_EFFECTS))
            Medicine(name, description, imageUrl, dosage, sideEffects)
        } else {
            null
        }.also {
            cursor.close()
        }
    }

    fun deleteMedicineByName(name: String) {
        database.delete(MedicineDatabaseHelper.TABLE_MEDICINE, "${MedicineDatabaseHelper.COLUMN_NAME} = ?", arrayOf(name))
    }

    fun isMedicineExist(name: String): Boolean {
        val cursor: Cursor = database.query(
            MedicineDatabaseHelper.TABLE_MEDICINE,
            arrayOf(MedicineDatabaseHelper.COLUMN_NAME),
            "${MedicineDatabaseHelper.COLUMN_NAME} = ?",
            arrayOf(name),
            null, null, null
        )

        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

}
