package com.example.deal

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deal.model.OrderItem
import com.example.deal.model.OrdersResponse
import com.example.deal.network.RetrofitClient
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class my_orders : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: OrderAdapter
    private val orderList = mutableListOf<OrderItem>()
    private lateinit var layoutEmptyState: LinearLayout

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_orders)

        // Back button
        val btnBack: MaterialCardView = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // RecyclerView
        recycler = findViewById(R.id.recyclerOrders)
        recycler.layoutManager = LinearLayoutManager(this)

        // Empty state layout
        layoutEmptyState = findViewById(R.id.layoutEmptyState)

        val user = auth.currentUser
        if (user != null) {
            adapter = OrderAdapter(orderList, user.uid)
            recycler.adapter = adapter
            fetchOrders(user.uid)
        } else {
            showEmptyState()
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchOrders(userId: String) {
        RetrofitClient.api.getOrders(userId)
            .enqueue(object : Callback<OrdersResponse> {
                override fun onResponse(
                    call: Call<OrdersResponse>,
                    response: Response<OrdersResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        orderList.clear()
                        val data = response.body()?.data ?: emptyList()
                        if (data.isNotEmpty()) {
                            orderList.addAll(data)
                            adapter.notifyDataSetChanged()
                            showOrders()
                        } else {
                            showEmptyState()
                        }
                    } else {
                        showEmptyState()
                        Toast.makeText(this@my_orders, "No orders found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<OrdersResponse>, t: Throwable) {
                    showEmptyState()
                    Toast.makeText(this@my_orders, "API Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Show orders RecyclerView
    private fun showOrders() {
        recycler.visibility = View.VISIBLE
        layoutEmptyState.visibility = View.GONE
    }

    // Show empty state layout
    private fun showEmptyState() {
        recycler.visibility = View.GONE
        layoutEmptyState.visibility = View.VISIBLE
    }
}