package com.example.auctionkingdom

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class GameActivity : AppCompatActivity() {

    private lateinit var gameStatusTextView: TextView
    private lateinit var socket: Socket
    private var player1Email: String? = null
    private var player2Email: String? = null
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameStatusTextView = findViewById(R.id.game_status_text_view)

        player1Email = intent.getStringExtra("player1Email")
        player2Email = intent.getStringExtra("player2Email")

        gameStatusTextView.text = "Player 1: $player1Email\nPlayer 2: $player2Email"

        setupSocket()

        // 게임 시작
        if (player1Email != null && player2Email != null) {
            startGame(player1Email!!, player2Email!!)
        }
    }

    private fun setupSocket() {
        socket = IO.socket("http://172.10.7.80:80")
        socket.on(Socket.EVENT_CONNECT) {
            // 연결 시 로그 메시지 출력
            socket.emit("joinGame", player1Email, player2Email)
        }
        socket.connect()
    }

    private fun startGame(player1: String, player2: String) {
        val json = JSONObject().apply {
            put("player1", player1)
            put("player2", player2)
        }

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/startGame")
            .post(RequestBody.create("application/json".toMediaType(), json.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@GameActivity, "Failed to start game", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                fetchGameStatus(player1, player2)
            }
        })
    }


    private fun fetchGameStatus(player1: String, player2: String) {
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/getGameStatus?player1=$player1&player2=$player2")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@GameActivity, "Failed to fetch game status", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val rounds = jsonResponse.getInt("rounds")
                        val currentRound = jsonResponse.getInt("currentRound")
                        val player1Gold = jsonResponse.getInt("player1Gold")
                        val player2Gold = jsonResponse.getInt("player2Gold")
                        val player1Power = jsonResponse.getInt("player1Power")
                        val player2Power = jsonResponse.getInt("player2Power")
                        val currentCardPower = jsonResponse.getInt("currentCardPower")

                        gameStatusTextView.text = """
                        Rounds: $currentRound / $rounds
                        Player 1 Gold: $player1Gold
                        Player 2 Gold: $player2Gold
                        Player 1 Power: $player1Power
                        Player 2 Power: $player2Power
                        Current Card Power: $currentCardPower
                    """.trimIndent()
                    } catch (e: Exception) {
                        Toast.makeText(this@GameActivity, "Failed to parse game status", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }

    override fun onBackPressed() {
        showExitConfirmationDialog()
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("게임에서 나가겠습니까?")
            .setPositiveButton("네") { dialog, id ->
                leaveGame()
            }
            .setNegativeButton("아니오") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun leaveGame() {
        // 게임 나가기 로직 추가 (필요한 경우)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}
