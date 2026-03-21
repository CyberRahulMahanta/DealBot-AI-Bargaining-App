package com.example.deal

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.deal.model.ApiResponse
import com.example.deal.network.RetrofitClient
import com.example.deal.utils.Constants
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

    private lateinit var btnBack: com.google.android.material.card.MaterialCardView

    private val auth = FirebaseAuth.getInstance()

    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data?.getBooleanExtra("updated", false) == true) {
            // Reload user profile from backend
            loadUserData(auth.currentUser!!.uid)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

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

        // Load user data from backend
        loadUserData(uid)

        // Edit profile button
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("firebase_uid", uid)
            editProfileLauncher.launch(intent)
        }

        // Delete profile button (API not implemented)
        btnDeleteProfile.setOnClickListener {
            Toast.makeText(this, "Delete API not implemented yet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData(uid: String) {
        RetrofitClient.api.getUser(uid)
            .enqueue(object : Callback<ApiResponse> {

                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val user = response.body()?.data

                        tvName.text = user?.name ?: ""
                        tvEmail.text = user?.email ?: ""
                        tvPhone.text = user?.phone ?: ""
                        tvBirthday.text = user?.birthday ?: ""
                        tvLocation.text = user?.address ?: ""

                        val imagePath = user?.profile_image
                        if (!imagePath.isNullOrEmpty()) {
                            Glide.with(this@UserProfileActivity).clear(imgProfile)
                            Glide.with(this@UserProfileActivity)
                                .load(Constants.BASE_URL + imagePath + "?${System.currentTimeMillis()}")
                                .placeholder(R.drawable.ic_person)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(imgProfile)
                        } else {
                            imgProfile.setImageResource(R.drawable.ic_person)
                        }

                    } else {
                        Toast.makeText(this@UserProfileActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@UserProfileActivity, "API Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}