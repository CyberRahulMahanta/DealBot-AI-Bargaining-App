package com.example.deal

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.deal.model.ApiResponse
import com.example.deal.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var btnLogin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        btnLogin = findViewById(R.id.btnLogin)

        // =========================
        // 🔥 Login Click with Animation
        // =========================
        btnLogin.setOnClickListener {

            // 🔥 Press Animation
            btnLogin.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(80)
                .withEndAction {
                    btnLogin.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(80)
                        .start()
                }
                .start()

            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Validation
            if (email.isEmpty()) {
                emailInput.error = "Email is required"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Enter valid email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput.error = "Password required"
                return@setOnClickListener
            }

            // 🔥 Disable button + loading
            setLoadingState(true)

            // Firebase Login
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        handleUserLogin()
                    } else {
                        setLoadingState(false)

                        Toast.makeText(
                            this,
                            "No Internet Connection..!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    // =========================
    // 🔥 Backend User Check
    // =========================
    private fun handleUserLogin() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            setLoadingState(false)
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = user.uid

        RetrofitClient.api.getUser(uid)
            .enqueue(object : Callback<ApiResponse> {

                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {

                    if (response.isSuccessful && response.body()?.success == true) {

                        Toast.makeText(
                            this@LoginActivity,
                            "Login Successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()

                    } else {
                        setLoadingState(false)

                        Toast.makeText(
                            this@LoginActivity,
                            "User not found. Please sign up first.",
                            Toast.LENGTH_LONG
                        ).show()

                        auth.signOut()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    setLoadingState(false)

                    Toast.makeText(
                        this@LoginActivity,
                        "Connect to Rahul's laptop",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // =========================
    // 🔥 Loading State Handler
    // =========================
    private fun setLoadingState(isLoading: Boolean) {
        btnLogin.isEnabled = !isLoading
        btnLogin.text = if (isLoading) "Logging in..." else "Login"
    }
}