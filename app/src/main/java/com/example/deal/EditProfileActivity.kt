package com.example.deal

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton
import android.widget.*
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
    private val calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        initViews()
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
    }
    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right)
        }
        btnChangePhoto.setOnClickListener {
            // Bounce animation on tap
            val scaleX = ObjectAnimator.ofFloat(btnChangePhoto, "scaleX", 1f, 1.2f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnChangePhoto, "scaleY", 1f, 1.2f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                interpolator = OvershootInterpolator()
                start()
            }
            // TODO: Launch image picker
        }
        cardBirthday.setOnClickListener {
            showDatePicker()
        }
        btnSave.setOnClickListener {
            // Press animation
            val scaleDown = ObjectAnimator.ofFloat(btnSave, "scaleX", 1f, 0.95f)
            val scaleDownY = ObjectAnimator.ofFloat(btnSave, "scaleY", 1f, 0.95f)
            val scaleUp = ObjectAnimator.ofFloat(btnSave, "scaleX", 0.95f, 1f)
            val scaleUpY = ObjectAnimator.ofFloat(btnSave, "scaleY", 0.95f, 1f)
            AnimatorSet().apply {
                play(scaleDown).with(scaleDownY)
                play(scaleUp).with(scaleUpY).after(scaleDown)
                duration = 150
                start()
            }
            // TODO: Save profile data
        }
    }
    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            this,
            R.style.CustomDatePickerTheme,
            { _, year, month, day ->
                calendar.set(year, month, day)
                val format = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                tvBirthday.text = format.format(calendar.time)
                tvBirthday.setTextColor(resources.getColor(android.R.color.black, theme))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }
    private fun playEntranceAnimations() {
        val profileSection = findViewById<View>(R.id.imgProfile).parent.parent as View
        val formCard = findViewById<View>(R.id.etFullName).parent.parent.parent as View
        val saveBtn = findViewById<View>(R.id.btnSave).parent as View
        val views = listOf(profileSection, formCard, saveBtn)
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
        // Floating blob animations
        val blobs = listOf(
            findViewById<View>(android.R.id.content).rootView
        )
        // Subtle infinite floating for background blobs handled via XML animator if needed
    }
}