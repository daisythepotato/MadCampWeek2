package com.example.auctionkingdom

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var profileNameTextView: TextView
    private lateinit var kingdomNameTextView: TextView
    private lateinit var coinTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var exitButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val email = intent.getStringExtra("email")
        profileNameTextView = findViewById(R.id.profile_name)
        kingdomNameTextView = findViewById(R.id.kingdom_name)
        coinTextView = findViewById(R.id.coin_text)
        profileImageView = findViewById(R.id.profile_image)
        exitButton = findViewById(R.id.exit_button)

        // 사용자 데이터 불러오기
        fetchUserData(email)

        // 프로필 이미지 클릭 이벤트
        profileImageView.setOnClickListener {
            val intent = Intent(this, ProfileDetailActivity::class.java)
            intent.putExtra("email", email)
            startActivityForResult(intent, PROFILE_DETAIL_REQUEST_CODE)
        }

        // 방 생성 및 입장 화면으로 이동
        val swordsIcon: ImageView = findViewById(R.id.nav_swords)
        swordsIcon.setOnClickListener {
            val intent = Intent(this, RoomActivity::class.java)
            intent.putExtra("email", email) // 이메일 전달
            startActivity(intent)
        }

        val navShop = findViewById<ImageView>(R.id.nav_shop)
        val navTrophy = findViewById<ImageView>(R.id.nav_trophy)
        val navCards = findViewById<ImageView>(R.id.nav_cards)
        val navInfo = findViewById<ImageView>(R.id.nav_info)

        navShop.setOnClickListener {
            changeIconAndNavigate(navShop, R.drawable.shop_pop_icon, ShopActivity::class.java, email)
        }

        navTrophy.setOnClickListener {
            changeIconAndNavigate(navTrophy, R.drawable.rank_pop_icon, RankingActivity::class.java, email)
        }

        navCards.setOnClickListener {
            changeIconAndNavigate(navCards, R.drawable.library_pop_icon, LibraryActivity::class.java, email)
        }

        navInfo.setOnClickListener {
            changeIconAndNavigate(navInfo, R.drawable.info_pop_icon, InfoActivity::class.java, email)
        }

        // 종료 버튼 클릭 이벤트
        exitButton.setOnClickListener {
            email?.let {
                if (it.startsWith("AuctionKingdomGuest")) {
                    deleteGuestAccount(it)
                }
            }
            finishAndRemoveTask()
        }
    }

    private fun changeIconAndNavigate(icon: ImageView, newIconResId: Int, targetActivity: Class<*>, email: String?) {
        icon.setImageResource(newIconResId)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, targetActivity)
            intent.putExtra("email", email)
            startActivity(intent)
            // 여기서 원래 아이콘으로 복원
            icon.setImageResource(getOriginalIcon(icon.id))
        }, 300) // 0.3초 후 페이지 이동
    }

    private fun getOriginalIcon(id: Int): Int {
        return when (id) {
            R.id.nav_shop -> R.drawable.shop_icon
            R.id.nav_trophy -> R.drawable.trophy_icon
            R.id.nav_cards -> R.drawable.cards_icon
            R.id.nav_info -> R.drawable.info_icon
            else -> R.drawable.default_icon // 기본 아이콘 설정 (필요 시)
        }
    }

    private fun fetchUserData(email: String?) {
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
                        val coins = jsonResponse.getInt("coins")
                        val profileImageName = jsonResponse.getString("profileImage")

                        val resId = resources.getIdentifier(profileImageName, "drawable", packageName)
                        profileImageView.setImageResource(resId)
                        profileNameTextView.text = nickname
                        kingdomNameTextView.text = kingdomName
                        coinTextView.text = coins.toString()
                    } catch (e: Exception) {
                        profileNameTextView.text = "Failed to parse data"
                        kingdomNameTextView.text = ""
                        coinTextView.text = ""
                    }
                }
            }
        })
    }

    private fun deleteGuestAccount(email: String) {
        val json = JSONObject()
        json.put("email", email)

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/deleteUser")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 실패 처리
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // 성공 처리
                }
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PROFILE_DETAIL_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.let {
                val nickname = it.getStringExtra("nickname")
                val kingdomName = it.getStringExtra("kingdomName")
                val profileImage = it.getStringExtra("profileImage")

                val resId = resources.getIdentifier(profileImage, "drawable", packageName)
                profileImageView.setImageResource(resId)
                profileNameTextView.text = nickname
                kingdomNameTextView.text = kingdomName
            }
        }
    }

    companion object {
        private const val PROFILE_DETAIL_REQUEST_CODE = 1001
    }
}
