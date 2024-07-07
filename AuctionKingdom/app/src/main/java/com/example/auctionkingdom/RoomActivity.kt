package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class RoomActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var roomsTextView: TextView
    private lateinit var userRoomsTextView: TextView
    private var email: String? = null
    private var currentRoomCode: String? = null
    private lateinit var socket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        email = intent.getStringExtra("email")

        val createRoomButton: Button = findViewById(R.id.create_room_button)
        val joinRoomButton: Button = findViewById(R.id.join_room_button)
        val leaveRoomButton: Button = findViewById(R.id.leave_room_button)
        val createRoomCodeEditText: EditText = findViewById(R.id.create_room_code_edit_text)
        val joinRoomCodeEditText: EditText = findViewById(R.id.join_room_code_edit_text)
        roomsTextView = findViewById(R.id.rooms_text_view)
        userRoomsTextView = findViewById(R.id.user_rooms_text_view)

        createRoomButton.setOnClickListener {
            val roomCode = createRoomCodeEditText.text.toString()
            if (email != null) {
                createRoom(roomCode, email!!)
            } else {
                Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
            }
        }

        joinRoomButton.setOnClickListener {
            val roomCode = joinRoomCodeEditText.text.toString()
            if (email != null) {
                joinRoom(roomCode, email!!)
            } else {
                Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
            }
        }

        leaveRoomButton.setOnClickListener {
            if (currentRoomCode != null && email != null) {
                leaveRoom(currentRoomCode!!, email!!)
            } else {
                Toast.makeText(this, "You are not currently in any room", Toast.LENGTH_SHORT).show()
            }
        }

        // 방 목록 가져오기
        fetchRooms()

        // 사용자가 속한 방 목록 가져오기
        if (email != null) {
            fetchUserRooms(email!!)
        }

        // 소켓 설정 및 이벤트 처리
        setupSocket()
    }

    private fun setupSocket() {
        socket = IO.socket("http://172.10.7.80:80")
        socket.on(Socket.EVENT_CONNECT) {
            // 연결 시 로그 메시지 출력
        }
        socket.on("roomListUpdated") {
            fetchRooms()
        }
        socket.on("roomUpdated") { args ->
            val data = args[0] as JSONObject
            val roomCode = data.getString("code")
            if (roomCode == currentRoomCode) {
                // RoomDetailActivity에서 fetchRoomDetails 호출
                val intent = Intent(this, RoomDetailActivity::class.java)
                intent.putExtra("roomCode", roomCode)
                startActivity(intent)
            }
        }
        socket.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }

    private fun createRoom(code: String, player: String) {
        val json = JSONObject().apply {
            put("code", code)
            put("player", player)
        }

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/createRoom")
            .post(RequestBody.create("application/json".toMediaType(), json.toString()))
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
                        val success = jsonResponse.getBoolean("success")
                        if (success) {
                            val roomCode = jsonResponse.getString("roomCode")
                            Toast.makeText(this@RoomActivity, "Room created: $roomCode", Toast.LENGTH_SHORT).show()
                            currentRoomCode = roomCode // 방 코드를 저장
                            fetchUserRooms(email!!)

                            // RoomDetailActivity로 이동
                            val intent = Intent(this@RoomActivity, RoomDetailActivity::class.java)
                            intent.putExtra("roomCode", roomCode)
                            startActivity(intent)
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

    private fun joinRoom(code: String, player: String) {
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
                        if (success) {
                            Toast.makeText(this@RoomActivity, "Joined room: $code", Toast.LENGTH_SHORT).show()
                            currentRoomCode = code // 방 코드를 저장
                            val intent = Intent(this@RoomActivity, RoomDetailActivity::class.java)
                            intent.putExtra("roomCode", code)
                            startActivity(intent)
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
                    Toast.makeText(this@RoomActivity, "Failed to leave room", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(response.body?.string())
                        val success = jsonResponse.getBoolean("success")
                        if (success) {
                            Toast.makeText(this@RoomActivity, "Left room successfully", Toast.LENGTH_SHORT).show()
                            currentRoomCode = null
                            fetchUserRooms(email)
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

    private fun fetchRooms() {
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/getRooms")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RoomActivity, "Failed to fetch rooms", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonArray = JSONArray(responseData)
                        val roomsList = StringBuilder()
                        for (i in 0 until jsonArray.length()) {
                            val room = jsonArray.getJSONObject(i)
                            val code = room.getString("code")
                            val players = room.getJSONArray("players")
                            roomsList.append("Room: $code, Players: ${players.length()}/2\n")
                        }
                        roomsTextView.text = roomsList.toString()
                    } catch (e: Exception) {
                        Toast.makeText(this@RoomActivity, "Failed to parse rooms response", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun fetchUserRooms(email: String) {
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/getUserRooms?email=$email")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RoomActivity, "Failed to fetch user rooms", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonArray = JSONArray(responseData)
                        val userRoomsList = StringBuilder()
                        for (i in 0 until jsonArray.length()) {
                            val room = jsonArray.getJSONObject(i)
                            val code = room.getString("code")
                            userRoomsList.append("Room: $code\n")
                        }
                        userRoomsTextView.text = userRoomsList.toString()

                        currentRoomCode = if (jsonArray.length() > 0) jsonArray.getJSONObject(0).getString("code") else null
                    } catch (e: Exception) {
                        Toast.makeText(this@RoomActivity, "Failed to parse user rooms response", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
