package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val stampImageView: ImageView = findViewById(R.id.stamp_image)

        Handler(Looper.getMainLooper()).postDelayed({
            stampImageView.visibility = ImageView.VISIBLE

            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }, 1000)
        }, 1000)
    }
}
