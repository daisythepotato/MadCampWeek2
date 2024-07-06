package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class RoomActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        val email = intent.getStringExtra("email")

        val createRoomButton: Button = findViewById(R.id.create_room_button)
        val joinRoomButton: Button = findViewById(R.id.join_room_button)
        val createRoomCodeEditText: EditText = findViewById(R.id.create_room_code_edit_text)
        val joinRoomCodeEditText: EditText = findViewById(R.id.join_room_code_edit_text)

        createRoomButton.setOnClickListener {
            val roomCode = createRoomCodeEditText.text.toString()
            if (email != null) {
                createRoom(roomCode, email)
            } else {
                Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
            }
        }

        joinRoomButton.setOnClickListener {
            val roomCode = joinRoomCodeEditText.text.toString()
            if (email != null) {
                joinRoom(roomCode, email)
            } else {
                Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createRoom(code: String, email: String) {
        val json = JSONObject().apply {
            put("code", code)
        }

        val requestBody = RequestBody.create("application/json".toMediaType(), json.toString())

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/createRoom")
            .post(requestBody)
            .build()

        Log.d("RoomActivity", "Sending request to create room: $json") // 추가된 로그

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("RoomActivity", "Failed to create room", e) // 추가된 로그
                    Toast.makeText(this@RoomActivity, "Failed to create room", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("RoomActivity", "Response: $responseData") // 추가된 로그
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val success = jsonResponse.getBoolean("success")
                        if (success) {
                            val roomCode = jsonResponse.getString("roomCode")
                            Toast.makeText(this@RoomActivity, "Room created: $roomCode", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@RoomActivity, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("RoomActivity", "Failed to parse response", e) // 추가된 로그
                        Toast.makeText(this@RoomActivity, "Failed to parse response", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun joinRoom(code: String, email: String) {
        val json = JSONObject().apply {
            put("code", code)
            put("player", email)
        }

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/joinRoom")
            .post(RequestBody.create("application/json".toMediaType(), json.toString()))
            .build()

        Log.d("RoomActivity", "Sending request to join room: $json") // 추가된 로그

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("RoomActivity", "Failed to join room", e) // 추가된 로그
                    Toast.makeText(this@RoomActivity, "Failed to join room", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("RoomActivity", "Response: $responseData") // 추가된 로그
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val success = jsonResponse.getBoolean("success")
                        val gameState = jsonResponse.getString("gameState")
                        if (success && gameState == "started") {
                            Toast.makeText(this@RoomActivity, "Game started", Toast.LENGTH_SHORT).show()
                            // 게임 화면으로 이동
                            val intent = Intent(this@RoomActivity, GameActivity::class.java)
                            startActivity(intent)
                        } else if (success) {
                            Toast.makeText(this@RoomActivity, "Waiting for another player", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@RoomActivity, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("RoomActivity", "Failed to parse response", e) // 추가된 로그
                        Toast.makeText(this@RoomActivity, "Failed to parse response", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
