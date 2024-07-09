package com.example.auctionkingdom

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        socket.emit("joinRoom", "$player1Email-$player2Email")


        // 게임 시작
        if (player1Email != null && player2Email != null) {
            startGame(player1Email!!, player2Email!!)
        }

        placeBetButton.setOnClickListener {
            val betAmount = betAmountEditText.text.toString().toIntOrNull()
            if (betAmount != null && currentEmail != null) {
                placeBet(player1Email!!, player2Email!!, currentEmail!!, betAmount)
            } else {
                Toast.makeText(this, "Invalid bet amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun placeBet(player1: String, player2: String, playerEmail: String, betAmount: Int) {
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
            socket.emit("joinRoom", "$player1Email-$player2Email")
        }
        socket.on("roundResult") { args ->
            runOnUiThread {
                val data = args[0] as JSONObject
                val player1Gold = data.getInt("player1Gold")
                val player2Gold = data.getInt("player2Gold")
                val player1Power = data.getInt("player1Power")
                val player2Power = data.getInt("player2Power")
                val currentCardName = data.getString("currentCardName")
                val currentCardImage = data.getString("currentCardImage")
                val currentCardPower = data.getInt("currentCardPower")
                val currentRound = data.getInt("currentRound")
                val player1Bet = data.getInt("player1Bet")
                val player2Bet = data.getInt("player2Bet")

                gameStatusTextView.text = "Card: $currentCardName\nPower: $currentCardPower\nRound: $currentRound / 15\nPlayer 1 Gold: $player1Gold\nPlayer 2 Gold: $player2Gold\nPlayer 1 Power: $player1Power\nPlayer 2 Power: $player2Power"

                val resourceId = resources.getIdentifier(currentCardImage.replace(".png", ""), "drawable", packageName)
                cardImageView.setImageResource(resourceId)

                AlertDialog.Builder(this)
                    .setTitle("Round ${currentRound-1} Result")
                    .setMessage("Player 1 Bet: $player1Bet\nPlayer 2 Bet: $player2Bet\n")
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                    })
                    .show()
            }
        }
        socket.on("gameOver") { args ->
            runOnUiThread {
                val data = args[0] as JSONObject
                val message = data.getString("message")
                val intent = Intent(this, GameOverActivity::class.java).apply {
                    putExtra("message", message)
                }
                startActivity(intent)
                finish()
            }
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
                        val currentRound = jsonResponse.getInt("currentRound")
                        val player1Gold = jsonResponse.getInt("player1Gold")
                        val player2Gold = jsonResponse.getInt("player2Gold")
                        val player1Power = jsonResponse.getInt("player1Power")
                        val player2Power = jsonResponse.getInt("player2Power")
                        val currentCardName = jsonResponse.getString("currentCardName")
                        val currentCardPower = jsonResponse.getInt("currentCardPower")
                        val currentCardImage = jsonResponse.getString("currentCardImage")

                        gameStatusTextView.text = "Card: $currentCardName\nPower: $currentCardPower\nRound: $currentRound / 15\nPlayer 1 Gold: $player1Gold\nPlayer 2 Gold: $player2Gold\nPlayer 1 Power: $player1Power\nPlayer 2 Power: $player2Power"


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
        AlertDialog.Builder(this)
            .setMessage("Do you really want to exit the game?")
            .setPositiveButton("Yes") { _, _ ->
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
