package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Google Sign-In 옵션 구성
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Google Sign-In 버튼 클릭 리스너
        val googleSignInButton: SignInButton = findViewById(R.id.btn_google_sign_in)
        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        // Guest 버튼 클릭 리스너
        val guestButton: Button = findViewById(R.id.btn_guest)
        guestButton.setOnClickListener {
            // 게스트 로그인 처리
            Toast.makeText(this, "Logged in as Guest", Toast.LENGTH_SHORT).show()
            // 다음 액티비티로 이동
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                // 로그인 성공 처리
                Toast.makeText(this, "Logged in as ${account.displayName}", Toast.LENGTH_SHORT).show()
                // 다음 액티비티로 이동
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } catch (e: ApiException) {
                // 로그인 실패 처리
                Toast.makeText(this, "Google Sign-In failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
