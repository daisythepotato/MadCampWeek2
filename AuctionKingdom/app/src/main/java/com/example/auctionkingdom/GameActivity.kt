package com.example.auctionkingdom

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class GameActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var gameStateTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var roomCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameStateTextView = findViewById(R.id.game_state_text_view)
        roomCode = intent.getStringExtra("roomCode") ?: return

        val playerMoveButton: Button = findViewById(R.id.player_move_button)
        playerMoveButton.setOnClickListener {
            // 플레이어의 이동 이벤트 처리
            updateGameState("Player moved")
        }

        startGameStateSync()
    }

    private fun startGameStateSync() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                fetchGameState()
                handler.postDelayed(this, 3000) // 3초마다 상태를 동기화
            }
        }, 3000)
    }

    private fun fetchGameState() {
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/getGameState?code=$roomCode")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    gameStateTextView.text = "Failed to load game state"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val gameState = jsonResponse.getString("gameState")
                        gameStateTextView.text = gameState
                    } catch (e: Exception) {
                        gameStateTextView.text = "Failed to parse game state"
                    }
                }
            }
        })
    }

    private fun updateGameState(newGameState: String) {
        val json = JSONObject().apply {
            put("code", roomCode)
            put("gameState", newGameState)
        }

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/updateGameState")
            .post(RequestBody.create("application/json".toMediaType(), json.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    gameStateTextView.text = "Failed to update game state"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                fetchGameState() // 게임 상태를 다시 불러옵니다.
            }
        })
    }

    private fun checkGameEnd() {
        // 게임이 끝나는 조건을 확인하는 로직을 여기에 추가

        // 예를 들어, 특정 조건이 만족되면 게임이 끝났다고 가정
        val gameEnded = true

        if (gameEnded) {
            val result = "player1_win" // 실제 결과를 여기에 넣습니다.
            saveGameResult(result)
        }
    }

    private fun saveGameResult(result: String) {
        val json = JSONObject().apply {
            put("code", roomCode)
            put("result", result)
        }

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/saveGameResult")
            .post(RequestBody.create("application/json".toMediaType(), json.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    gameStateTextView.text = "Failed to save game result"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    gameStateTextView.text = "Game result saved"
                    // 게임이 끝난 후의 처리를 여기에 추가
                }
            }
        })
    }

}
