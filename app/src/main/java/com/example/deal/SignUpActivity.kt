package com.example.deal

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.deal.model.ApiResponse
import com.example.deal.model.CreateUserRequest
import com.example.deal.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        auth = FirebaseAuth.getInstance()

        val nameInput = findViewById<TextInputEditText>(R.id.nameInput)
        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val btnSignup = findViewById<MaterialButton>(R.id.signUpButton)

        btnSignup.setOnClickListener {

            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // =========================
            // Validation
            // =========================

            if (name.isEmpty()) {
                nameInput.error = "Name required"
                nameInput.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                emailInput.error = "Email required"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Enter valid email"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput.error = "Password required"
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6) {
                passwordInput.error = "Minimum 6 characters required"
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            // =========================
            // Firebase Signup
            // =========================

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        val user = auth.currentUser

                        if (user != null) {
                            // 🔥 Create user in backend
                            createUserInBackend(
                                uid = user.uid,
                                email = user.email ?: "",
                                name = name
                            )
                        }

                    } else {

                        Toast.makeText(
                            this,
                            "Signup Failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    // =========================
    // Backend User Creation
    // =========================

    private fun createUserInBackend(uid: String, email: String, name: String) {

        val newUser = CreateUserRequest(
            firebase_uid = uid,
            name = name,
            email = email,
            phone = null,
            birthday = null,
            address = null
        )

        RetrofitClient.api.createUser(newUser)
            .enqueue(object : Callback<ApiResponse> {

                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {

                    if (response.isSuccessful && response.body()?.success == true) {

                        Toast.makeText(
                            this@SignUpActivity,
                            "Account Created Successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        // 🔥 Go to Edit Profile
                        startActivity(Intent(this@SignUpActivity, EditProfileActivity::class.java))
                        finish()

                    } else {
                        Toast.makeText(
                            this@SignUpActivity,
                            "User creation failed: ${response.body()?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {

                    Toast.makeText(
                        this@SignUpActivity,
                        "API Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}