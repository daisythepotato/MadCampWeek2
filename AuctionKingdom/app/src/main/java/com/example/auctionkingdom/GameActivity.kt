package com.example.auctionkingdom

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val player1Email = intent.getStringExtra("player1Email") ?: "Player1"
        val player2Email = intent.getStringExtra("player2Email") ?: "Player2"

        val player1TextView: TextView = findViewById(R.id.player1_text_view)
        val player2TextView: TextView = findViewById(R.id.player2_text_view)

        player1TextView.text = player1Email
        player2TextView.text = player2Email
    }
}