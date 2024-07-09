package com.example.auctionkingdom

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class GameActivity : AppCompatActivity() {

    private lateinit var player1TextView: TextView
    private lateinit var player2TextView: TextView
    private lateinit var gameStatusTextView: TextView
    private lateinit var socket: Socket
    private val client = OkHttpClient()
    private var gameId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        player1TextView = findViewById(R.id.player1_text_view)
        player2TextView = findViewById(R.id.player2_text_view)
        gameStatusTextView = findViewById(R.id.game_status_text_view)

        val player1Email = intent.getStringExtra("player1Email")
        val player2Email = intent.getStringExtra("player2Email")

        player1TextView.text = player1Email
        player2TextView.text = player2Email

        if (player1Email != null && player2Email != null) {
            createGame(player1Email, player2Email)
        }

        // 소켓 설정 및 이벤트 처리
        setupSocket()
    }

    private fun setupSocket() {
        socket = IO.socket("http://172.10.7.80:80")
        socket.on(Socket.EVENT_CONNECT) {
            // 연결 시 로그 메시지 출력
        }
        socket.on("gameUpdated") { args ->
            val data = args[0] as JSONObject
            runOnUiThread {
                updateGameState(data)
            }
        }
        socket.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }

    private fun createGame(player1Email: String, player2Email: String) {
        val json = JSONObject().apply {
            put("player1Email", player1Email)
            put("player2Email", player2Email)
        }

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/createGame")
            .post(RequestBody.create("application/json".toMediaType(), json.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // 실패 시 처리
                    gameStatusTextView.text = "Failed to create game: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val success = jsonResponse.getBoolean("success")
                        if (success) {
                            gameId = jsonResponse.getString("gameId")
                            gameStatusTextView.text = "Game created successfully"
                        } else {
                            gameStatusTextView.text = "Failed to create game: ${jsonResponse.getString("message")}"
                        }
                    } catch (e: Exception) {
                        gameStatusTextView.text = "Failed to parse response: ${e.message}"
                    }
                }
            }
        })
    }

    private fun updateGameState(data: JSONObject) {
        // 여기에 게임 상태 업데이트 로직을 추가합니다.
    }
}
