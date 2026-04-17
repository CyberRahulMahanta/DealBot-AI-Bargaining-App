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
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.deal.adapter.FlashSaleAdapter
import com.example.deal.model.Product
import com.example.deal.model.UnreadCountResponse
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
    private lateinit var tvNotificationBadge: TextView

    private var userId: String = ""
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val btnNotification = findViewById<MaterialCardView>(R.id.btnNotification)
        tvNotificationBadge = findViewById(R.id.tvNotificationBadge)

        btnNotification.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }

        tvHours = findViewById(R.id.tvHours)
        tvMinutes = findViewById(R.id.tvMinutes)
        tvSeconds = findViewById(R.id.tvSeconds)

        flashSaleRecyclerView = findViewById(R.id.flashSaleRecyclerView)
        setupFlashSaleRecyclerView()

        loadProducts()
        setupCountdownTimer()
        setupBottomNav()
        setupChipGroup()
        animateEntrance()

        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            userId = firebaseUser.uid
        }

        if (userId.isNotEmpty()) {
            loadUnreadCount()
        }
    }

    override fun onResume() {
        super.onResume()
        if (userId.isNotEmpty()) {
            loadUnreadCount()
        }
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        countDownTimer = null
        super.onDestroy()
    }

    private fun setupFlashSaleRecyclerView() {
        val horizontalLayoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        ).apply {
            initialPrefetchItemCount = 6
        }

        flashSaleRecyclerView.apply {
            layoutManager = horizontalLayoutManager
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            setItemViewCacheSize(12)
            itemAnimator = null
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        }
    }

    // -------------------------------
    // API CALL
    // -------------------------------
    private fun loadProducts() {
        RetrofitClient.api.getProducts()
            .enqueue(object : Callback<List<Product>> {
                override fun onResponse(
                    call: Call<List<Product>>,
                    response: Response<List<Product>>
                ) {
                    if (response.isSuccessful) {
                        val products = response.body()
                        if (products != null) {
                            flashSaleRecyclerView.adapter = FlashSaleAdapter(products)
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

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(totalMillis, 1000) {

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
        val tabOrders = findViewById<LinearLayout>(R.id.tabOrders)
        val tabProfile = findViewById<LinearLayout>(R.id.tabProfile)

        tabHome.setOnClickListener {
            // Already on Home
        }

        tabSearch.setOnClickListener {
            Toast.makeText(this, "Search Clicked", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ProductSearchActivity::class.java))
        }

        tabCart.setOnClickListener {
            Toast.makeText(this, "Cart Clicked", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, my_carts::class.java))
        }

        tabOrders.setOnClickListener {
            Toast.makeText(this, "My Orders Clicked", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, my_orders::class.java))
        }

        tabProfile.setOnClickListener {
            Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, UserProfileActivity::class.java))
        }
    }

    // -------------------------------
    // CHIP GROUP
    // -------------------------------
    private fun setupChipGroup() {
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroup)
        chipGroup.setOnCheckedStateChangeListener { _, _ ->
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

    private fun loadUnreadCount() {
        RetrofitClient.api.getUnreadNotificationCount(userId)
            .enqueue(object : Callback<UnreadCountResponse> {
                override fun onResponse(
                    call: Call<UnreadCountResponse>,
                    response: Response<UnreadCountResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val count = response.body()!!.unread_count
                        updateBadge(count)
                    } else {
                        updateBadge(0)
                    }
                }

                override fun onFailure(call: Call<UnreadCountResponse>, t: Throwable) {
                    updateBadge(0)
                }
            })
    }

    private fun updateBadge(count: Int) {
        if (count > 0) {
            tvNotificationBadge.visibility = View.VISIBLE
            tvNotificationBadge.text = if (count > 9) "9+" else count.toString()
        } else {
            tvNotificationBadge.visibility = View.GONE
        }
    }
}