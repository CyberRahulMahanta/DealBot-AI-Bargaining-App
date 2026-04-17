package com.example.deal.model

data class CouponValidateResponse(
    val success: Boolean,
    val valid: Boolean,
    val code: String?,
    val discount_amount: Double,
    val message: String
)