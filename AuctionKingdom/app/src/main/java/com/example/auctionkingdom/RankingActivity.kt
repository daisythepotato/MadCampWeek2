package com.example.auctionkingdom

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class RankingActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        val backButton: ImageView = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.ranking_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchRankingData { rankingData ->
            // 점수 순서로 정렬
            val sortedRankingData = rankingData.sortedByDescending { it.score }
            runOnUiThread {
                Log.d("RankingActivity", "Setting adapter with data: $sortedRankingData")
                if (sortedRankingData.isNotEmpty()) {
                    recyclerView.adapter = RankingAdapter(sortedRankingData)
                } else {
                    Log.e("RankingActivity", "No data to display")
                }
            }
        }
    }

    private fun fetchRankingData(callback: (List<RankingItem>) -> Unit) {
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/ranking") // 여기에 실제 API 엔드포인트를 넣으세요.
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Log.e("RankingActivity", "Network request failed")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseData ->
                    Log.d("RankingActivity", "Response Data: $responseData")
                    val rankingData = parseRankingData(responseData)
                    callback(rankingData)
                }
            }
        })
    }

    private fun parseRankingData(responseData: String): List<RankingItem> {
        val rankingData = mutableListOf<RankingItem>()
        try {
            val jsonArray = JSONArray(responseData)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val profileImage = jsonObject.getString("profileImage") // 문자열로 저장된 이미지 이름
                val nickname = jsonObject.getString("nickname")
                val kingdomName = jsonObject.getString("kingdomName")
                val score = jsonObject.getInt("score")
                rankingData.add(RankingItem(profileImage, nickname, kingdomName, score))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RankingActivity", "Error parsing ranking data", e)
        }
        return rankingData
    }
}