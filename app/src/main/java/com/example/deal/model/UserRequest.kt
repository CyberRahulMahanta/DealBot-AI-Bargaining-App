package com.example.deal.model

data class UserRequest(
    val name: String?,
    val email: String?,
    val phone: String?,
    val birthday: String?,
    val address: String?,
    val profile_image: String? = null
)