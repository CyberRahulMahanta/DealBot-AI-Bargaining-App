package com.example.deal.model

data class OrderResponse(
    val success: Boolean,
    val message: String,
    val payment_id: String,
    val payment_status: String
)