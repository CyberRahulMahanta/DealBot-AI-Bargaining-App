package com.example.deal.model

data class Order(
    val productName: String,
    val amount: Double,
    val paymentId: String,
    val status: String,
    val time: String,
    val productImage: String
)