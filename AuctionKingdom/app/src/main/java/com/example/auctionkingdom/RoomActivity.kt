package com.example.auctionkingdom

import android.os.Bundle
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

        val createRoomButton: Button = findViewById(R.id.create_room_button)
        val joinRoomButton: Button = findViewById(R.id.join_room_button)
        val roomCodeEditText: EditText = findViewById(R.id.room_code_edit_text)

        createRoomButton.setOnClickListener {
            createRoom()
        }

        joinRoomButton.setOnClickListener {
            val roomCode = roomCodeEditText.text.toString()
            joinRoom(roomCode)
        }
    }

    private fun createRoom() {
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/createRoom")
            .post(RequestBody.create("application/json".toMediaType(), ""))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RoomActivity, "Failed to create room", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val roomCode = jsonResponse.getString("roomCode")
                        Toast.makeText(this@RoomActivity, "Room created: $roomCode", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@RoomActivity, "Failed to parse response", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun joinRoom(code: String) {
        val player = "player_email@example.com" // 실제 플레이어 정보를 사용
        val json = JSONObject().apply {
            put("code", code)
            put("player", player)
        }

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/joinRoom")
            .post(RequestBody.create("application/json".toMediaType(), json.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RoomActivity, "Failed to join room", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val success = jsonResponse.getBoolean("success")
                        val gameState = jsonResponse.getString("gameState")
                        if (success && gameState == "ready") {
                            Toast.makeText(this@RoomActivity, "Room joined, game is ready to start", Toast.LENGTH_SHORT).show()
                        } else if (success) {
                            Toast.makeText(this@RoomActivity, "Room joined, waiting for another player", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@RoomActivity, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@RoomActivity, "Failed to parse response", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
