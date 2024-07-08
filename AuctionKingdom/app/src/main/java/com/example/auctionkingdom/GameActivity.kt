package com.example.auctionkingdom

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class GameActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var roundTextView: TextView
    private lateinit var cardTextView: TextView
    private lateinit var cardImageView: ImageView
    private lateinit var player1TextView: TextView
    private lateinit var player2TextView: TextView
    private lateinit var player1GoldTextView: TextView
    private lateinit var player2GoldTextView: TextView
    private lateinit var player1PowerTextView: TextView
    private lateinit var player2PowerTextView: TextView
    private lateinit var betAmountInput: EditText
    private lateinit var betButton: Button
    private var roomCode: String? = null
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        roundTextView = findViewById(R.id.round_text_view)
        cardTextView = findViewById(R.id.card_text_view)
        cardImageView = findViewById(R.id.card_image_view)
        player1TextView = findViewById(R.id.player1_text_view)
        player2TextView = findViewById(R.id.player2_text_view)
        player1GoldTextView = findViewById(R.id.player1_gold_text_view)
        player2GoldTextView = findViewById(R.id.player2_gold_text_view)
        player1PowerTextView = findViewById(R.id.player1_power_text_view)
        player2PowerTextView = findViewById(R.id.player2_power_text_view)
        betAmountInput = findViewById(R.id.bet_amount_input)
        betButton = findViewById(R.id.bet_button)

        roomCode = intent.getStringExtra("roomCode")
        email = intent.getStringExtra("email")

        betButton.setOnClickListener {
            placeBet()
        }

        if (roomCode != null) {
            fetchGameState(roomCode!!)
        }
    }

    private fun fetchGameState(code: String) {
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/getGameState?code=$code")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@GameActivity, "Failed to fetch game state", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val players = jsonResponse.getJSONArray("players")

                        roundTextView.text = "Round: ${jsonResponse.getInt("currentRound")}"
                        val currentCard = jsonResponse.getJSONObject("currentCard")
                        cardTextView.text = "Card: ${currentCard.getString("name")} (Power: ${currentCard.getInt("power")})"
                        val cardImageRes = resources.getIdentifier(currentCard.getString("image"), "drawable", packageName)
                        cardImageView.setImageResource(cardImageRes)

                        val player1 = players.getJSONObject(0)
                        player1TextView.text = player1.getString("nickname")
                        player1GoldTextView.text = "Gold: ${player1.getInt("gold")}"
                        player1PowerTextView.text = "Power: ${player1.getInt("nationalPower")}"

                        val player2 = players.getJSONObject(1)
                        player2TextView.text = player2.getString("nickname")
                        player2GoldTextView.text = "Gold: ${player2.getInt("gold")}"
                        player2PowerTextView.text = "Power: ${player2.getInt("nationalPower")}"
                    } catch (e: Exception) {
                        Toast.makeText(this@GameActivity, "Failed to parse game state", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun placeBet() {
        val betAmount = betAmountInput.text.toString().toIntOrNull()
        if (betAmount == null || betAmount <= 0) {
            Toast.makeText(this, "Invalid bet amount", Toast.LENGTH_SHORT).show()
            return
        }

        val json = JSONObject().apply {
            put("code", roomCode)
            put("email", email)
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
                runOnUiThread {
                    Toast.makeText(this@GameActivity, "Bet placed successfully", Toast.LENGTH_SHORT).show()
                    fetchGameState(roomCode!!)
                }
            }
        })
    }
}
