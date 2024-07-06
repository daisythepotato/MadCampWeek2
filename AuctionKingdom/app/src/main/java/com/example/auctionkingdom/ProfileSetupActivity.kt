package com.example.auctionkingdom

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class ProfileSetupActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private var isKingdomNameAvailable = false
    private var isNicknameAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        val email = intent.getStringExtra("email")

        val kingdomNameEditText: EditText = findViewById(R.id.kingdom_name_edit_text)
        val nicknameEditText: EditText = findViewById(R.id.nickname_edit_text)
        val kingdomNameResultText: TextView = findViewById(R.id.kingdom_name_result_text)
        val nicknameResultText: TextView = findViewById(R.id.nickname_result_text)

        val checkKingdomButton: Button = findViewById(R.id.check_kingdom_button)
        checkKingdomButton.setOnClickListener {
            val kingdomName = kingdomNameEditText.text.toString()
            if (kingdomName.isNotEmpty()) {
                checkAvailability("kingdomName", kingdomName, kingdomNameResultText)
            } else {
                showAlertDialog("Please enter your kingdom name")
            }
        }

        val checkNicknameButton: Button = findViewById(R.id.check_nickname_button)
        checkNicknameButton.setOnClickListener {
            val nickname = nicknameEditText.text.toString()
            if (nickname.isNotEmpty()) {
                checkAvailability("nickname", nickname, nicknameResultText)
            } else {
                showAlertDialog("Please enter your nickname")
            }
        }

        val saveButton: Button = findViewById(R.id.save_button)
        saveButton.setOnClickListener {
            val nickname = nicknameEditText.text.toString()
            val kingdomName = kingdomNameEditText.text.toString()
            if (nickname.isNotEmpty() && kingdomName.isNotEmpty() && isKingdomNameAvailable && isNicknameAvailable) {
                saveUserProfile(email ?: "", nickname, kingdomName)
            } else {
                showAlertDialog("Please fill out all fields and ensure names are available")
            }
        }

        kingdomNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                kingdomNameResultText.visibility = TextView.GONE
                isKingdomNameAvailable = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        nicknameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                nicknameResultText.visibility = TextView.GONE
                isNicknameAvailable = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun checkAvailability(field: String, value: String, resultText: TextView) {
        val json = JSONObject().apply {
            put("field", field)
            put("value", value)
        }

        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())
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
                    showAlertDialog("Failed to save profile: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    Toast.makeText(this@ProfileSetupActivity, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ProfileSetupActivity, MainActivity::class.java).apply {
                        putExtra("email", email)
                        putExtra("nickname", nickname)
                        putExtra("kingdomName", kingdomName)
                    }
                    startActivity(intent)
                    finish()
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
