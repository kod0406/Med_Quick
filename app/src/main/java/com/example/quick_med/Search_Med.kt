package com.example.quick_med

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import android.widget.Toast

class Search_Med : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var listView: ListView
    private val serviceKey = "zp%2FXmsF6TzhsNiU1jUF2ElWrarTPBUzV7ccDYcc8jPtbcz3%2BkkzF9ZG%2BegIM2ib7CgLvq1LEZF%2FrG0MH1gDqLw%3D%3D"
    private lateinit var medicineDAO: MedicineDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchmed)

        medicineDAO = MedicineDAO(this)
        searchView = findViewById(R.id.search_view)
        listView = findViewById(R.id.list_view)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchMedicines(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun searchMedicines(query: String) {
        val medicines = mutableListOf<Medicine>()
        var primaryApiCompleted = false
        var additionalApiCompleted = false

        fun checkAndUpdateUI() {
            if (primaryApiCompleted && additionalApiCompleted) {
                runOnUiThread {
                    if (medicines.isNotEmpty()) {
                        listView.adapter = MedicineAdapter(this@Search_Med, medicines)
                        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                            val selectedMedicine = medicines[position]
                            val intent = Intent(this@Search_Med, Info_Med::class.java).apply {
                                putExtra("name", selectedMedicine.name)
                                putExtra("imageUrl", selectedMedicine.imageUrl)
                                putExtra("effect", selectedMedicine.description)
                                putExtra("dosage", selectedMedicine.dosage)
                                putExtra("sideEffects", selectedMedicine.sideEffects)
                            }
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this@Search_Med, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        searchMedicinesFromPrimaryAPI(query, medicines) {
            primaryApiCompleted = true
            checkAndUpdateUI()
        }
        searchMedicinesFromAdditionalAPI(query, medicines) {
            additionalApiCompleted = true
            checkAndUpdateUI()
        }
    }

    private fun searchMedicinesFromPrimaryAPI(query: String, medicines: MutableList<Medicine>, onComplete: () -> Unit) {
        thread {
            try {
                val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
                val url = URL("https://apis.data.go.kr/1471000/DrbEasyDrugInfoService/getDrbEasyDrugList?serviceKey=$serviceKey&pageNo=1&numOfRows=100&itemName=$encodedQuery&type=json")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                // 로그 추가
                println("Primary API Response: $response")

                val jsonObject = JSONObject(response)
                val body = jsonObject.getJSONObject("body")
                val items = body.getJSONArray("items")

                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val name = item.getString("itemName")
                    val description = item.getString("efcyQesitm")
                    val imageUrl = item.optString("itemImage", null)
                    val dosage = item.optString("useMethodQesitm", "해당 API에서는 정보를 제공하지 않습니다.")
                    val sideEffects = item.optString("seQesitm", "해당 API에서는 정보를 제공하지 않습니다.")
                    medicines.add(Medicine(name, description, imageUrl, dosage, sideEffects))
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                }
            } finally {
                onComplete()
            }
        }
    }

    private fun searchMedicinesFromAdditionalAPI(query: String, medicines: MutableList<Medicine>, onComplete: () -> Unit) {
        thread {
            try {
                val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
                val url = URL("https://apis.data.go.kr/1471000/MdcinGrnIdntfcInfoService01/getMdcinGrnIdntfcInfoList01?serviceKey=$serviceKey&item_name=$encodedQuery&pageNo=1&numOfRows=99&type=json")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                // 로그 추가
                println("Additional API Response: $response")

                val jsonObject = JSONObject(response)
                val body = jsonObject.getJSONObject("body")
                val items = body.getJSONArray("items")

                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val name = item.getString("ITEM_NAME")
                    val description = item.optString("CHART", "정보 없음")
                    val imageUrl = item.optString("ITEM_IMAGE", null)
                    val dosage = "해당 API에서는 정보를 제공하지 않습니다." // 이 API에서는 복용 방법 정보를 제공하지 않음
                    val sideEffects = "해당 API에서는 정보를 제공하지 않습니다." // 이 API에서는 부작용 정보를 제공하지 않음
                    medicines.add(Medicine(name, description, imageUrl, dosage, sideEffects))
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                }
            } finally {
                onComplete()
            }
        }
    }
}
