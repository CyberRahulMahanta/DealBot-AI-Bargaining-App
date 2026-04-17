package com.example.deal.model

data class ShippingMethodsResponse(
    val success: Boolean,
    val data: List<ShippingMethodItem>
)