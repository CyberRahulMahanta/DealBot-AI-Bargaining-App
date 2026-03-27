package com.example.deal

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deal.adapter.CartAdapter
import com.example.deal.model.*
import com.example.deal.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class my_carts : AppCompatActivity(),
    CartAdapter.OnQuantityChangeListener,
    PaymentResultListener {

    private lateinit var rvItems: RecyclerView
    private lateinit var btnCheckout: MaterialButton
    private lateinit var progressOverlay: View

    private var cartList: MutableList<CartItem> = mutableListOf()
    private lateinit var adapter: CartAdapter
    private var totalAmountGlobal = 0.0
    private var razorpayPaymentId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_carts)

        // Initialize all views
        rvItems = findViewById(R.id.rvCartItems)
        rvItems.layoutManager = LinearLayoutManager(this)

        btnCheckout = findViewById(R.id.btnCheckout)
        progressOverlay = findViewById(R.id.progressOverlay)

        val btnBack = findViewById<View>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        btnCheckout.setOnClickListener {
            placeOrderFromCart()
        }

        loadCartData()
        setupSwipeToDelete()
    }

    // ---------------- UI ----------------
    private fun showLoading() {
        progressOverlay.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressOverlay.visibility = View.GONE
    }

    private fun checkCartEmpty(items: List<CartItem>) {
        val footer = findViewById<View>(R.id.footerSection)
        val emptyState = findViewById<LinearLayout>(R.id.layoutEmptyState)

        if (items.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            rvItems.visibility = View.GONE
            footer.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            rvItems.visibility = View.VISIBLE
            footer.visibility = View.VISIBLE
            updatePrice(items)
        }
    }

    private fun updatePrice(items: List<CartItem>) {
        val subTotal = items.sumOf {
            var price = it.selling_price
            if (it.negotiation_status == "Negotiation done") price *= 0.9
            price * it.quantity
        }
        val delivery = 40
        val grandTotal = subTotal + delivery

        findViewById<TextView>(R.id.tvSubTotalValue).text = "₹ $subTotal"
        findViewById<TextView>(R.id.tvDeliveryValue).text = "₹ $delivery"
        findViewById<TextView>(R.id.tvGrandTotalValue).text = "₹ $grandTotal"

        totalAmountGlobal = grandTotal
    }

    override fun onQuantityChanged(updatedList: List<CartItem>) {
        updatePrice(updatedList)
    }

    // ---------------- LOAD CART ----------------
    private fun loadCartData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        RetrofitClient.api.getCart(userId)
            .enqueue(object : Callback<CartResponse> {
                override fun onResponse(call: Call<CartResponse>, response: Response<CartResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {

                        cartList = response.body()?.data?.toMutableList() ?: mutableListOf()
                        cartList.forEach { it.quantity = 1 }

                        adapter = CartAdapter(cartList, this@my_carts)
                        rvItems.adapter = adapter

                        checkCartEmpty(cartList)
                    } else {
                        checkCartEmpty(emptyList())
                    }
                }

                override fun onFailure(call: Call<CartResponse>, t: Throwable) {
                    checkCartEmpty(emptyList())
                }
            })
    }

    // ---------------- PAYMENT ----------------
    private fun placeOrderFromCart() {
        if (cartList.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        startPayment(totalAmountGlobal)
    }

    private fun startPayment(amount: Double) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_Y6AmzoQ1dZOCsA")

        try {
            val options = JSONObject()
            options.put("name", "Deal App")
            options.put("description", "Cart Payment")
            options.put("currency", "INR")
            options.put("amount", (amount * 100).toInt()) // in paise

            checkout.open(this, options)

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPaymentSuccess(paymentId: String?) {
        razorpayPaymentId = paymentId ?: ""
        Toast.makeText(this, "Payment Success: $paymentId", Toast.LENGTH_SHORT).show()
        showLoading()
        createOrdersAfterPayment()
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show()
    }

    // ---------------- ORDER ----------------
// ---------------- ORDER & CART CLEANUP ----------------
    private fun createOrdersAfterPayment() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        showLoading()

        if (cartList.isEmpty()) {
            hideLoading()
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val totalItems = cartList.size
        var successCount = 0

        cartList.forEach { item ->
            var finalPrice = item.selling_price
            if (item.negotiation_status == "Negotiation done") finalPrice *= 0.9

            val request = CreateOrderRequest(
                user_id = user.uid,
                product_id = item.id,
                quantity = item.quantity,
                price = finalPrice,
                total_amount = finalPrice * item.quantity,
                color = item.selected_color ?: "#FF0000",
                negotiation_status = item.negotiation_status ?: "Negotiation not done",
                payment_id = razorpayPaymentId,
                payment_status = "Success"
            )

            RetrofitClient.api.createOrder(request)
                .enqueue(object : Callback<OrderResponse> {
                    override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {

                            // Delete cart item from backend
                            RetrofitClient.api.deleteCartItem(item.id)
                                .enqueue(object : Callback<BasicResponse> {
                                    override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                                        cartList.remove(item)
                                        adapter.notifyDataSetChanged()
                                        checkCartEmpty(cartList)

                                        successCount++
                                        if (successCount == totalItems) {
                                            hideLoading()
                                            val intent = Intent(this@my_carts, payment_sucess::class.java)
                                            intent.putExtra("payment_id", razorpayPaymentId)
                                            intent.putExtra("total_amount", totalAmountGlobal)
                                            intent.putExtra("payment_status", "Success")
                                            intent.putExtra("payment_time", System.currentTimeMillis())
                                            startActivity(intent)
                                            finish()
                                        }
                                    }

                                    override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                                        Toast.makeText(this@my_carts, "Failed to remove item from cart", Toast.LENGTH_SHORT).show()
                                    }
                                })

                        } else {
                            hideLoading()
                            Toast.makeText(this@my_carts, "Order failed for ${item.name}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                        hideLoading()
                        Toast.makeText(this@my_carts, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    // ---------------- SWIPE DELETE ----------------
    private fun setupSwipeToDelete() {
        val deleteIcon = ContextCompat.getDrawable(this, R.drawable.delete_icon_white)
        val background = ColorDrawable(Color.RED)

        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = cartList[position]

                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

                // API call to delete
                RetrofitClient.api.deleteCartItem(item.id)
                    .enqueue(object : Callback<BasicResponse> {
                        override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                            if (response.isSuccessful && response.body()?.success == true) {
                                cartList.removeAt(position)
                                adapter.notifyItemRemoved(position)
                                checkCartEmpty(cartList)
                            } else {
                                adapter.notifyItemChanged(position)
                                Toast.makeText(this@my_carts, "Failed to remove item", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                            adapter.notifyItemChanged(position)
                            Toast.makeText(this@my_carts, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView

                if (dX > 0) {
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                } else {
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                }

                background.draw(c)
                deleteIcon?.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(rvItems)
    }
}