package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class GameOverActivity : AppCompatActivity() {

    private lateinit var backgroundImageView: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var playerNameTextView: TextView
    private lateinit var playerKingdomNameTextView: TextView
    private lateinit var playerPowerTextView: TextView
    private lateinit var playerGoldTextView: TextView
    private lateinit var returnToMainButton: ImageButton
    private lateinit var profileImageName: String
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        backgroundImageView = findViewById(R.id.background_image)
        resultTextView = findViewById(R.id.result_text_view)
        playerNameTextView = findViewById(R.id.player_name_text_view)
        playerKingdomNameTextView = findViewById(R.id.player_kingdom_name_text_view)
        playerPowerTextView = findViewById(R.id.player_power_text_view)
        playerGoldTextView = findViewById(R.id.player_gold_text_view)
        returnToMainButton = findViewById(R.id.return_to_main_button)

        val currentEmail = intent.getStringExtra("currentEmail")
        val winner = intent.getStringExtra("winner")
        val loser = intent.getStringExtra("loser")
        val winnerPower = intent.getIntExtra("winnerPower",0)
        val loserPower = intent.getIntExtra("loserPower",0)
        val winnerGold = intent.getIntExtra("winnerGold",0)
        val loserGold = intent.getIntExtra("loserGold",0)

        profileImageName = "ttt"
        fetchUserInfo(currentEmail, currentEmail, winner, winnerPower, loserPower, winnerGold, loserGold)


        returnToMainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun fetchUserInfo(email: String?, currentEmail: String?, winner: String?, winnerPower: Int, loserPower: Int, winnerGold: Int, loserGold: Int) {
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/getUser?email=$email")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@GameOverActivity, "Failed to fetch user info", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val name = jsonResponse.getString("nickname")
                        val kingdomName = jsonResponse.getString("kingdomName")
                        playerNameTextView.text = name
                        playerKingdomNameTextView.text = kingdomName

                        profileImageName = jsonResponse.getString("profileImage")
                        if(currentEmail == winner){
                            resultTextView.text="You Win!!"
                            playerPowerTextView.text="내 국력 : $winnerPower"
                            playerGoldTextView.text="남은 돈 : $winnerGold"
                            if(profileImageName == "profile_image_1"){
                                backgroundImageView.setImageResource(R.drawable.win_background1)
                            } else if(profileImageName == "profile_image_2"){
                                backgroundImageView.setImageResource(R.drawable.win_background2)
                            } else if(profileImageName == "profile_image_3"){
                                backgroundImageView.setImageResource(R.drawable.win_background3)
                            } else if(profileImageName == "profile_image_4"){
                                backgroundImageView.setImageResource(R.drawable.win_background4)
                            }
                        } else{
                            resultTextView.text="You Lose..."
                            playerPowerTextView.text="내 국력 : $loserPower"
                            playerGoldTextView.text="남은 돈 : $loserGold"
                            if(profileImageName == "profile_image_1"){
                                backgroundImageView.setImageResource(R.drawable.lose_background1)
                            } else if(profileImageName == "profile_image_2"){
                                backgroundImageView.setImageResource(R.drawable.lose_background2)
                            } else if(profileImageName == "profile_image_3"){
                                backgroundImageView.setImageResource(R.drawable.lose_background3)
                            } else if(profileImageName == "profile_image_4"){
                                backgroundImageView.setImageResource(R.drawable.lose_background4)
                            }
                        }

                    } catch (e: Exception) {
                        Toast.makeText(this@GameOverActivity, "Failed to parse user info", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
