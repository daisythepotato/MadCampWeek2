package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode

class ProfileDetailActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_detail)

        val email = intent.getStringExtra("email")
        val nicknameTextView: TextView = findViewById(R.id.nickname)
        val kingdomNameTextView: TextView = findViewById(R.id.kingdom_name)
        val tierFlagImageView: ImageView = findViewById(R.id.tier_flag)
        val gameCountTextView: TextView = findViewById(R.id.game_count)
        val winRateTextView: TextView = findViewById(R.id.win_rate)
        val scoreTextView: TextView = findViewById(R.id.score)
        val profileImageView: ImageView = findViewById(R.id.profile_image)
        val editIcon: ImageView = findViewById(R.id.edit_icon)
        val backButton: ImageView = findViewById(R.id.back_button)

        // 사용자 데이터 불러오기
        fetchUserData(email, nicknameTextView, kingdomNameTextView, tierFlagImageView, gameCountTextView, winRateTextView, scoreTextView, profileImageView)

        // 편집 아이콘 클릭 이벤트
        editIcon.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

        // 뒤로 가기 버튼 클릭 이벤트
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchUserData(email: String?, nicknameTextView: TextView, kingdomNameTextView: TextView, tierFlagImageView: ImageView, gameCountTextView: TextView, winRateTextView: TextView, scoreTextView: TextView, profileImageView: ImageView) {
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
                    nicknameTextView.text = "Failed to load data"
                    kingdomNameTextView.text = ""
                    gameCountTextView.text = ""
                    winRateTextView.text = ""
                    scoreTextView.text = ""
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val nickname = jsonResponse.getString("nickname")
                        val kingdomName = jsonResponse.getString("kingdomName")
                        val wins = jsonResponse.getInt("wins")
                        val draws = jsonResponse.getInt("draws")
                        val losses = jsonResponse.getInt("losses")
                        val score = jsonResponse.getInt("score")
                        val profileImageRes = jsonResponse.getInt("profileImage")

                        val totalGames = wins + draws + losses
                        val winRate = if (totalGames > 0) BigDecimal(wins.toDouble() / totalGames * 100).setScale(2, RoundingMode.HALF_UP).toDouble() else 0.0

                        nicknameTextView.text = nickname
                        kingdomNameTextView.text = kingdomName
                        gameCountTextView.text = "${wins}승 ${draws}무 ${losses}패"
                        winRateTextView.text = "$winRate%"
                        scoreTextView.text = "Score: $score"

                        // 티어 깃발 이미지 설정
                        tierFlagImageView.setImageResource(getTierFlagResource(score))
                        profileImageView.setImageResource(profileImageRes)
                    } catch (e: Exception) {
                        nicknameTextView.text = "Failed to parse data"
                        kingdomNameTextView.text = ""
                        gameCountTextView.text = ""
                        winRateTextView.text = ""
                        scoreTextView.text = ""
                    }
                }
            }
        })
    }

    private fun getTierFlagResource(score: Int): Int {
        return when {
            score < 1000 -> R.drawable.bronze_flag
            score < 2000 -> R.drawable.silver_flag
            else -> R.drawable.gold_flag
        }
    }
}
