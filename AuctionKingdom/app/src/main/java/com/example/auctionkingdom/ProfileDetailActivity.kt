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
    private lateinit var email: String
    private lateinit var nicknameTextView: TextView
    private lateinit var kingdomNameTextView: TextView
    private lateinit var tierFlagImageView: ImageView
    private lateinit var gameCountTextView: TextView
    private lateinit var winRateTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var profileImageView: ImageView
    private var profileImageName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_detail)

        email = intent.getStringExtra("email") ?: ""
        nicknameTextView = findViewById(R.id.nickname)
        kingdomNameTextView = findViewById(R.id.kingdom_name)
        tierFlagImageView = findViewById(R.id.tier_flag)
        gameCountTextView = findViewById(R.id.game_count)
        winRateTextView = findViewById(R.id.win_rate)
        scoreTextView = findViewById(R.id.score)
        profileImageView = findViewById(R.id.profile_image)
        val editIcon: ImageView = findViewById(R.id.edit_icon)
        val backButton: ImageView = findViewById(R.id.back_button)

        // 사용자 데이터 불러오기
        fetchUserData(email)

        // 편집 아이콘 클릭 이벤트
        editIcon.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("email", email)
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE)
        }

        // 뒤로 가기 버튼 클릭 이벤트
        backButton.setOnClickListener {
            setResultAndFinish()
        }
    }

    private fun fetchUserData(email: String) {
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
                        profileImageName = jsonResponse.getString("profileImage")
                        val resId = resources.getIdentifier(profileImageName, "drawable", packageName)
                        profileImageView.setImageResource(resId)

                        val totalGames = wins + draws + losses
                        val winRate = if (totalGames > 0) BigDecimal(wins.toDouble() / totalGames * 100).setScale(2, RoundingMode.HALF_UP).toDouble() else 0.0

                        nicknameTextView.text = nickname
                        kingdomNameTextView.text = kingdomName
                        gameCountTextView.text = "${wins}승 ${draws}무 ${losses}패"
                        winRateTextView.text = "$winRate%"
                        scoreTextView.text = "$score"

                        // 티어 깃발 이미지 설정
                        tierFlagImageView.setImageResource(getTierFlagResource(score))

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            fetchUserData(email)
        }
    }

    private fun setResultAndFinish() {
        val intent = Intent().apply {
            putExtra("nickname", nicknameTextView.text.toString())
            putExtra("kingdomName", kingdomNameTextView.text.toString())
            putExtra("profileImage", profileImageName)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        private const val EDIT_PROFILE_REQUEST_CODE = 1001
    }
}
