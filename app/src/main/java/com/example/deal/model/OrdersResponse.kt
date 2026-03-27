package com.example.deal.model

data class OrdersResponse(
    val success: Boolean,
    val data: List<OrderItem>
)

data class OrderItem(
    val id: Int,
    val product_name: String,
    val product_image: String,
    val total_amount: Double,
    val payment_id: String,
    val payment_status: String,
    val payment_time: String
)