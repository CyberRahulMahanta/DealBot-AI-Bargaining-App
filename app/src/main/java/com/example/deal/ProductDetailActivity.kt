package com.example.deal

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.deal.model.Product
import com.example.deal.model.ProductResponse
import com.example.deal.network.ApiService
import com.example.deal.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var productImage: ImageView
    private lateinit var productName: TextView
    private lateinit var productPrice: TextView
    private lateinit var productBrand: TextView
    private lateinit var productRating: TextView
    private lateinit var productReviewCount: TextView
    private lateinit var colorContainer: LinearLayout
    private lateinit var btnBuyNow: MaterialButton

    private var product: Product? = null
    private var selectedColorIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Initialize views
        productImage = findViewById(R.id.imgProduct)
        productName = findViewById(R.id.tvProductName)
        productPrice = findViewById(R.id.tvPrice)
        productBrand = findViewById(R.id.tvBrandName)
        productRating = findViewById(R.id.tvRating)
        productReviewCount = findViewById(R.id.tvReviewCount)
        colorContainer = findViewById(R.id.colorContainer)
        btnBuyNow = findViewById(R.id.btnBuyNow)

        // Get product ID from intent
        val productId = intent.getIntExtra("product_id", -1)
        if (productId != -1) {
            loadProduct(productId)
        } else {
            Toast.makeText(this, "Invalid product", Toast.LENGTH_SHORT).show()
        }

// Buy Now Button
        btnBuyNow.setOnClickListener {
            if (product == null) {
                Toast.makeText(this, "Product not loaded yet", Toast.LENGTH_SHORT).show()
                Log.e("BUY_NOW", "Product is null")
                return@setOnClickListener
            }

            val colors = product?.available_colors?.split(",") ?: emptyList()
            val selectedColor = if (colors.isNotEmpty() && selectedColorIndex < colors.size) {
                colors[selectedColorIndex]
            } else "N/A"

            Log.d("BUY_NOW", "Launching CheckoutActivity with: id=${product?.id}, name=${product?.name}, price=${product?.selling_price}, color=$selectedColor")

            try {
                val intent = Intent(this, CheckoutActivity::class.java).apply {
                    putExtra("product_id", product?.id ?: -1)
                    putExtra("product_name", product?.name ?: "Unknown")
                    putExtra("product_price", product?.selling_price ?: 0.0)
                    putExtra("selected_color", selectedColor)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("BUY_NOW_ERROR", e.message ?: "Unknown error")
                Toast.makeText(this, "Failed to open checkout", Toast.LENGTH_SHORT).show()
            }
        }



    }

    private fun loadProduct(id: Int) {

        val api = RetrofitClient.api

        api.getProductById(id).enqueue(object : Callback<ProductResponse> {

            override fun onResponse(
                call: Call<ProductResponse>,
                response: Response<ProductResponse>
            ) {
                if (response.isSuccessful) {
                    product = response.body()?.data
                    product?.let { populateProductDetails(it) }
                        ?: Toast.makeText(
                            this@ProductDetailActivity,
                            "Product not found",
                            Toast.LENGTH_SHORT
                        ).show()
                } else {
                    Toast.makeText(
                        this@ProductDetailActivity,
                        "Server error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                Log.e("API_ERROR", t.message ?: "Unknown error")
                Toast.makeText(
                    this@ProductDetailActivity,
                    "Failed to load product",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun populateProductDetails(product: Product) {
        // Safe UI updates
        productName.text = product.name ?: "Unknown"
        productPrice.text = "₹${product.selling_price ?: 0.0}"
        productBrand.text = product.brand ?: "Unknown"
        productRating.text = product.rating?.toString() ?: "0"
        productReviewCount.text = "(${product.review_count ?: 0} Reviews)"

        // Load image safely
        product.image_url?.let {
            val fullImageUrl = "http://192.168.74.224:8000/$it"
            Glide.with(this).load(fullImageUrl).into(productImage)
        }

        // Colors
        setupColorViews(product)

        // Features and warranty
        loadFeatures(product)
        loadWarranty(product)
    }

    private fun setupColorViews(product: Product) {
        colorContainer.removeAllViews()
        selectedColorIndex = 0
        val colors = product.available_colors?.split(",") ?: emptyList()

        colors.forEachIndexed { index, hex ->
            try {
                val colorView = View(this)
                val size = 80 // px
                val params = LinearLayout.LayoutParams(size, size)
                params.setMargins(16, 0, 16, 0)
                colorView.layoutParams = params

                val drawable = GradientDrawable()
                drawable.shape = GradientDrawable.OVAL
                drawable.setColor(Color.parseColor(hex))
                if (index == selectedColorIndex) drawable.setStroke(6, Color.GRAY)
                colorView.background = drawable

                colorView.setOnClickListener {
                    selectedColorIndex = index
                    for (i in 0 until colorContainer.childCount) {
                        val child = colorContainer.getChildAt(i)
                        val d = child.background as GradientDrawable
                        if (i == selectedColorIndex) d.setStroke(6, Color.GRAY)
                        else d.setStroke(0, Color.TRANSPARENT)
                    }
                    Toast.makeText(this, "Selected color: $hex", Toast.LENGTH_SHORT).show()
                }

                colorContainer.addView(colorView)
            } catch (e: Exception) {
                Log.e("COLOR_ERROR", "Invalid color: $hex")
            }
        }
    }

    private fun loadFeatures(product: Product) {
        val container = findViewById<LinearLayout>(R.id.featuresContainer)
        container.removeAllViews()
        product.features.forEach { feature ->
            val view = layoutInflater.inflate(R.layout.feature_item, container, false)
            view.findViewById<TextView>(R.id.featureText).text = feature
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.weight = 1f
            params.marginEnd = 8.dpToPx()
            view.layoutParams = params
            container.addView(view)
        }
    }

    private fun loadWarranty(product: Product) {
        val container = findViewById<LinearLayout>(R.id.warrantyContainer)
        container.removeAllViews()
        product.warranty?.takeIf { it.isNotEmpty() && it != "None" }?.let { warrantyText ->
            val view = layoutInflater.inflate(R.layout.warranty_item, container, false)
            view.findViewById<TextView>(R.id.warrantyText).text = warrantyText
            container.addView(view)
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}