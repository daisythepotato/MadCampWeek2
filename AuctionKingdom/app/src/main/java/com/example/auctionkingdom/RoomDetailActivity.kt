package com.example.auctionkingdom

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class RoomDetailActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var roomCodeTextView: TextView
    private lateinit var playersTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)

        roomCodeTextView = findViewById(R.id.room_code_text_view)
        playersTextView = findViewById(R.id.players_text_view)

        val roomCode = intent.getStringExtra("roomCode")
        roomCodeTextView.text = "Room Code: $roomCode"

        // 방 상세 정보 가져오기
        if (roomCode != null) {
            fetchRoomDetails(roomCode)
        } else {
            Toast.makeText(this, "Room code not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchRoomDetails(roomCode: String) {
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/getRoomDetails?code=$roomCode")
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
                        val playersList = StringBuilder()
                        for (i in 0 until players.length()) {
                            val player = players.getJSONObject(i)
                            val nickname = player.getString("nickname")
                            playersList.append("Player: $nickname\n")
                        }
                        playersTextView.text = playersList.toString()
                    } catch (e: Exception) {
                        Toast.makeText(this@RoomDetailActivity, "Failed to parse room details", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
