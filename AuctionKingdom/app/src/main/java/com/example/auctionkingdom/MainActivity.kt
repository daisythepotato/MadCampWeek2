package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val email = intent.getStringExtra("email")
        val profileNameTextView: TextView = findViewById(R.id.profile_name)
        val kingdomNameTextView: TextView = findViewById(R.id.kingdom_name)
        val scoreTextView: TextView = findViewById(R.id.score_text)
        val coinTextView: TextView = findViewById(R.id.coin_text)
        val profileImageView: ImageView = findViewById(R.id.profile_image) // 추가된 부분

        // 사용자 데이터 불러오기
        fetchUserData(email, profileNameTextView, kingdomNameTextView, scoreTextView, coinTextView, profileImageView)

        // 방 생성 및 입장 화면으로 이동
        val swordsIcon: ImageView = findViewById(R.id.nav_swords)
        swordsIcon.setOnClickListener {
            val intent = Intent(this, RoomActivity::class.java)
            intent.putExtra("email", email) // 이메일 전달
            startActivity(intent)
        }
    }

    private fun fetchUserData(email: String?, profileNameTextView: TextView, kingdomNameTextView: TextView, scoreTextView: TextView, coinTextView: TextView, profileImageView: ImageView) {
        if (email == null) {
            return
        }

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/getUser?email=$email")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // 실패 시 처리
                    profileNameTextView.text = "Failed to load data"
                    kingdomNameTextView.text = ""
                    scoreTextView.text = ""
                    coinTextView.text = ""
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val nickname = jsonResponse.getString("nickname")
                        val kingdomName = jsonResponse.getString("kingdomName")
                        val score = jsonResponse.getInt("score")
                        val coins = jsonResponse.getInt("coins")
                        // profileImage 필드를 사용하는 경우 추가 필요
                        // val profileImageRes = jsonResponse.getInt("profileImage")

                        profileNameTextView.text = nickname
                        kingdomNameTextView.text = kingdomName
                        scoreTextView.text = "Score: $score"
                        coinTextView.text = coins.toString()
                        // profileImageView.setImageResource(profileImageRes) // 추가된 부분
                    } catch (e: Exception) {
                        profileNameTextView.text = "Failed to parse data"
                        kingdomNameTextView.text = ""
                        scoreTextView.text = ""
                        coinTextView.text = ""
                    }
                }
            }
        })
    }
}
