package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var logoImage: ImageView
    private lateinit var stampImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        logoImage = findViewById(R.id.logo_image)
        stampImage = findViewById(R.id.stamp_image)

        Handler().postDelayed({
            stampImage.visibility = ImageView.VISIBLE
            val stampAnimation = AnimationUtils.loadAnimation(this, R.anim.stamp_animation)
            stampImage.startAnimation(stampAnimation)

            Handler().postDelayed({
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }, 1000)
        }, 2000)
    }
}
