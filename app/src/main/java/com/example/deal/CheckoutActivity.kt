package com.example.deal

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.deal.model.ApiResponse
import com.example.deal.model.CouponValidateResponse
import com.example.deal.model.CreateOrderRequest
import com.example.deal.model.NotificationRequest
import com.example.deal.model.OrderResponse
import com.example.deal.model.ProductResponse
import com.example.deal.model.ShippingMethodsResponse
import com.example.deal.network.RetrofitClient
import com.example.deal.utils.Constants
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
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

    private lateinit var tvSubtotal: TextView
    private lateinit var tvShippingAmount: TextView
    private lateinit var tvDiscountAmount: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvBottomTotalAmount: TextView

    private lateinit var etCouponCode: EditText
    private lateinit var btnApplyCoupon: MaterialButton
    private lateinit var tvCouponApplied: TextView

    private lateinit var cardShippingDHL: MaterialCardView
    private lateinit var cardShippingRegular: MaterialCardView
    private lateinit var imgShippingDhlCheck: ImageView
    private lateinit var imgShippingRegularCheck: ImageView

    private var quantity = 1
    private var basePrice = 0.0
    private var originalPrice = 0.0

    private var negotiationStatus: String = "Negotiation not done"
    private var productId: Int = -1
    private var selectedColor: String = "#FF0000"
    private var productNameValue: String = "Product"
    private var isNegotiated: Boolean = false

    private var totalAmount = 0.0
    private var razorpayPaymentId: String = ""

    private var shippingAmount = 0.0
    private var discountAmount = 0.0

    private var dhlShippingAmount = 10.0
    private var regularShippingAmount = 5.0
    private var selectedShippingType = "DHL"

    private lateinit var tvShippingDhlTitle: TextView
    private lateinit var tvShippingDhlDesc: TextView
    private lateinit var tvShippingRegularTitle: TextView
    private lateinit var tvShippingRegularDesc: TextView

    private var appliedCouponCode: String? = null

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        initViews()
        readIntentData()
        setupColor()
        setupQuantityButtons()
        loadInitialUI()
        loadShippingMethods()
        loadProductImageOnly()
        auth.currentUser?.let { loadUserData(it.uid) }

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnPay.setOnClickListener {
            startPayment()
        }

        btnApplyCoupon.setOnClickListener {
            applyCoupon()
        }

        cardShippingDHL.setOnClickListener {
            selectedShippingType = "DHL"
            shippingAmount = dhlShippingAmount
            updateShippingSelectionUI()
            updatePrice()
        }

        cardShippingRegular.setOnClickListener {
            selectedShippingType = "REGULAR"
            shippingAmount = regularShippingAmount
            updateShippingSelectionUI()
            updatePrice()
        }
    }

    private fun initViews() {
        tvProductName = findViewById(R.id.tvOrderProductName)
        tvProductPrice = findViewById(R.id.tvOrderProductPrice)
        tvQty = findViewById(R.id.tvOrderQty)
        btnPlus = findViewById(R.id.btnPlus)
        btnMinus = findViewById(R.id.btnMinus)
        imgProduct = findViewById(R.id.imgOrderProduct)
        viewSelectedColor = findViewById(R.id.viewSelectedColor)

        tvCustomerName = findViewById(R.id.tvCustomerName)
        tvAddressDetail = findViewById(R.id.tvAddressDetail)
        btnEditAddress = findViewById(R.id.btnEditAddress)
        btnBack = findViewById(R.id.btnBack)
        btnPay = findViewById(R.id.btnPay)
        tvNegotiationStatus = findViewById(R.id.tvNegotiationStatus)

        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvShippingAmount = findViewById(R.id.tvShippingAmount)
        tvDiscountAmount = findViewById(R.id.tvDiscountAmount)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvBottomTotalAmount = findViewById(R.id.tvBottomTotalAmount)

        etCouponCode = findViewById(R.id.etCouponCode)
        btnApplyCoupon = findViewById(R.id.btnApplyCoupon)
        tvCouponApplied = findViewById(R.id.tvCouponApplied)

        cardShippingDHL = findViewById(R.id.cardShippingDHL)
        cardShippingRegular = findViewById(R.id.cardShippingRegular)
        imgShippingDhlCheck = findViewById(R.id.imgShippingDhlCheck)
        imgShippingRegularCheck = findViewById(R.id.imgShippingRegularCheck)

        tvShippingDhlTitle = findViewById(R.id.tvShippingDhlTitle)
        tvShippingDhlDesc = findViewById(R.id.tvShippingDhlDesc)
        tvShippingRegularTitle = findViewById(R.id.tvShippingRegularTitle)
        tvShippingRegularDesc = findViewById(R.id.tvShippingRegularDesc)
    }

    private fun readIntentData() {
        productId = intent.getIntExtra("product_id", -1)
        selectedColor = intent.getStringExtra("selected_color") ?: "#FF0000"
        negotiationStatus = intent.getStringExtra("negotiation_status") ?: "Negotiation not done"
        productNameValue = intent.getStringExtra("product_name") ?: "Product"

        basePrice = intent.getDoubleExtra("product_price", 0.0)
        originalPrice = intent.getDoubleExtra("original_price", 0.0)
        isNegotiated = intent.getBooleanExtra("is_negotiated", false)

        tvNegotiationStatus.text = negotiationStatus

        Toast.makeText(
            this,
            "Received price: ₹$basePrice",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setupColor() {
        try {
            viewSelectedColor.background?.let {
                DrawableCompat.setTint(it, Color.parseColor(selectedColor))
            }
        } catch (_: Exception) {
            viewSelectedColor.background?.let {
                DrawableCompat.setTint(it, Color.GRAY)
            }
        }
    }

    private fun setupQuantityButtons() {
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
    }

    private fun loadInitialUI() {
        tvProductName.text = productNameValue
        tvCouponApplied.visibility = View.GONE
        updatePrice()
    }

    private fun loadShippingMethods() {
        RetrofitClient.api.getShippingMethods()
            .enqueue(object : Callback<ShippingMethodsResponse> {
                override fun onResponse(
                    call: Call<ShippingMethodsResponse>,
                    response: Response<ShippingMethodsResponse>
                ) {
                    val body = response.body()

                    if (response.isSuccessful && body?.success == true && body.data.isNotEmpty()) {
                        body.data.forEachIndexed { index, item ->
                            if (index == 0) {
                                dhlShippingAmount = item.amount
                                tvShippingDhlTitle.text = "${item.name} (₹%.2f)".format(item.amount)
                                tvShippingDhlDesc.text =
                                    item.description ?: item.estimated_time ?: "Fast delivery"
                            } else if (index == 1) {
                                regularShippingAmount = item.amount
                                tvShippingRegularTitle.text = "${item.name} (₹%.2f)".format(item.amount)
                                tvShippingRegularDesc.text =
                                    item.description ?: item.estimated_time ?: "Standard delivery"
                            }
                        }
                    }

                    selectedShippingType = "DHL"
                    shippingAmount = dhlShippingAmount
                    updateShippingSelectionUI()
                    updatePrice()
                }

                override fun onFailure(call: Call<ShippingMethodsResponse>, t: Throwable) {
                    selectedShippingType = "DHL"
                    shippingAmount = dhlShippingAmount
                    updateShippingSelectionUI()
                    updatePrice()
                }
            })
    }

    private fun updatePrice() {
        val subtotal = basePrice * quantity
        val total = subtotal + shippingAmount - discountAmount

        totalAmount = total

        tvProductPrice.text = "Price: ₹%.2f".format(subtotal)
        tvSubtotal.text = "₹%.2f".format(subtotal)
        tvShippingAmount.text = "₹%.2f".format(shippingAmount)
        tvDiscountAmount.text = "-₹%.2f".format(discountAmount)
        tvTotalAmount.text = "₹%.2f".format(total)
        tvBottomTotalAmount.text = "₹%.2f".format(total)
        btnPay.text = "Pay – ₹%.2f".format(total)

        if (discountAmount > 0) {
            tvDiscountAmount.setTextColor(Color.parseColor("#16A34A"))
        } else {
            tvDiscountAmount.setTextColor(Color.parseColor("#64748B"))
        }
    }

    private fun startPayment() {
        if (totalAmount <= 0.0) {
            Toast.makeText(this, "Invalid payment amount", Toast.LENGTH_SHORT).show()
            return
        }

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

    override fun onPaymentSuccess(paymentId: String?) {
        razorpayPaymentId = paymentId ?: ""
        Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show()
        createOrder()
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show()
    }

    private fun createOrder() {
        val user = auth.currentUser ?: return

        val finalPricePerItem = basePrice
        val subtotal = finalPricePerItem * quantity
        val total = subtotal + shippingAmount - discountAmount

        val request = CreateOrderRequest(
            user_id = user.uid,
            product_id = productId,
            quantity = quantity,
            price = finalPricePerItem,
            total_amount = total,
            color = selectedColor,
            negotiation_status = negotiationStatus,
            payment_id = razorpayPaymentId,
            payment_status = "Success"
        )

        RetrofitClient.api.createOrder(request)
            .enqueue(object : Callback<OrderResponse> {
                override fun onResponse(
                    call: Call<OrderResponse>,
                    response: Response<OrderResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        insertPurchaseNotification(user.uid, total)
                    } else {
                        Toast.makeText(
                            this@CheckoutActivity,
                            "Order failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                    Toast.makeText(
                        this@CheckoutActivity,
                        t.message ?: "Order error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun insertPurchaseNotification(userId: String, total: Double) {
        val request = NotificationRequest(
            user_id = userId,
            title = "Order Placed",
            message = "You bought $productNameValue for ₹$total",
            type = "purchase"
        )

        RetrofitClient.api.addNotification(request)
            .enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(
                    call: Call<Map<String, String>>,
                    response: Response<Map<String, String>>
                ) {
                    openSuccessScreen(total)
                }

                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    openSuccessScreen(total)
                }
            })
    }

    private fun openSuccessScreen(total: Double) {
        val intent = Intent(this@CheckoutActivity, payment_sucess::class.java)
        intent.putExtra("payment_id", razorpayPaymentId)
        intent.putExtra("total_amount", total)
        intent.putExtra("payment_status", "Success")
        intent.putExtra("payment_time", System.currentTimeMillis())
        startActivity(intent)
        finish()
    }

    private fun loadProductImageOnly() {
        if (productId == -1) return

        RetrofitClient.api.getProductById(productId)
            .enqueue(object : Callback<ProductResponse> {
                override fun onResponse(
                    call: Call<ProductResponse>,
                    response: Response<ProductResponse>
                ) {
                    val data = response.body()?.data ?: return

                    if (productNameValue.isBlank() || productNameValue == "Product") {
                        productNameValue = data.name ?: "Product"
                        tvProductName.text = productNameValue
                    }

                    Glide.with(this@CheckoutActivity)
                        .load(Constants.BASE_URL + (data.image_url ?: ""))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(imgProduct)
                }

                override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                }
            })
    }

    private fun loadUserData(uid: String) {
        RetrofitClient.api.getUser(uid)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(
                    call: Call<ApiResponse>,
                    response: Response<ApiResponse>
                ) {
                    val user = response.body()?.data ?: return
                    tvCustomerName.text = user.name
                    tvAddressDetail.text = user.address
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                }
            })
    }

    private fun updateShippingSelectionUI() {
        if (selectedShippingType == "DHL") {
            cardShippingDHL.setCardBackgroundColor(Color.parseColor("#EFF6FF"))
            cardShippingDHL.strokeColor = Color.parseColor("#2563EB")
            cardShippingDHL.strokeWidth = dpToPx(2)
            imgShippingDhlCheck.setImageResource(R.drawable.ic_radio_checked)

            cardShippingRegular.setCardBackgroundColor(Color.parseColor("#F8FAFC"))
            cardShippingRegular.strokeColor = Color.parseColor("#E2E8F0")
            cardShippingRegular.strokeWidth = dpToPx(1)
            imgShippingRegularCheck.setImageResource(R.drawable.ic_radio_unchecked)
        } else {
            cardShippingRegular.setCardBackgroundColor(Color.parseColor("#EFF6FF"))
            cardShippingRegular.strokeColor = Color.parseColor("#2563EB")
            cardShippingRegular.strokeWidth = dpToPx(2)
            imgShippingRegularCheck.setImageResource(R.drawable.ic_radio_checked)

            cardShippingDHL.setCardBackgroundColor(Color.parseColor("#F8FAFC"))
            cardShippingDHL.strokeColor = Color.parseColor("#E2E8F0")
            cardShippingDHL.strokeWidth = dpToPx(1)
            imgShippingDhlCheck.setImageResource(R.drawable.ic_radio_unchecked)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun applyCoupon() {
        val coupon = etCouponCode.text.toString().trim()

        if (coupon.isEmpty()) {
            discountAmount = 0.0
            appliedCouponCode = null
            tvCouponApplied.visibility = View.VISIBLE
            tvCouponApplied.text = "Enter coupon code"
            tvCouponApplied.setTextColor(Color.parseColor("#DC2626"))
            updatePrice()
            return
        }

        RetrofitClient.api.validateCoupon(coupon)
            .enqueue(object : Callback<CouponValidateResponse> {
                override fun onResponse(
                    call: Call<CouponValidateResponse>,
                    response: Response<CouponValidateResponse>
                ) {
                    val body = response.body()

                    if (response.isSuccessful && body != null) {
                        if (body.valid) {
                            discountAmount = body.discount_amount
                            appliedCouponCode = body.code
                            tvCouponApplied.visibility = View.VISIBLE
                            tvCouponApplied.text = body.message
                            tvCouponApplied.setTextColor(Color.parseColor("#16A34A"))
                        } else {
                            discountAmount = 0.0
                            appliedCouponCode = null
                            tvCouponApplied.visibility = View.VISIBLE
                            tvCouponApplied.text = body.message
                            tvCouponApplied.setTextColor(Color.parseColor("#DC2626"))
                        }
                    } else {
                        discountAmount = 0.0
                        appliedCouponCode = null
                        tvCouponApplied.visibility = View.VISIBLE
                        tvCouponApplied.text = "Coupon validation failed"
                        tvCouponApplied.setTextColor(Color.parseColor("#DC2626"))
                    }

                    updatePrice()
                }

                override fun onFailure(call: Call<CouponValidateResponse>, t: Throwable) {
                    discountAmount = 0.0
                    appliedCouponCode = null
                    tvCouponApplied.visibility = View.VISIBLE
                    tvCouponApplied.text = "Server error"
                    tvCouponApplied.setTextColor(Color.parseColor("#DC2626"))
                    updatePrice()
                }
            })
    }
}