package com.example.deal.model

import com.example.deal.model.Product

data class ProductResponse(
    val success: Boolean,
    val message: String,
    val data: Product?,
    val error: String?,
    val datas: List<Product>
)