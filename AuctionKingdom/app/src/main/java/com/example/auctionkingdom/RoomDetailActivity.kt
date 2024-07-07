package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
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
    private lateinit var socket: Socket
    private var roomCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)

        roomDetailTextView = findViewById(R.id.room_detail_text_view)
        roomCode = intent.getStringExtra("roomCode")

        if (roomCode != null) {
            fetchRoomDetails(roomCode!!)
        } else {
            Toast.makeText(this, "Room code not found", Toast.LENGTH_SHORT).show()
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

    override fun onBackPressed() {
        val intent = Intent(this, RoomActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }
}
