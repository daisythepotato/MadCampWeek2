package com.example.auctionkingdom

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class RoomDetailActivity : AppCompatActivity() {

    private lateinit var socket: Socket
    private lateinit var roomDetailTextView: TextView
    private lateinit var roomCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)

        roomDetailTextView = findViewById(R.id.room_detail_text_view)
        roomCode = intent.getStringExtra("roomCode")!!

        fetchRoomDetails(roomCode)

        // 소켓 설정 및 이벤트 처리
        setupSocket()
    }

    private fun setupSocket() {
        socket = IO.socket("http://172.10.7.80:80")
        socket.on(Socket.EVENT_CONNECT) {
            socket.emit("joinRoom", roomCode)
        }
        socket.on("roomUpdated") {
            fetchRoomDetails(roomCode)
        }
        socket.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }

    private fun fetchRoomDetails(roomCode: String) {
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/getRoomDetails?code=$roomCode")
            .get()
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
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
                        roomDetailTextView.text = playerList.toString()
                    } catch (e: Exception) {
                        Toast.makeText(this@RoomDetailActivity, "Failed to parse room details", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
