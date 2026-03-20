package com.example.deal
import com.example.deal.R
import com.example.deal.model.ApiResponse
import com.example.deal.network.RetrofitClient
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.deal.EditProfileActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvBirthday: TextView
    private lateinit var tvLocation: TextView
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnDeleteProfile: MaterialButton

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize views
        imgProfile = findViewById(R.id.imgProfile)
        tvName = findViewById(R.id.tvName)
        tvPhone = findViewById(R.id.tvPhone)
        tvEmail = findViewById(R.id.tvEmail)
        tvBirthday = findViewById(R.id.tvBirthday)
        tvLocation = findViewById(R.id.tvLocation)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnDeleteProfile = findViewById(R.id.btnDeleteProfile)

        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val uid = currentUser.uid

        // Load user from FastAPI
        loadUserData(uid)

        // Edit profile button
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Delete profile (not implemented yet in backend)
        btnDeleteProfile.setOnClickListener {
            deleteUser(uid)
        }
    }

    private fun loadUserData(uid: String) {

        RetrofitClient.api.getUser(uid)
            .enqueue(object : Callback<ApiResponse> {

                override fun onResponse(
                    call: Call<ApiResponse>,
                    response: Response<ApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {

                        val user = response.body()?.data

                        tvName.text = user?.name ?: ""
                        tvEmail.text = user?.email ?: ""

                        // Backend doesn't provide these yet
                        tvPhone.text = "Not available"
                        tvBirthday.text = "Not available"
                        tvLocation.text = "Not available"

                        // Default profile image
                        imgProfile.setImageResource(android.R.drawable.ic_menu_myplaces)

                    } else {
                        Toast.makeText(
                            this@UserProfileActivity,
                            "User not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(
                        this@UserProfileActivity,
                        "API Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun deleteUser(uid: String) {
        Toast.makeText(
            this,
            "Delete API not implemented yet",
            Toast.LENGTH_SHORT
        ).show()
    }
}