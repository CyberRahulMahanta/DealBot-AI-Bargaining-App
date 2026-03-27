package com.example.deal.model

data class CartItem(
    val id: Int,
    val product_id: Int,
    val name: String,
    val selling_price: Double,
    val image_url: String,
    val selected_color: String,
    val negotiation_status: String?,
    var quantity: Int = 1,
)