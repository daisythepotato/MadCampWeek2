package com.example.auctionkingdom

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class GameActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var roundTextView: TextView
    private lateinit var cardImageView: ImageView
    private lateinit var cardPowerTextView: TextView
    private lateinit var bidEditText: EditText
    private lateinit var submitBidButton: Button
    private lateinit var playersStatusTextView: TextView
    private lateinit var socket: Socket
    private var roomCode: String? = null
    private var email: String? = null
    private var roundNumber = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        roundTextView = findViewById(R.id.round_text_view)
        cardImageView = findViewById(R.id.card_image_view)
        cardPowerTextView = findViewById(R.id.card_power_text_view)
        bidEditText = findViewById(R.id.bid_edit_text)
        submitBidButton = findViewById(R.id.submit_bid_button)
        playersStatusTextView = findViewById(R.id.players_status_text_view)
        roomCode = intent.getStringExtra("roomCode")
        email = intent.getStringExtra("email")

        submitBidButton.setOnClickListener {
            val bid = bidEditText.text.toString().toIntOrNull()
            if (bid != null && bid > 0) {
                submitBid(bid)
            } else {
                Toast.makeText(this, "Please enter a valid bid", Toast.LENGTH_SHORT).show()
            }
        }

        // 소켓 설정 및 이벤트 처리
        setupSocket()
    }

    private fun setupSocket() {
        socket = IO.socket("http://172.10.7.80:80")
        socket.on(Socket.EVENT_CONNECT) {
            socket.emit("joinRoom", roomCode)
        }
        socket.on("roundUpdate") { args ->
            val data = args[0] as JSONObject
            updateRound(data)
        }
        socket.connect()
    }

    private fun submitBid(bid: Int) {
        val json = JSONObject().apply {
            put("roomCode", roomCode)
            put("email", email)
            put("bid", bid)
        }

        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/submitBid")
            .post(RequestBody.create("application/json".toMediaType(), json.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@GameActivity, "Failed to submit bid", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    Toast.makeText(this@GameActivity, "Bid submitted", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun updateRound(data: JSONObject) {
        val newRoundNumber = data.getInt("roundNumber")
        val cardPower = data.getInt("cardPower")
        val player1Gold = data.getInt("player1Gold")
        val player2Gold = data.getInt("player2Gold")

        runOnUiThread {
            roundTextView.text = "Round $newRoundNumber"
            cardPowerTextView.text = "Card Power: $cardPower"
            playersStatusTextView.text = "Player 1: $player1Gold gold\nPlayer 2: $player2Gold gold"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }
}
