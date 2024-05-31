package com.example.quick_med

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class DrugInteractionChecker(private val context: Context) {

    private val serviceKey = "zp%2FXmsF6TzhsNiU1jUF2ElWrarTPBUzV7ccDYcc8jPtbcz3%2BkkzF9ZG%2BegIM2ib7CgLvq1LEZF%2FrG0MH1gDqLw%3D%3D"

    fun checkInteractions(callback: (List<DrugInteraction>, List<String>) -> Unit) {
        val medicineDAO = MedicineDAO(context)
        val medicines = medicineDAO.getAllMedicines()
        val interactions = mutableSetOf<DrugInteraction>()
        val noInteractionMeds = mutableListOf<String>()
        var checkedMedicines = 0

        medicines.forEach { medicine ->
            val encodedQuery = java.net.URLEncoder.encode(medicine.name, "UTF-8")
            val url = URL("https://apis.data.go.kr/1471000/DURPrdlstInfoService03/getUsjntTabooInfoList03?serviceKey=$serviceKey&pageNo=1&numOfRows=99&type=json&itemName=$encodedQuery")

            thread {
                try {
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val body = jsonObject.getJSONObject("body")
                    val items = body.optJSONArray("items")

                    if (items != null) {
                        var hasInteraction = false
                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)
                            val mixtureItemName = item.getString("MIXTURE_ITEM_NAME")
                            val prohbtContent = item.getString("PROHBT_CONTENT")

                            medicines.forEach { storedMedicine ->
                                if (storedMedicine.name == mixtureItemName && medicine.name != storedMedicine.name) {
                                    val sortedPair = if (medicine.name < storedMedicine.name) {
                                        Pair(medicine.name, storedMedicine.name)
                                    } else {
                                        Pair(storedMedicine.name, medicine.name)
                                    }
                                    val interaction = DrugInteraction(sortedPair.first, sortedPair.second, prohbtContent)
                                    interactions.add(interaction)
                                    hasInteraction = true
                                }
                            }
                        }
                        if (!hasInteraction) {
                            noInteractionMeds.add(medicine.name)
                        }
                    } else {
                        noInteractionMeds.add(medicine.name)
                    }

                    synchronized(this) {
                        checkedMedicines++
                        if (checkedMedicines == medicines.size) {
                            (context as MainActivity).runOnUiThread {
                                callback(interactions.toList(), noInteractionMeds)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    synchronized(this) {
                        checkedMedicines++
                        if (checkedMedicines == medicines.size) {
                            (context as MainActivity).runOnUiThread {
                                callback(interactions.toList(), noInteractionMeds)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class DrugInteraction(val medicine1: String, val medicine2: String, val reason: String)
