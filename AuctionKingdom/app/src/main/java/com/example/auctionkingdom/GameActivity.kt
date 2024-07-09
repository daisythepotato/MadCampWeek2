package com.example.auctionkingdom

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
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
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameStatusTextView = findViewById(R.id.game_status_text_view)

        player1Email = intent.getStringExtra("player1Email")
        player2Email = intent.getStringExtra("player2Email")

        gameStatusTextView.text = "Player 1: $player1Email\nPlayer 2: $player2Email"

        setupSocket()
    }

    private fun setupSocket() {
        socket = IO.socket("http://172.10.7.80:80")
        socket.on(Socket.EVENT_CONNECT) {
            // 연결 시 로그 메시지 출력
            socket.emit("joinGame", player1Email, player2Email)
        }
        socket.connect()
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
