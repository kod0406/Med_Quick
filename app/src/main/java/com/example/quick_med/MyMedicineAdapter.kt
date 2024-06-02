package com.example.quick_med

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

class MyMedicineAdapter(private val context: Context, private val dataSource: List<Medicine>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = convertView ?: inflater.inflate(R.layout.list_item_my_medicine, parent, false)
        val nameTextView = rowView.findViewById<TextView>(R.id.medicineName)
        val effectTextView = rowView.findViewById<TextView>(R.id.effectInfo)
        val dosageTextView = rowView.findViewById<TextView>(R.id.dosageInfo)
        val sideEffectsTextView = rowView.findViewById<TextView>(R.id.sideEffectsInfo)
        val imageView = rowView.findViewById<ImageView>(R.id.medicineImage)

        val medicine = getItem(position) as Medicine
        nameTextView.text = medicine.name
        effectTextView.text = if (medicine.description.isNullOrEmpty()) "해당 API에서는 정보를 제공하지 않습니다." else medicine.description
        dosageTextView.text = if (medicine.dosage.isNullOrEmpty()) "해당 API에서는 정보를 제공하지 않습니다." else medicine.dosage
        sideEffectsTextView.text = if (medicine.sideEffects.isNullOrEmpty()) "해당 API에서는 정보를 제공하지 않습니다." else medicine.sideEffects

        if (medicine.imageUrl != null) {
            Picasso.get().load(medicine.imageUrl).placeholder(R.drawable.placeholder).into(imageView)
        } else {
            imageView.setImageResource(R.drawable.placeholder)
        }

        return rowView
    }
}
