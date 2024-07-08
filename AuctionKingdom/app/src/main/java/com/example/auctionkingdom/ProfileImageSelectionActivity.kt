package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class ProfileImageSelectionActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var viewPager: ViewPager2
    private lateinit var profileNameText: TextView
    private lateinit var profileAgeText: TextView
    private lateinit var profileDescriptionText: TextView

    private val profileImages = listOf(
        "profile_image_1", // 문자열로 리소스 이름 저장
        "profile_image_2",
        "profile_image_3",
        "profile_image_4"
    )

    private val profileNames = listOf(
        "이름: 아스트리아",
        "이름: 레오나르도",
        "이름: 네로",
        "이름: 노바리스"
    )

    private val profileAges = listOf(
        "나이: 3살",
        "나이: 5살",
        "나이: 4살",
        "나이: 2살"
    )

    private val profileDescriptions = listOf(
        "설명: 아스트리아 왕은 호기심과 탐구심이 넘치는 지혜로운 지도자입니다. 왕국의 지식과 정보를 수집하는 데 뛰어난 능력을 가지고 있으며, 항상 새로운 모험을 찾아 떠나는 성향이 있습니다.",
        "설명: 레오나르도 왕은 지혜롭고 현명한 고양이 왕으로, 왕국의 모든 결정은 그의 신중한 판단에 따라 이루어집니다. 모든 고양이들이 신뢰하는 지도자로서, 평화를 유지하려는 노력에서 절대 흔들리지 않습니다.",
        "설명: 네로 왕은 강력하고 무자비한 통치자로, 왕국의 모든 고양이들에게 두려움과 존경을 동시에 받습니다. 왕국의 법과 질서를 엄격하게 지키며, 강한 통치력으로 왕국을 다스립니다.",
        "설명: 노바리스 왕은 왕국에서 가장 용감한 고양이로, 위험한 상황에서도 두려워하지 않습니다. 왕국을 지키기 위해 언제나 앞장서는 그는 용기와 희생정신으로 다른 고양이들에게 영감을 주며, 강인한 리더십을 발휘합니다."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_image_selection)

        val email = intent.getStringExtra("email")
        val nickname = intent.getStringExtra("nickname")
        val kingdomName = intent.getStringExtra("kingdomName")

        viewPager = findViewById(R.id.viewPager)
        profileNameText = findViewById(R.id.profile_name)
        profileAgeText = findViewById(R.id.profile_age)
        profileDescriptionText = findViewById(R.id.profile_description)

        val selectButton: Button = findViewById(R.id.select_button)

        viewPager.adapter = ViewPagerAdapter(this, profileImages.map { resources.getIdentifier(it, "drawable", packageName) })
        profileNameText.text = profileNames[0]
        profileAgeText.text = profileAges[0]
        profileDescriptionText.text = profileDescriptions[0]

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                profileNameText.text = profileNames[position]
                profileAgeText.text = profileAges[position]
                profileDescriptionText.text = profileDescriptions[position]
            }
        })

        selectButton.setOnClickListener {
            val selectedPosition = viewPager.currentItem
            val selectedImageName = profileImages[selectedPosition]
            saveUserProfile(email ?: "", nickname ?: "", kingdomName ?: "", selectedImageName)
        }
    }

    private fun saveUserProfile(email: String, nickname: String, kingdomName: String, profileImageName: String) {
        val json = JSONObject().apply {
            put("email", email)
            put("nickname", nickname)
            put("kingdomName", kingdomName)
            put("profileImage", profileImageName)
        }

        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/saveUser")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    showAlertDialog("Failed to save profile: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProfileImageSelectionActivity, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@ProfileImageSelectionActivity, MainActivity::class.java).apply {
                            putExtra("email", email)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        showAlertDialog("Failed to save profile: ${response.message}")
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
