package com.example.deal

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.deal.network.RetrofitClient
import com.example.deal.model.Product
import com.example.deal.model.ProductResponse
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProductDetailActivity : AppCompatActivity() {

    private lateinit var productImage: ImageView
    private lateinit var productName: TextView
    private lateinit var productPrice: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Initialize views
        productImage = findViewById(R.id.imgProduct)
        productName = findViewById(R.id.tvProductName)
        productPrice = findViewById(R.id.tvPrice)

        // ✅ Get product ID from intent
        val productId = intent.getIntExtra("product_id", -1)
        if (productId == -1) {
            Toast.makeText(this, "Invalid Product ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ✅ Load product details from API
        loadProduct(productId)

        setupClickListeners()
        playEntranceAnimations()
    }

    private fun loadProduct(productId: Int) {
        RetrofitClient.instance.getProductById(productId)
            .enqueue(object : Callback<ProductResponse> {
                override fun onResponse(
                    call: Call<ProductResponse>,
                    response: Response<ProductResponse>
                ) {
                    val body = response.body()
                    if (response.isSuccessful && body != null && body.success && body.data != null) {
                        val product = body.data

                        productName.text = product.name
                        productPrice.text = "₹${product.selling_price}"

                        val imageUrl = product.image_url?.let { "http://192.168.73.139:8000/$it" }
                        if (!imageUrl.isNullOrEmpty()) {
                            Picasso.get().load(imageUrl).into(productImage)
                        } else {
                            productImage.setImageResource(R.drawable.sale_img1)
                        }
                    } else {
                        Toast.makeText(this@ProductDetailActivity, "Product not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                    Toast.makeText(this@ProductDetailActivity, "Failed to load product", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.btnBack).setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        var isFavorite = false
        findViewById<View>(R.id.btnFavorite).setOnClickListener {
            isFavorite = !isFavorite
            val icon = if (isFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
            findViewById<ImageView>(R.id.icFavorite).setImageResource(icon)
            it.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
            }.start()
        }

        findViewById<View>(R.id.btnBuyNow).setOnClickListener {
            Toast.makeText(this, "Added to purchase!", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnNegotiate).setOnClickListener {
            Toast.makeText(this, "Negotiate price", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnCart).setOnClickListener {
            Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playEntranceAnimations() {
        val imageContainer = findViewById<View>(R.id.imageContainer)
        val infoContainer = findViewById<View>(R.id.infoContainer)
        val bottomBar = findViewById<View>(R.id.bottomBar)

        listOf(imageContainer, infoContainer, bottomBar).forEach {
            it.alpha = 0f
            it.translationY = 40f
        }

        val decel = DecelerateInterpolator(1.5f)

        ObjectAnimator.ofFloat(imageContainer, "alpha", 0f, 1f).apply { duration = 500; interpolator = decel; start() }
        ObjectAnimator.ofFloat(imageContainer, "translationY", 40f, 0f).apply { duration = 500; interpolator = decel; start() }

        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(infoContainer, "alpha", 0f, 1f).apply { duration = 500 },
                ObjectAnimator.ofFloat(infoContainer, "translationY", 40f, 0f).apply { duration = 500 }
            )
            startDelay = 200
            interpolator = decel
            start()
        }

        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(bottomBar, "alpha", 0f, 1f).apply { duration = 500 },
                ObjectAnimator.ofFloat(bottomBar, "translationY", 60f, 0f).apply { duration = 500 }
            )
            startDelay = 400
            interpolator = decel
            start()
        }
    }
}