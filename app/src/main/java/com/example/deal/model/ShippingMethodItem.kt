package com.example.deal.model
data class ShippingMethodItem(
    val id: Int,
    val name: String,
    val description: String?,
    val amount: Double,
    val estimated_time: String?
)