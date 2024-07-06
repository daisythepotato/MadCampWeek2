package com.example.auctionkingdom

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val email = intent.getStringExtra("email")
        val nickname = intent.getStringExtra("nickname")
        val kingdomName = intent.getStringExtra("kingdomName")

        val emailTextView: TextView = findViewById(R.id.text_email)
        val nicknameTextView: TextView = findViewById(R.id.text_nickname)
        val kingdomNameTextView: TextView = findViewById(R.id.text_kingdom_name)

        emailTextView.text = "Email: $email"
        nicknameTextView.text = "Nickname: $nickname"
        kingdomNameTextView.text = "Kingdom Name: $kingdomName"
    }
}
