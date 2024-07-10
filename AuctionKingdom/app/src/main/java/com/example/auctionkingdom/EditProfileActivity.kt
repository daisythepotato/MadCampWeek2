package com.example.auctionkingdom

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class EditProfileActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val profileImages = arrayOf(
        "profile_image_1",
        "profile_image_2",
        "profile_image_3",
        "profile_image_4"
    )
    private var currentImageIndex = 0
    private lateinit var profileImageView: ImageView
    private var isKingdomNameAvailable = false
    private var isNicknameAvailable = false
    private var isKingdomNameNotChanged = true
    private var isNicknameNotChanged = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val email = intent.getStringExtra("email")
        val nicknameInput: EditText = findViewById(R.id.nickname_input)
        val kingdomNameInput: EditText = findViewById(R.id.kingdom_name_input)
        profileImageView = findViewById(R.id.profile_image)
        val leftArrow: ImageView = findViewById(R.id.left_arrow)
        val rightArrow: ImageView = findViewById(R.id.right_arrow)
        val confirmButton: ImageButton = findViewById(R.id.confirm_button)
        val cancelButton: ImageButton = findViewById(R.id.cancel_button)
        val nicknameResultText: TextView = findViewById(R.id.nickname_result_text)
        val kingdomNameResultText: TextView = findViewById(R.id.kingdom_name_result_text)

        // 초기 사용자 데이터 불러오기
        fetchUserData(email, nicknameInput, kingdomNameInput, profileImageView)

        // 캐릭터 이미지 변경 이벤트
        leftArrow.setOnClickListener {
            currentImageIndex = (currentImageIndex - 1 + profileImages.size) % profileImages.size
            val resId = resources.getIdentifier(profileImages[currentImageIndex], "drawable", packageName)
            profileImageView.setImageResource(resId)
        }

        rightArrow.setOnClickListener {
            currentImageIndex = (currentImageIndex + 1) % profileImages.size
            val resId = resources.getIdentifier(profileImages[currentImageIndex], "drawable", packageName)
            profileImageView.setImageResource(resId)
        }

        // 확인 버튼 클릭 이벤트
        confirmButton.setOnClickListener {
            Log.d("EditProfileActivity", "Confirm button clicked")
            val newNickname = nicknameInput.text.toString()
            val newKingdomName = kingdomNameInput.text.toString()

            if (newNickname.isNotEmpty() && newKingdomName.isNotEmpty()) {
                if ((isNicknameAvailable && isKingdomNameAvailable) || (isKingdomNameNotChanged && isNicknameNotChanged)) {
                    saveUserData(email, newNickname, newKingdomName, profileImages[currentImageIndex])
                } else {
                    showAlertDialog("Please check the availability of the nickname and kingdom name.")
                }
            } else {
                showAlertDialog("Please fill out all fields.")
            }
        }

        // 취소 버튼 클릭 이벤트
        cancelButton.setOnClickListener {
            Log.d("EditProfileActivity", "Cancel button clicked")
            finish()
        }

        // 뒤로 가기 버튼 클릭 이벤트
        val backButton: ImageView = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            Log.d("EditProfileActivity", "Back button clicked")
            finish()
        }

        // 닉네임과 왕국 이름의 중복 확인 버튼 추가
        val checkNicknameButton: ImageButton = findViewById(R.id.check_nickname_button)
        checkNicknameButton.setOnClickListener {
            val nickname = nicknameInput.text.toString()
            isNicknameNotChanged = false
            if (nickname.isNotEmpty()) {
                checkAvailability("nickname", nickname, nicknameResultText)
            } else {
                showAlertDialog("Please enter your nickname")
            }
        }

        val checkKingdomButton: ImageButton = findViewById(R.id.check_kingdomName_button)
        checkKingdomButton.setOnClickListener {
            val kingdomName = kingdomNameInput.text.toString()
            isKingdomNameNotChanged = false
            if (kingdomName.isNotEmpty()) {
                checkAvailability("kingdomName", kingdomName, kingdomNameResultText)
            } else {
                showAlertDialog("Please enter your kingdom name")
            }
        }

        nicknameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                nicknameResultText.visibility = TextView.GONE
                isNicknameAvailable = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        kingdomNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                kingdomNameResultText.visibility = TextView.GONE
                isKingdomNameAvailable = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })
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
                Log.e("EditProfileActivity", "Failed to fetch user data", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val nickname = jsonResponse.getString("nickname")
                        val kingdomName = jsonResponse.getString("kingdomName")
                        val profileImagePath = jsonResponse.getString("profileImage")

                        nicknameInput.setText(nickname)
                        kingdomNameInput.setText(kingdomName)

                        // 파일 경로를 인덱스로 매핑
                        val profileImageIndex = profileImages.indexOfFirst { profileImagePath.contains(it) }
                        if (profileImageIndex != -1) {
                            currentImageIndex = profileImageIndex
                            val resId = resources.getIdentifier(profileImages[profileImageIndex], "drawable", packageName)
                            profileImageView.setImageResource(resId)
                        } else {
                            profileImageView.setImageResource(R.drawable.default_icon)
                        }
                    } catch (e: Exception) {
                        nicknameInput.setText("Failed to parse data")
                        kingdomNameInput.setText("")
                        Log.e("EditProfileActivity", "Failed to parse user data", e)
                    }
                }
            }
        })
    }

    private fun checkAvailability(field: String, value: String, resultText: TextView) {
        val json = JSONObject().apply {
            put("field", field)
            put("value", value)
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/checkAvailability")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    showAlertDialog("Failed to check availability: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val available = jsonResponse.getBoolean("available")
                        resultText.visibility = TextView.VISIBLE
                        if (available) {
                            resultText.text = "You can use this name."
                            resultText.setTextColor(Color.GREEN)
                            if (field == "kingdomName") {
                                isKingdomNameAvailable = true
                            } else if (field == "nickname") {
                                isNicknameAvailable = true
                            }
                        } else {
                            resultText.text = "You can't use this name. Please insert other name."
                            resultText.setTextColor(Color.RED)
                            if (field == "kingdomName") {
                                isKingdomNameAvailable = false
                            } else if (field == "nickname") {
                                isNicknameAvailable = false
                            }
                        }
                    } catch (e: Exception) {
                        showAlertDialog("Error parsing server response: ${e.message}")
                    }
                }
            }
        })
    }

    private fun saveUserData(email: String?, nickname: String, kingdomName: String, profileImage: String) {
        if (email == null) {
            Log.e("EditProfileActivity", "Email is null")
            return
        }

        val json = JSONObject()
        json.put("email", email)
        json.put("nickname", nickname)
        json.put("kingdomName", kingdomName)
        json.put("profileImage", profileImage)

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/updateUser")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // 실패 시 처리
                    Log.e("EditProfileActivity", "Failed to save user data", e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    // 성공 시 처리
                    if (response.isSuccessful) {
                        Log.d("EditProfileActivity", "User data saved successfully")
                        val intent = Intent().apply {
                            putExtra("nickname", nickname)
                            putExtra("kingdomName", kingdomName)
                            putExtra("profileImage", profileImage)
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                    } else {
                        Log.e("EditProfileActivity", "Failed to save user data: ${response.code}")
                    }
                }
            }
        })
    }

    private fun showAlertDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        val alert = builder.create()
        alert.show()
    }
}
