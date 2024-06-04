package com.example.quick_med

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.Switch
import android.widget.TextView

class MedicineCalendarAdapter(private val context: Context, private var medicines: MutableList<MedicineCalendar>) : BaseAdapter() {

    override fun getCount(): Int = medicines.size

    override fun getItem(position: Int): MedicineCalendar = medicines[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view =
                LayoutInflater.from(context).inflate(R.layout.item_calendar_medicine, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val medicine = getItem(position)
        viewHolder.tvMedicine.text = medicine.name
        viewHolder.switchMedicine.isChecked = medicine.isChecked
        viewHolder.switchMedicine.setOnCheckedChangeListener { _, isChecked ->
            medicine.isChecked = isChecked
        }



        viewHolder.btnDelete.setOnClickListener {
            medicines.removeAt(position)
            notifyDataSetChanged()
        }


        return view
    }

    fun getMedicines(): MutableList<MedicineCalendar> = medicines

    fun updateMedicines(newMedicines: List<MedicineCalendar>) {
        medicines = newMedicines.toMutableList()
        notifyDataSetChanged()
    }

    private class ViewHolder(view: View) {
        val tvMedicine: TextView = view.findViewById(R.id.tvMedicine)
        val switchMedicine: Switch = view.findViewById(R.id.switchMedicine)
        val btnDelete: Button =
            view.findViewById(R.id.btnDelete)
    }
}