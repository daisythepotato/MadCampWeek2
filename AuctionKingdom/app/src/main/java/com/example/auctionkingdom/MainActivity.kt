package com.example.auctionkingdom

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
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

        // 상태 바와 네비게이션 바를 투명하게 설정
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // 상태 바와 네비게이션 바의 색상을 투명하게 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }

        val email = intent.getStringExtra("email")
        val profileNameTextView: TextView = findViewById(R.id.profile_name)
        val kingdomNameTextView: TextView = findViewById(R.id.kingdom_name)
        val scoreTextView: TextView = findViewById(R.id.score_text)
        val coinTextView: TextView = findViewById(R.id.coin_text) // 추가된 부분

        // 프로필 사진 설정
        val profileImageView: ImageView = findViewById(R.id.profile_image)
        profileImageView.setImageResource(R.drawable.profile_placeholder) // 실제 이미지를 사용하려면 이 부분을 변경합니다.

        // 사용자 데이터 불러오기
        fetchUserData(email, profileNameTextView, kingdomNameTextView, scoreTextView, coinTextView)

        // 방 생성 및 입장 화면으로 이동
        val swordsIcon: ImageView = findViewById(R.id.nav_swords)
        swordsIcon.setOnClickListener {
            val intent = Intent(this, RoomActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchUserData(email: String?, profileNameTextView: TextView, kingdomNameTextView: TextView, scoreTextView: TextView, coinTextView: TextView) {
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
                    coinTextView.text = "" // 추가된 부분
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
                        val coins = jsonResponse.getInt("coins") // 추가된 부분

                        profileNameTextView.text = nickname
                        kingdomNameTextView.text = kingdomName
                        scoreTextView.text = "Score: $score"
                        coinTextView.text = coins.toString() // 추가된 부분
                    } catch (e: Exception) {
                        profileNameTextView.text = "Failed to parse data"
                        kingdomNameTextView.text = ""
                        scoreTextView.text = ""
                        coinTextView.text = "" // 추가된 부분
                    }
                }
            }
        })
    }
}
