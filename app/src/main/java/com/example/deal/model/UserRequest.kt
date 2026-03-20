package com.example.deal.model

data class UserRequest(
    val id: Int,
    val firebase_uid: String,
    val name: String?,
    val email: String?,
    val phone: String?,
    val birthday: String?,
    val address: String?
)