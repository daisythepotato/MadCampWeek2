package com.example.auctionkingdom

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class ProfileSetupActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        val email = intent.getStringExtra("email")
        val name = intent.getStringExtra("name")

        val nicknameEditText: EditText = findViewById(R.id.nickname_edit_text)
        val kingdomNameEditText: EditText = findViewById(R.id.kingdom_name_edit_text)

        val saveButton: Button = findViewById(R.id.save_button)
        saveButton.setOnClickListener {
            val nickname = nicknameEditText.text.toString()
            val kingdomName = kingdomNameEditText.text.toString()
            if (nickname.isNotEmpty() && kingdomName.isNotEmpty()) {
                saveUserProfile(email ?: "", nickname, kingdomName)
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserProfile(email: String, nickname: String, kingdomName: String) {
        val json = JSONObject().apply {
            put("email", email)
            put("nickname", nickname)
            put("kingdomName", kingdomName)
        }

        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/saveUser")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ProfileSetupActivity, "Failed to save profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    Toast.makeText(this@ProfileSetupActivity, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        })
    }
}
