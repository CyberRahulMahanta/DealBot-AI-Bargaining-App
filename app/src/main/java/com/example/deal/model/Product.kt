package com.example.deal.model

data class Product(
    val id: Int,
    val name: String,
    val description: String?,
    val selling_price: Double,
    val min_price: Double?,
    val category: String?,
    val brand: String?,
    val features: List<String>?,
    val warranty: String?,
    val stock_quantity: Int?,
    val image_url: String?,
    val popularity_score: Double?
)