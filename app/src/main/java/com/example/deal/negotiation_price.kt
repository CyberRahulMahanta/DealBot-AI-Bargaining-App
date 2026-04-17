package com.example.deal

import ApiResponseGeneric
import BargainRequest
import NegotiationMessageData
import StartNegotiationData
import StartNegotiationRequest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.deal.model.ApiResponse
import com.example.deal.model.NegotiationChatMessage
import com.example.deal.model.Product
import com.example.deal.model.ProductResponse
import com.example.deal.network.RetrofitClient
import com.example.deal.utils.Constants
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class negotiation_price : AppCompatActivity() {

    private lateinit var btnBack: MaterialCardView
    private lateinit var imgProduct: ImageView
    private lateinit var imgProductThumb: ImageView
    private lateinit var tvProductName: TextView
    private lateinit var tvBrand: TextView
    private lateinit var viewColorCircle: View
    private lateinit var tvOriginalPrice: TextView
    private lateinit var rvNegotiationChat: RecyclerView
    private lateinit var etOfferPrice: TextInputEditText
    private lateinit var btnSendOffer: MaterialButton
    private lateinit var tvNegotiationStatus: TextView
    private lateinit var btnContinueCheckout: MaterialButton
    private lateinit var imgUserAvatar: ImageView

    private var product: Product? = null
    private var selectedColor: String = "N/A"
    private var sessionId: String? = null

    // Final price that must go to checkout
    private var currentNegotiatedPrice: Double = 0.0
    private var negotiationStatus: String = "COUNTER"
    private var isDealAccepted: Boolean = false

    private val chatList = mutableListOf<NegotiationChatMessage>()
    private lateinit var chatAdapter: NegotiationChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_negotiation_price)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                imeInsets.bottom
            )

            insets
        }

        initViews()
        loadUserAvatar()
        setupRecyclerView()
        setupListeners()

        selectedColor = intent.getStringExtra("selected_color") ?: "N/A"

        val productId = intent.getIntExtra("product_id", -1)
        if (productId == -1) {
            Toast.makeText(this, "Invalid product", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadProduct(productId)
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        imgProduct = findViewById(R.id.imgProduct)
        imgProductThumb = findViewById(R.id.imgProductThumb)
        tvProductName = findViewById(R.id.tvProductName)
        tvBrand = findViewById(R.id.tvBrand)
        viewColorCircle = findViewById(R.id.viewColorCircle)
        tvOriginalPrice = findViewById(R.id.tvOriginalPrice)
        rvNegotiationChat = findViewById(R.id.rvNegotiationChat)
        etOfferPrice = findViewById(R.id.etOfferPrice)
        btnSendOffer = findViewById(R.id.btnSendOffer)
        tvNegotiationStatus = findViewById(R.id.tvNegotiationStatus)
        btnContinueCheckout = findViewById(R.id.btnContinueCheckout)
        imgUserAvatar = findViewById(R.id.imgUserAvatar)
    }

    private fun setupRecyclerView() {
        chatAdapter = NegotiationChatAdapter(chatList)
        rvNegotiationChat.layoutManager = LinearLayoutManager(this)
        rvNegotiationChat.adapter = chatAdapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSendOffer.setOnClickListener {
            val message = etOfferPrice.text?.toString()?.trim().orEmpty()

            if (message.isEmpty()) {
                Toast.makeText(this, "Enter your message or offer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isDealAccepted) {
                Toast.makeText(this, "Deal already accepted. Continue to checkout.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendMessageToAi(message)
        }

        btnContinueCheckout.setOnClickListener {
            val currentProduct = product
            if (currentProduct == null) {
                Toast.makeText(this, "Product not loaded", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val finalPrice = when {
                currentNegotiatedPrice > 0.0 -> currentNegotiatedPrice
                (currentProduct.selling_price ?: 0.0) > 0.0 -> currentProduct.selling_price ?: 0.0
                else -> 0.0
            }

            val finalStatus = if (isDealAccepted || negotiationStatus.equals("ACCEPT", true)) {
                "Negotiation done"
            } else {
                "Negotiation in progress"
            }

            Toast.makeText(this, "Checkout price: ₹$finalPrice", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, CheckoutActivity::class.java).apply {
                putExtra("product_id", currentProduct.id ?: -1)
                putExtra("product_name", currentProduct.name ?: "Unknown")
                putExtra("product_price", finalPrice)
                putExtra("original_price", currentProduct.selling_price ?: 0.0)
                putExtra("selected_color", selectedColor)
                putExtra("negotiation_status", finalStatus)
                putExtra("is_negotiated", isDealAccepted || negotiationStatus.equals("ACCEPT", true))
            }

            startActivity(intent)
        }
    }

    private fun loadProduct(productId: Int) {
        RetrofitClient.api.getProductById(productId)
            .enqueue(object : Callback<ProductResponse> {
                override fun onResponse(
                    call: Call<ProductResponse>,
                    response: Response<ProductResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()?.data
                        if (data != null) {
                            product = data
                            bindProduct(data)
                            startNegotiationSession(productId)
                        } else {
                            Toast.makeText(
                                this@negotiation_price,
                                "Product not found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@negotiation_price,
                            "Failed to load product",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                    Toast.makeText(
                        this@negotiation_price,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun bindProduct(product: Product) {
        tvProductName.text = product.name ?: "Unknown Product"
        tvBrand.text = "Brand: ${product.brand ?: "Unknown"}"

        try {
            val color = android.graphics.Color.parseColor(selectedColor)
            viewColorCircle.background.setTint(color)
        } catch (e: Exception) {
            viewColorCircle.background.setTint(android.graphics.Color.GRAY)
        }

        val originalPrice = product.selling_price ?: 0.0
        tvOriginalPrice.text = "₹$originalPrice"

        // Set only once, do not overwrite later negotiated value
        if (currentNegotiatedPrice <= 0.0) {
            currentNegotiatedPrice = originalPrice
        }

        val imageUrl = product.image_url?.let { "${Constants.BASE_URL}$it" }
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this).load(imageUrl).into(imgProduct)
            Glide.with(this).load(imageUrl).into(imgProductThumb)
        }

        val minPrice = product.min_price ?: 0.0
        tvNegotiationStatus.text = "Current price: ₹$currentNegotiatedPrice | Min: ₹$minPrice"
    }

    private fun startNegotiationSession(productId: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val request = StartNegotiationRequest(
            product_id = productId,
            customer_id = userId
        )

        RetrofitClient.api.startNegotiation(request)
            .enqueue(object : Callback<ApiResponseGeneric<StartNegotiationData>> {
                override fun onResponse(
                    call: Call<ApiResponseGeneric<StartNegotiationData>>,
                    response: Response<ApiResponseGeneric<StartNegotiationData>>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()?.data
                        sessionId = data?.session_id

                        val welcomeMessage = data?.welcome_message
                            ?: "Namaste ji! Aap apna offer bhejiye."
                        addAiMessage(welcomeMessage)

                        // Use server initial price only if valid
                        if ((data?.initial_price ?: 0.0) > 0.0) {
                            currentNegotiatedPrice = data?.initial_price ?: currentNegotiatedPrice
                        }

                        val minPrice = product?.min_price ?: 0.0
                        tvNegotiationStatus.text =
                            "Current price: ₹$currentNegotiatedPrice | Min: ₹$minPrice"
                    } else {
                        Toast.makeText(
                            this@negotiation_price,
                            response.body()?.message ?: "Failed to start negotiation",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponseGeneric<StartNegotiationData>>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@negotiation_price,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun sendMessageToAi(message: String) {
        val currentSessionId = sessionId
        val currentProduct = product

        if (currentSessionId.isNullOrEmpty()) {
            Toast.makeText(this, "Negotiation session not started", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentProduct?.id == null) {
            Toast.makeText(this, "Product not loaded", Toast.LENGTH_SHORT).show()
            return
        }

        addUserMessage(message)
        etOfferPrice.text?.clear()

        val request = BargainRequest(
            session_id = currentSessionId,
            product_id = currentProduct.id,
            message = message
        )

        RetrofitClient.api.sendNegotiationMessage(request)
            .enqueue(object : Callback<ApiResponseGeneric<NegotiationMessageData>> {
                override fun onResponse(
                    call: Call<ApiResponseGeneric<NegotiationMessageData>>,
                    response: Response<ApiResponseGeneric<NegotiationMessageData>>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()?.data

                        val aiReply = data?.shopkeeper_message ?: "Theek hai ji, aur bataiye."
                        addAiMessage(aiReply)

                        // Always update if API returns a valid price
                        if ((data?.current_price ?: 0.0) > 0.0) {
                            currentNegotiatedPrice = data?.current_price ?: currentNegotiatedPrice
                        }

                        negotiationStatus = data?.status ?: "COUNTER"

                        when {
                            negotiationStatus.equals("ACCEPT", true) -> {
                                isDealAccepted = true

                                // If API did not send current_price, keep old negotiated price
                                if (currentNegotiatedPrice <= 0.0) {
                                    currentNegotiatedPrice = currentProduct.selling_price ?: 0.0
                                }

                                tvNegotiationStatus.text =
                                    "Deal accepted at ₹$currentNegotiatedPrice"

                                btnSendOffer.isEnabled = false
                                etOfferPrice.isEnabled = false
                            }

                            negotiationStatus.equals("WALK_AWAY", true) -> {
                                tvNegotiationStatus.text = "Negotiation closed"
                            }

                            else -> {
                                tvNegotiationStatus.text =
                                    "Current negotiated price: ₹$currentNegotiatedPrice"
                            }
                        }
                    } else {
                        addAiMessage(
                            response.body()?.message
                                ?: "Sorry, message could not be processed."
                        )
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponseGeneric<NegotiationMessageData>>,
                    t: Throwable
                ) {
                    addAiMessage("Network error: ${t.message}")
                }
            })
    }

    private fun loadUserAvatar() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId.isNullOrEmpty()) {
            imgUserAvatar.setImageResource(R.drawable.ic_person)
            return
        }

        RetrofitClient.api.getUser(userId)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val user = response.body()?.data
                        val imagePath = user?.profile_image

                        if (!imagePath.isNullOrEmpty()) {
                            Glide.with(this@negotiation_price)
                                .load(Constants.BASE_URL + imagePath + "?${System.currentTimeMillis()}")
                                .placeholder(R.drawable.ic_person)
                                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(imgUserAvatar)
                        } else {
                            imgUserAvatar.setImageResource(R.drawable.ic_person)
                        }
                    } else {
                        imgUserAvatar.setImageResource(R.drawable.ic_person)
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    imgUserAvatar.setImageResource(R.drawable.ic_person)
                }
            })
    }

    private fun addUserMessage(text: String) {
        chatList.add(NegotiationChatMessage(text, true))
        chatAdapter.notifyItemInserted(chatList.size - 1)
        rvNegotiationChat.scrollToPosition(chatList.size - 1)
    }

    private fun addAiMessage(text: String) {
        chatList.add(NegotiationChatMessage(text, false))
        chatAdapter.notifyItemInserted(chatList.size - 1)
        rvNegotiationChat.scrollToPosition(chatList.size - 1)
    }
}