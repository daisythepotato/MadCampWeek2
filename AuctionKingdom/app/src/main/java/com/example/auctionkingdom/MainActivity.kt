package com.example.auctionkingdom

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val email = intent.getStringExtra("email")
        val nickname = intent.getStringExtra("nickname")
        val kingdomName = intent.getStringExtra("kingdomName")

        val profileNameTextView: TextView = findViewById(R.id.profile_name)
        val kingdomNameTextView: TextView = findViewById(R.id.kingdom_name)

        profileNameTextView.text = nickname
        kingdomNameTextView.text = kingdomName

        // 프로필 사진 설정
        val profileImageView: ImageView = findViewById(R.id.profile_image)
        profileImageView.setImageResource(R.drawable.profile_placeholder) // 실제 이미지를 사용하려면 이 부분을 변경합니다.
    }
}
