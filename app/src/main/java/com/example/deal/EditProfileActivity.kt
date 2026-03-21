package com.example.deal

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.deal.model.ApiResponse
import com.example.deal.model.UserRequest
import com.example.deal.network.RetrofitClient
import com.example.deal.utils.Constants
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var btnBack: MaterialCardView
    private lateinit var btnChangePhoto: MaterialCardView
    private lateinit var cardBirthday: MaterialCardView
    private lateinit var tvBirthday: TextView
    private lateinit var btnSave: MaterialButton
    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etAddress: EditText
    private lateinit var imgProfile: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private var selectedImageUri: Uri? = null
    private var userUid: String? = null
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initViews()
        userUid = intent.getStringExtra("firebase_uid")

        if (userUid == null) {
            Toast.makeText(this, "User ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedImageUri = result.data?.data
                imgProfile.setImageURI(null)
                imgProfile.setImageURI(selectedImageUri)
                imgProfile.imageTintList = null
            }
        }

        loadUserData(userUid!!)
        setupListeners()
        playEntranceAnimations()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        cardBirthday = findViewById(R.id.cardBirthday)
        tvBirthday = findViewById(R.id.tvBirthday)
        btnSave = findViewById(R.id.btnSave)
        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        etAddress = findViewById(R.id.etAddress)
        imgProfile = findViewById(R.id.imgProfile)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        cardBirthday.setOnClickListener { showDatePicker() }

        btnChangePhoto.setOnClickListener {
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(btnChangePhoto, "scaleX", 1f, 1.2f, 1f),
                    ObjectAnimator.ofFloat(btnChangePhoto, "scaleY", 1f, 1.2f, 1f)
                )
                duration = 300
                interpolator = OvershootInterpolator()
                start()
            }
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        btnSave.setOnClickListener { updateUserProfile() }
    }

    private fun updateUserProfile() {
        val name = etFullName.text.toString().trim()
        if (name.isEmpty()) {
            etFullName.error = "Enter name"
            return
        }

        val imagePath = selectedImageUri?.let { "uploads/${userUid}.jpg" }

        val request = UserRequest(
            name = name,
            email = etEmail.text.toString(),
            phone = etPhone.text.toString().ifEmpty { null },
            birthday = if (tvBirthday.text.toString() == "Select Birthday") null else tvBirthday.text.toString(),
            address = etAddress.text.toString().ifEmpty { null },
            profile_image = imagePath
        )

        btnSave.isEnabled = false
        btnSave.text = "Saving..."

        userUid?.let { uid ->
            RetrofitClient.api.updateUser(uid, request)
                .enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        btnSave.isEnabled = true
                        btnSave.text = "Save"
                        if (response.isSuccessful && response.body()?.success == true) {
                            uploadProfileImage() // upload image after updating
                        } else {
                            Toast.makeText(this@EditProfileActivity, "Update failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        btnSave.isEnabled = true
                        btnSave.text = "Save"
                        Toast.makeText(this@EditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun uploadProfileImage() {
        if (selectedImageUri == null) {
            sendUpdateResult()
            return
        }

        try {
            val inputStream = contentResolver.openInputStream(selectedImageUri!!)
            val file = File(cacheDir, "profile.jpg")
            file.outputStream().use { output -> inputStream?.copyTo(output) }

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            userUid?.let { uid ->
                RetrofitClient.api.uploadProfileImage(uid, body)
                    .enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                            Toast.makeText(this@EditProfileActivity, "Profile Updated ✅", Toast.LENGTH_SHORT).show()
                            sendUpdateResult()
                        }

                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Toast.makeText(this@EditProfileActivity, "Upload failed: ${t.message}", Toast.LENGTH_LONG).show()
                            sendUpdateResult()
                        }
                    })
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendUpdateResult() {
        val intent = Intent()
        intent.putExtra("updated", true)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun loadUserData(uid: String) {
        RetrofitClient.api.getUser(uid)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val user = response.body()?.data
                        etFullName.setText(user?.name ?: "")
                        etEmail.setText(user?.email ?: "")
                        etPhone.setText(user?.phone ?: "")
                        etAddress.setText(user?.address ?: "")
                        tvBirthday.text = user?.birthday ?: "Select Birthday"

                        val imagePath = user?.profile_image
                        if (!imagePath.isNullOrEmpty()) {
                            Glide.with(this@EditProfileActivity)
                                .load(Constants.BASE_URL + imagePath + "?${System.currentTimeMillis()}")
                                .placeholder(R.drawable.ic_person)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(imgProfile)
                        } else {
                            imgProfile.setImageResource(R.drawable.ic_person)
                        }

                        etEmail.isFocusable = false
                        etEmail.isClickable = false
                        etEmail.isCursorVisible = false
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@EditProfileActivity, "API Error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                val format = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                tvBirthday.text = format.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun playEntranceAnimations() {
        val views = listOf(findViewById<View>(R.id.imgProfile), findViewById<View>(R.id.etFullName), findViewById<View>(R.id.btnSave))
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 60f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay((index * 120).toLong())
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }
}