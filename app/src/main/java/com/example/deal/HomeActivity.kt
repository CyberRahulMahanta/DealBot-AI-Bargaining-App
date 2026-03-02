package com.example.deal

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup

class HomeActivity : AppCompatActivity() {
    private lateinit var tvHours: TextView
    private lateinit var tvMinutes: TextView
    private lateinit var tvSeconds: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)
        tvHours = findViewById(R.id.tvHours)
        tvMinutes = findViewById(R.id.tvMinutes)
        tvSeconds = findViewById(R.id.tvSeconds)
        setupCountdownTimer()
        setupBottomNav()
        setupChipGroup()
        animateEntrance()
    }
    private fun setupCountdownTimer() {
        // 8 hours, 25 minutes, 16 seconds in milliseconds
        val totalMillis = (8 * 3600 + 25 * 60 + 16) * 1000L
        object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60
                tvHours.text = String.format("%02d", hours)
                tvMinutes.text = String.format("%02d", minutes)
                tvSeconds.text = String.format("%02d", seconds)
            }
            override fun onFinish() {
                tvHours.text = "00"
                tvMinutes.text = "00"
                tvSeconds.text = "00"
            }
        }.start()
    }
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> true
                R.id.nav_cart -> true
                R.id.nav_favorite -> true
                R.id.nav_account -> true
                else -> false
            }
        }
    }
    private fun setupChipGroup() {
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroup)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            // Handle category filter selection
        }
    }
    private fun animateEntrance() {
        val bannerCard = findViewById<MaterialCardView>(R.id.bannerCard)
        val flashSaleContainer = findViewById<View>(R.id.flashSaleContainer)
        val views = listOf(bannerCard, flashSaleContainer)
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 40f
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                duration = 500
                startDelay = (index * 150).toLong()
                interpolator = DecelerateInterpolator()
                start()
            }
            ObjectAnimator.ofFloat(view, "translationY", 40f, 0f).apply {
                duration = 500
                startDelay = (index * 150).toLong()
                interpolator = DecelerateInterpolator()
                start()
            }
        }
    }
}