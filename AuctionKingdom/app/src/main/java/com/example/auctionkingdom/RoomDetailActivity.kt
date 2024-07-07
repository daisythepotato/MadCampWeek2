package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class RoomDetailActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var roomDetailTextView: TextView
    private lateinit var leaveRoomButton: Button
    private lateinit var socket: Socket
    private var roomCode: String? = null
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)

        roomDetailTextView = findViewById(R.id.room_detail_text_view)
        leaveRoomButton = findViewById(R.id.leave_room_button)
        roomCode = intent.getStringExtra("roomCode")
        email = intent.getStringExtra("email")

        Log.d("RoomDetailActivity", "roomCode: $roomCode, email: $email") // 로그 추가

        if (roomCode != null && email != null) {
            fetchRoomDetails(roomCode!!)
        } else {
            Toast.makeText(this, "Room code or email not found", Toast.LENGTH_SHORT).show()
        }

        leaveRoomButton.setOnClickListener {
            if (roomCode != null && email != null) {
                leaveRoom(roomCode!!, email!!)
            } else {
                Toast.makeText(this, "Room code or email not found", Toast.LENGTH_SHORT).show()
            }
        }

        // 소켓 설정 및 이벤트 처리
        setupSocket()
    }

    private fun setupSocket() {
        socket = IO.socket("http://172.10.7.80:80")
        socket.on(Socket.EVENT_CONNECT) {
            // 연결 시 로그 메시지 출력
        }
        socket.on("roomUpdated") { args ->
            val data = args[0] as JSONObject
            val updatedRoomCode = data.getString("code")
            if (updatedRoomCode == roomCode) {
                fetchRoomDetails(updatedRoomCode)
            }
        }
        socket.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }

    private fun fetchRoomDetails(code: String) {
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/getRoomDetails?code=$code")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RoomDetailActivity, "Failed to fetch room details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val players = jsonResponse.getJSONArray("players")
                        val playerList = StringBuilder()
                        for (i in 0 until players.length()) {
                            playerList.append(players.getString(i)).append("\n")
                        }
                        roomDetailTextView.text = "Room Code: $code\nPlayers:\n$playerList"
                    } catch (e: Exception) {
                        Toast.makeText(this@RoomDetailActivity, "Failed to parse room details", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun leaveRoom(code: String, email: String) {
        val json = JSONObject().apply {
            put("code", code)
            put("email", email)
        }

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/leaveRoom")
            .post(RequestBody.create("application/json".toMediaType(), json.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RoomDetailActivity, "Failed to leave room", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(response.body?.string())
                        val success = jsonResponse.getBoolean("success")
                        if (success) {
                            Toast.makeText(this@RoomDetailActivity, "Left room successfully", Toast.LENGTH_SHORT).show()
                            // RoomActivity로 돌아가기
                            val intent = Intent(this@RoomDetailActivity, RoomActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@RoomDetailActivity, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@RoomDetailActivity, "Failed to parse response", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        if (roomCode != null && email != null) {
            leaveRoom(roomCode!!, email!!)
        } else {
            val intent = Intent(this, RoomActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }
}
