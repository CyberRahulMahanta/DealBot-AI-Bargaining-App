package com.example.deal

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deal.adapter.FlashSaleAdapter
import com.example.deal.model.Product
import com.example.deal.network.ApiService
import com.example.deal.network.RetrofitClient
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var tvHours: TextView
    private lateinit var tvMinutes: TextView
    private lateinit var tvSeconds: TextView
    private lateinit var flashSaleRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Countdown TextViews
        tvHours = findViewById(R.id.tvHours)
        tvMinutes = findViewById(R.id.tvMinutes)
        tvSeconds = findViewById(R.id.tvSeconds)

        // RecyclerView
        flashSaleRecyclerView = findViewById(R.id.flashSaleRecyclerView)
        flashSaleRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Start API loading
        loadProducts()

        // Other setups
        setupCountdownTimer()
        setupBottomNav()
        setupChipGroup()
        animateEntrance()
    }

    // -------------------------------
    // API CALL
    // -------------------------------
    private fun loadProducts() {

        val api = RetrofitClient.api   // ✅ FIXED

        api.getProducts()
            .enqueue(object : Callback<List<Product>> {

                override fun onResponse(
                    call: Call<List<Product>>,
                    response: Response<List<Product>>
                ) {
                    if (response.isSuccessful) {
                        val products = response.body()
                        if (products != null) {
                            flashSaleRecyclerView.adapter =
                                FlashSaleAdapter(products)
                        }
                    }
                }

                override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                    Toast.makeText(
                        this@HomeActivity,
                        "Failed: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // -------------------------------
    // COUNTDOWN TIMER
    // -------------------------------
    private fun setupCountdownTimer() {

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

    // -------------------------------
    // BOTTOM NAVIGATION
    // -------------------------------
    private fun setupBottomNav() {

        val tabHome = findViewById<LinearLayout>(R.id.tabHome)
        val tabSearch = findViewById<LinearLayout>(R.id.tabSearch)
        val tabCart = findViewById<LinearLayout>(R.id.tabCart)
        val tabFavorite = findViewById<LinearLayout>(R.id.tabFavorite)
        val tabProfile = findViewById<LinearLayout>(R.id.tabProfile)

        tabHome.setOnClickListener {
            // Already on Home
        }

        tabSearch.setOnClickListener {
            Toast.makeText(this, "Search Clicked", Toast.LENGTH_SHORT).show()
        }

        tabCart.setOnClickListener {
            Toast.makeText(this, "Cart Clicked", Toast.LENGTH_SHORT).show()
        }

        tabFavorite.setOnClickListener {
            Toast.makeText(this, "Favorite Clicked", Toast.LENGTH_SHORT).show()
        }
        tabProfile.setOnClickListener {
            Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }


    }

    // -------------------------------
    // CHIP GROUP
    // -------------------------------
    private fun setupChipGroup() {

        val chipGroup = findViewById<ChipGroup>(R.id.chipGroup)

        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            // Future: Filter products here
        }
    }

    // -------------------------------
    // ANIMATION
    // -------------------------------
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