package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
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
    private lateinit var p1NameTextView: TextView
    private lateinit var p1KingdomNameTextView: TextView
    private lateinit var p1ReadyStatusTextView: TextView
    private lateinit var p1ProfileImageView: ImageView
    private lateinit var p2NameTextView: TextView
    private lateinit var p2KingdomNameTextView: TextView
    private lateinit var p2ReadyStatusTextView: TextView
    private lateinit var p2ProfileImageView: ImageView
    private lateinit var leaveRoomButton: ImageButton
    private lateinit var toggleReadyButton: ImageButton
    private lateinit var matchButton: ImageButton
    private lateinit var socket: Socket
    private var roomCode: String? = null
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)

        p1NameTextView = findViewById(R.id.p1_name_text_view)
        p1KingdomNameTextView = findViewById(R.id.p1_kingdom_name_text_view)
        p1ReadyStatusTextView = findViewById(R.id.p1_ready_status_text_view)
        p1ProfileImageView = findViewById(R.id.p1_profile_image_view)
        p2NameTextView = findViewById(R.id.p2_name_text_view)
        p2KingdomNameTextView = findViewById(R.id.p2_kingdom_name_text_view)
        p2ReadyStatusTextView = findViewById(R.id.p2_ready_status_text_view)
        p2ProfileImageView = findViewById(R.id.p2_profile_image_view)
        leaveRoomButton = findViewById(R.id.leave_room_button)
        toggleReadyButton = findViewById(R.id.toggle_ready_button)
        matchButton = findViewById(R.id.match_button)

        roomCode = intent.getStringExtra("roomCode")
        email = intent.getStringExtra("email")

        Log.d("RoomDetailActivity", "roomCode: $roomCode, email: $email")

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

        toggleReadyButton.setOnClickListener {
            if (roomCode != null && email != null) {
                toggleReady(roomCode!!, email!!)
            } else {
                Toast.makeText(this, "Room code or email not found", Toast.LENGTH_SHORT).show()
            }
        }

        matchButton.setOnClickListener {
            if (roomCode != null && email != null) {
                checkAndStartMatch(roomCode!!, email!!)
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
            socket.emit("joinRoom", roomCode)
        }
        socket.on("roomUpdated") { args ->
            val data = args[0] as JSONObject
            val updatedRoomCode = data.getString("code")
            if (updatedRoomCode == roomCode) {
                fetchRoomDetails(updatedRoomCode)
            }
        }
        socket.on("startMatch") { args ->
            val data = args[0] as JSONObject
            val player1Email = data.getString("player1Email")
            val player2Email = data.getString("player2Email")
            runOnUiThread {
                Toast.makeText(this, "Match started", Toast.LENGTH_SHORT).show()
                startGameActivity(player1Email, player2Email, email!!)
            }
        }
        socket.connect()
    }

    private fun leaveRoomSocket(roomCode: String) {
        socket.emit("leaveRoom", roomCode)
    }

    private fun startGameActivity(player1Email: String, player2Email: String, currentEmail: String) {
        leaveRoomSocket(roomCode!!) // Ensure the user leaves the Room socket

        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("player1Email", player1Email)
            putExtra("player2Email", player2Email)
            putExtra("currentEmail", currentEmail)
        }
        startActivity(intent)
        finish()
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

                        // 첫 번째 플레이어 정보
                        if (players.length() > 0) {
                            val player1 = players.getJSONObject(0)
                            p1NameTextView.text = player1.getString("nickname")
                            p1KingdomNameTextView.text = player1.getString("kingdomName")
                            val readyStatus1 = if (player1.getBoolean("ready")) "Ready" else "Unready"
                            p1ReadyStatusTextView.text = readyStatus1
                            val profileImageRes = resources.getIdentifier(player1.getString("profileImage"), "drawable", packageName)
                            p1ProfileImageView.setImageResource(profileImageRes)
                        }

                        // 두 번째 플레이어 정보
                        if (players.length() > 1) {
                            val player2 = players.getJSONObject(1)
                            p2NameTextView.text = player2.getString("nickname")
                            p2KingdomNameTextView.text = player2.getString("kingdomName")
                            val readyStatus2 = if (player2.getBoolean("ready")) "Ready" else "Unready"
                            p2ReadyStatusTextView.text = readyStatus2
                            val profileImageRes = resources.getIdentifier(player2.getString("profileImage"), "drawable", packageName)
                            p2ProfileImageView.setImageResource(profileImageRes)
                        } else {
                            // 두 번째 플레이어가 없을 때 처리
                            p2NameTextView.text = "Waiting for Player 2"
                            p2KingdomNameTextView.text = ""
                            p2ReadyStatusTextView.text = ""
                            p2ProfileImageView.setImageResource(R.drawable.default_image)
                        }
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

    private fun toggleReady(code: String, email: String) {
        val json = JSONObject().apply {
            put("code", code)
            put("email", email)
        }

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/toggleReady")
            .post(RequestBody.create("application/json".toMediaType(), json.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RoomDetailActivity, "Failed to toggle ready state", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        Log.d("RoomDetailActivity", "Response: $responseData")
                        val jsonResponse = JSONObject(responseData)
                        val success = jsonResponse.getBoolean("success")
                        if (success) {
                            Toast.makeText(this@RoomDetailActivity, "Ready state toggled", Toast.LENGTH_SHORT).show()
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

    private fun checkAndStartMatch(code: String, email: String) {
        val json = JSONObject().apply {
            put("code", code)
            put("email", email)
        }

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/checkAndStartMatch")
            .post(RequestBody.create("application/json".toMediaType(), json.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RoomDetailActivity, "Failed to start match", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val success = jsonResponse.getBoolean("success")
                        if (success) {
                            // Extract player emails from the response
                            val player1Email = jsonResponse.getString("player1Email")
                            val player2Email = jsonResponse.getString("player2Email")

                            // Start GameActivity with player emails
                            startGameActivity(player1Email, player2Email, email!!)
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