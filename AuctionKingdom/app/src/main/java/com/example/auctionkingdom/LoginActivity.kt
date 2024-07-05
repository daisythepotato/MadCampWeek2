package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.SignInButton
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private val client = OkHttpClient()

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
                if (account != null) {
                    checkUserInDatabase(account.email ?: "", account.displayName ?: "")
                }
            } catch (e: ApiException) {
                // 로그인 실패 처리
                Toast.makeText(this, "Google Sign-In failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUserInDatabase(email: String, name: String) {
        val json = JSONObject().apply {
            put("email", email)
        }

        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/checkUser")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Server error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val exists = jsonResponse.getBoolean("exists")
                        if (exists) {
                            // 사용자 정보가 DB에 있음
                            val user = jsonResponse.getJSONObject("user")
                            Toast.makeText(this@LoginActivity, "Welcome back, ${user.getString("name")}", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        } else {
                            // 사용자 정보가 DB에 없음
                            val intent = Intent(this@LoginActivity, ProfileSetupActivity::class.java)
                            intent.putExtra("email", email)
                            intent.putExtra("name", name)
                            startActivity(intent)
                        }
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "Error parsing server response: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
