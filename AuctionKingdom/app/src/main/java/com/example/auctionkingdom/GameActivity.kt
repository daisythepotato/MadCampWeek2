package com.example.auctionkingdom

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
    private var currentEmail: String? = null
    private val client = OkHttpClient()
    private lateinit var cardImageView: ImageView
    private lateinit var betAmountEditText: EditText
    private lateinit var placeBetButton: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameStatusTextView = findViewById(R.id.game_status_text_view)
        cardImageView = findViewById(R.id.card_image_view)
        betAmountEditText = findViewById(R.id.bet_amount_edit_text)
        placeBetButton = findViewById(R.id.place_bet_button)

        player1Email = intent.getStringExtra("player1Email")
        player2Email = intent.getStringExtra("player2Email")
        currentEmail = intent.getStringExtra("currentEmail")

        gameStatusTextView.text = "Player 1: $player1Email\nPlayer 2: $player2Email"

        setupSocket()

        // 게임 시작
        if (player1Email != null && player2Email != null) {
            startGame(player1Email!!, player2Email!!)
        }

        placeBetButton.setOnClickListener {
            val betAmount = betAmountEditText.text.toString().toIntOrNull()
            if (betAmount != null && player1Email != null && player2Email != null) {
                placeBet(player1Email!!, player2Email!!, betAmount)
            } else {
                Toast.makeText(this, "Invalid bet amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun placeBet(player1: String, player2: String, betAmount: Int) {
        val playerEmail = if (player1 == currentEmail) player1 else player2
        val json = JSONObject().apply {
            put("player1", player1)
            put("player2", player2)
            put("playerEmail", playerEmail)
            put("betAmount", betAmount)
        }

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/placeBet")
            .post(RequestBody.create("application/json".toMediaType(), json.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@GameActivity, "Failed to place bet", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val success = jsonResponse.getBoolean("success")
                        if (success) {
                            Toast.makeText(this@GameActivity, "Bet placed successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@GameActivity, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@GameActivity, "Failed to parse response", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
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
                        val currentCardImage = jsonResponse.getString("currentCardImage")

                        gameStatusTextView.text = """
                        Rounds: $currentRound / $rounds
                        Player 1 Gold: $player1Gold
                        Player 2 Gold: $player2Gold
                        Player 1 Power: $player1Power
                        Player 2 Power: $player2Power
                        Current Card Power: $currentCardPower
                    """.trimIndent()

                        // 카드 이미지 설정
                        val imageResId = resources.getIdentifier(currentCardImage.replace(".png", ""), "drawable", packageName)
                        cardImageView.setImageResource(imageResId)

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