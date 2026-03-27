package com.example.deal.model

data class CartResponse(
    val success: Boolean,
    val data: List<CartItem>
)