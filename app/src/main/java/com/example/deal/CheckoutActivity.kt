package com.example.deal

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.deal.model.*
import com.example.deal.network.RetrofitClient
import com.example.deal.utils.Constants
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckoutActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var tvProductName: TextView
    private lateinit var tvProductPrice: TextView
    private lateinit var tvQty: TextView
    private lateinit var btnPlus: TextView
    private lateinit var btnMinus: TextView
    private lateinit var imgProduct: ImageView
    private lateinit var viewSelectedColor: View

    private lateinit var tvCustomerName: TextView
    private lateinit var tvAddressDetail: TextView
    private lateinit var btnEditAddress: ImageView
    private lateinit var btnBack: MaterialCardView
    private lateinit var btnPay: MaterialButton
    private lateinit var tvNegotiationStatus: TextView

    private var quantity = 1
    private var basePrice = 0.0
    private var negotiationStatus: String = "Negotiation not done"
    private var productId: Int = -1
    private var selectedColor: String = "#FF0000"

    private var totalAmount = 0.0
    private var razorpayPaymentId: String = ""

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        // Bind views
        tvProductName = findViewById(R.id.tvOrderProductName)
        tvProductPrice = findViewById(R.id.tvOrderProductPrice)
        tvQty = findViewById(R.id.tvOrderQty)
        btnPlus = findViewById(R.id.btnPlus)
        btnMinus = findViewById(R.id.btnMinus)
        imgProduct = findViewById(R.id.imgOrderProduct)
        viewSelectedColor = findViewById(R.id.viewSelectedColor)
        btnEditAddress = findViewById(R.id.btnEditAddress)
        btnBack = findViewById(R.id.btnBack)
        btnPay = findViewById(R.id.btnPay)
        tvNegotiationStatus = findViewById(R.id.tvNegotiationStatus)

        tvCustomerName = findViewById(R.id.tvCustomerName)
        tvAddressDetail = findViewById(R.id.tvAddressDetail)

        // Intent data
        productId = intent.getIntExtra("product_id", -1)
        selectedColor = intent.getStringExtra("selected_color") ?: "#FF0000"
        negotiationStatus = intent.getStringExtra("negotiation_status") ?: "Negotiation not done"

        tvNegotiationStatus.text = negotiationStatus

        viewSelectedColor.background?.let {
            DrawableCompat.setTint(it, Color.parseColor(selectedColor))
        }

        tvQty.text = quantity.toString()

        btnPlus.setOnClickListener {
            quantity++
            tvQty.text = quantity.toString()
            updatePrice()
        }

        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                tvQty.text = quantity.toString()
                updatePrice()
            }
        }

        if (productId != -1) fetchProductData(productId)

        auth.currentUser?.let { loadUserData(it.uid) }

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        btnPay.setOnClickListener {
            startPayment()
        }
    }

    private fun updatePrice() {
        var price = basePrice
        if (negotiationStatus == "Negotiation done") price *= 0.9

        totalAmount = price * quantity

        tvProductPrice.text = "Price: ₹$totalAmount"
        btnPay.text = "Pay – ₹$totalAmount"
    }

    // 🔥 START RAZORPAY
    private fun startPayment() {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_Y6AmzoQ1dZOCsA")

        try {
            val options = JSONObject()
            options.put("name", "Deal App")
            options.put("description", "Product Payment")
            options.put("currency", "INR")
            options.put("amount", (totalAmount * 100).toInt())

            checkout.open(this, options)

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ SUCCESS
    override fun onPaymentSuccess(paymentId: String?) {
        razorpayPaymentId = paymentId ?: ""

        Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show()

        createOrder()
    }

    // ❌ FAILURE
    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show()
    }

    // 🔥 CREATE ORDER AFTER PAYMENT
    private fun createOrder() {

        val user = auth.currentUser ?: return

        var finalPrice = basePrice
        if (negotiationStatus == "Negotiation done") finalPrice *= 0.9

        val total = finalPrice * quantity

        val request = CreateOrderRequest(
            user_id = user.uid,
            product_id = productId,
            quantity = quantity,
            price = finalPrice,
            total_amount = total,
            color = selectedColor,
            negotiation_status = negotiationStatus,

            // ✅ IMPORTANT
            payment_id = razorpayPaymentId,
            payment_status = "Success"
        )

        RetrofitClient.api.createOrder(request)
            .enqueue(object : Callback<OrderResponse> {

                override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {

                        val intent = Intent(this@CheckoutActivity, payment_sucess::class.java)
                        intent.putExtra("payment_id", razorpayPaymentId)
                        intent.putExtra("total_amount", total)
                        intent.putExtra("payment_status", "Success")
                        intent.putExtra("payment_time", System.currentTimeMillis())

                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(this@CheckoutActivity, "Order failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                    Toast.makeText(this@CheckoutActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchProductData(productId: Int) {
        RetrofitClient.api.getProductById(productId)
            .enqueue(object : Callback<ProductResponse> {
                override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                    val data = response.body()?.data ?: return
                    tvProductName.text = data.name
                    basePrice = data.selling_price ?: 0.0
                    updatePrice()

                    Glide.with(this@CheckoutActivity)
                        .load(Constants.BASE_URL + (data.image_url ?: ""))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(imgProduct)
                }

                override fun onFailure(call: Call<ProductResponse>, t: Throwable) {}
            })
    }

    private fun loadUserData(uid: String) {
        RetrofitClient.api.getUser(uid)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    val user = response.body()?.data ?: return
                    tvCustomerName.text = user.name
                    tvAddressDetail.text = user.address
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {}
            })
    }
}