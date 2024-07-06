package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
    private val profileImages = listOf(
        R.drawable.profile_image_1,
        R.drawable.profile_image_2,
        R.drawable.profile_image_3,
        R.drawable.profile_image_4
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_image_selection)

        val email = intent.getStringExtra("email")
        val nickname = intent.getStringExtra("nickname")
        val kingdomName = intent.getStringExtra("kingdomName")

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val selectButton: Button = findViewById(R.id.select_button)

        viewPager.adapter = ViewPagerAdapter(this, profileImages)

        selectButton.setOnClickListener {
            val selectedPosition = viewPager.currentItem
            val selectedImageRes = profileImages[selectedPosition]
            saveUserProfile(email ?: "", nickname ?: "", kingdomName ?: "", selectedImageRes)
        }
    }

    private fun saveUserProfile(email: String, nickname: String, kingdomName: String, profileImage: Int) {
        val json = JSONObject().apply {
            put("email", email)
            put("nickname", nickname)
            put("kingdomName", kingdomName)
            put("profileImage", profileImage)
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
