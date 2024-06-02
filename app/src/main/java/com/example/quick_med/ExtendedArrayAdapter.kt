package com.example.quick_med

import android.content.Context
import android.widget.ArrayAdapter

class ExtendedArrayAdapter<T>(context: Context, resource: Int, objects: MutableList<T>) : ArrayAdapter<T>(context, resource, objects) {

    fun getItems(): List<T> {
        val items = mutableListOf<T>()
        for (i in 0 until count) {
            items.add(getItem(i)!!)
        }
        return items
    }
}
