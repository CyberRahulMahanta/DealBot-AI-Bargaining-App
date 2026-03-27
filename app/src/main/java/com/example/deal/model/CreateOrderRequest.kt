package com.example.deal.model

data class CreateOrderRequest(
    val user_id: String,
    val product_id: Int,
    val quantity: Int,
    val price: Double,
    val total_amount: Double,
    val color: String,
    val negotiation_status: String,
    val payment_id: String,
    val payment_status: String
)