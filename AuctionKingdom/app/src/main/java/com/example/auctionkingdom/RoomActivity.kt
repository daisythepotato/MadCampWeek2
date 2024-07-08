package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private var email: String? = null
    private var currentRoomCode: String? = null
    private lateinit var socket: Socket // 소켓 변수 추가

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        email = intent.getStringExtra("email")

        val createRoomButton: Button = findViewById(R.id.create_room_button)
        val joinRoomButton: Button = findViewById(R.id.join_room_button)
        roomsTextView = findViewById(R.id.rooms_text_view)

        createRoomButton.setOnClickListener {
            showRoomCodeDialog("Create Room") { roomCode ->
                if (email != null) {
                    createRoom(roomCode, email!!)
                } else {
                    Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
                }
            }
        }

        joinRoomButton.setOnClickListener {
            showRoomCodeDialog("Join Room") { roomCode ->
                if (email != null) {
                    joinRoom(roomCode, email!!)
                } else {
                    Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 방 목록 가져오기
        fetchRooms()

        // 소켓 설정 및 이벤트 처리
        setupSocket()
    }

    private fun showRoomCodeDialog(title: String, callback: (String) -> Unit) {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_room_code, null)
        dialogBuilder.setView(dialogView)

        val roomCodeEditText: EditText = dialogView.findViewById(R.id.dialog_room_code_edit_text)
        dialogBuilder.setTitle(title)
        dialogBuilder.setPositiveButton("OK") { _, _ ->
            val roomCode = roomCodeEditText.text.toString()
            callback(roomCode)
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
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
                intent.putExtra("email", email) // 이메일 추가
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
                            // 방 생성 후 RoomDetailActivity로 이동
                            val intent = Intent(this@RoomActivity, RoomDetailActivity::class.java)
                            intent.putExtra("roomCode", roomCode)
                            intent.putExtra("email", player) // 이메일 추가
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
                            val intent = Intent(this@RoomActivity, RoomDetailActivity::class.java)
                            intent.putExtra("roomCode", code)
                            intent.putExtra("email", player) // 이메일 추가
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
}
