package com.example.auctionkingdom

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private val client = OkHttpClient()
    private var guestNickname: String? = null

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
            signOutAndSignIn()
        }

        // Guest 버튼 클릭 리스너
        val guestButton: Button = findViewById(R.id.btn_guest)
        guestButton.setOnClickListener {
            showGuestDialog()
        }
    }

    private fun showGuestDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Guest Login")
        builder.setMessage("Do you want to continue as a guest?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            dialog.dismiss()
            addGuestAccount()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun addGuestAccount() {
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/addGuest")
            .post(RequestBody.create(null, ""))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Failed to add guest: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val guest = jsonResponse.getJSONObject("guest")
                        guestNickname = guest.getString("nickname")
                        val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                            putExtra("email", "")
                            putExtra("nickname", guest.getString("nickname"))
                            putExtra("kingdomName", guest.getString("kingdomName"))
                        }
                        Toast.makeText(this@LoginActivity, "Logged in as ${guest.getString("nickname")}", Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "Error parsing server response: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun signOutAndSignIn() {
        googleSignInClient.signOut().addOnCompleteListener(this) {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    // 구글 로그인 성공
                    checkUserInDatabase(account)
                }
            } catch (e: ApiException) {
                // 로그인 실패 처리
                Toast.makeText(this, "Google Sign-In failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUserInDatabase(account: GoogleSignInAccount) {
        val json = JSONObject().apply {
            put("email", account.email)
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
                            val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                                putExtra("email", user.getString("email"))
                                putExtra("nickname", user.getString("nickname"))
                                putExtra("kingdomName", user.getString("kingdomName"))
                            }
                            Toast.makeText(this@LoginActivity, "Welcome back, ${user.getString("nickname")}", Toast.LENGTH_SHORT).show()
                            startActivity(intent)
                        } else {
                            val intent = Intent(this@LoginActivity, ProfileSetupActivity::class.java).apply {
                                putExtra("email", account.email)
                                putExtra("name", account.displayName)
                            }
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

    override fun onDestroy() {
        super.onDestroy()
        guestNickname?.let {
            deleteGuestAccount(it)
        }
    }

    private fun deleteGuestAccount(nickname: String) {
        val json = JSONObject().apply {
            put("nickname", nickname)
        }

        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/deleteGuest")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Failed to delete guest: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Guest deleted successfully", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
