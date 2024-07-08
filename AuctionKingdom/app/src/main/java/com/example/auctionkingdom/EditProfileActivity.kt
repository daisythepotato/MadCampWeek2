package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode

class EditProfileActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val profileImages = arrayOf(
        R.drawable.profile_image_1,
        R.drawable.profile_image_2,
        R.drawable.profile_image_3,
        R.drawable.profile_image_4
    )
    private var currentImageIndex = 0
    private lateinit var profileImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val email = intent.getStringExtra("email")
        val nicknameInput: EditText = findViewById(R.id.nickname_input)
        val kingdomNameInput: EditText = findViewById(R.id.kingdom_name_input)
        profileImageView = findViewById(R.id.profile_image)
        val leftArrow: ImageView = findViewById(R.id.left_arrow)
        val rightArrow: ImageView = findViewById(R.id.right_arrow)
        val confirmButton: Button = findViewById(R.id.confirm_button)
        val cancelButton: Button = findViewById(R.id.cancel_button)

        // 초기 사용자 데이터 불러오기
        fetchUserData(email, nicknameInput, kingdomNameInput, profileImageView)

        // 캐릭터 이미지 변경 이벤트
        leftArrow.setOnClickListener {
            currentImageIndex = (currentImageIndex - 1 + profileImages.size) % profileImages.size
            profileImageView.setImageResource(profileImages[currentImageIndex])
        }

        rightArrow.setOnClickListener {
            currentImageIndex = (currentImageIndex + 1) % profileImages.size
            profileImageView.setImageResource(profileImages[currentImageIndex])
        }

        // 확인 버튼 클릭 이벤트
        confirmButton.setOnClickListener {
            saveUserData(email, nicknameInput.text.toString(), kingdomNameInput.text.toString(), currentImageIndex)
        }

        // 취소 버튼 클릭 이벤트
        cancelButton.setOnClickListener {
            finish()
        }

        // 뒤로 가기 버튼 클릭 이벤트
        val backButton: ImageView = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchUserData(email: String?, nicknameInput: EditText, kingdomNameInput: EditText, profileImageView: ImageView) {
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
                    nicknameInput.setText("Failed to load data")
                    kingdomNameInput.setText("")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val nickname = jsonResponse.getString("nickname")
                        val kingdomName = jsonResponse.getString("kingdomName")
                        val profileImageRes = jsonResponse.getInt("profileImage")

                        nicknameInput.setText(nickname)
                        kingdomNameInput.setText(kingdomName)
                        profileImageView.setImageResource(profileImages[profileImageRes])
                        currentImageIndex = profileImageRes
                    } catch (e: Exception) {
                        nicknameInput.setText("Failed to parse data")
                        kingdomNameInput.setText("")
                    }
                }
            }
        })
    }

    private fun saveUserData(email: String?, nickname: String, kingdomName: String, profileImageIndex: Int) {
        if (email == null) {
            return
        }

        val json = JSONObject()
        json.put("email", email)
        json.put("nickname", nickname)
        json.put("kingdomName", kingdomName)
        json.put("profileImage", profileImageIndex)

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/updateUser")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // 실패 시 처리
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    // 성공 시 처리
                    finish()
                }
            }
        })
    }
}
