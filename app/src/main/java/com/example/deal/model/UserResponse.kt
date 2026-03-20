package com.example.deal.model

data class ApiResponse(
    val success: Boolean,
    val message: String?,
    val data: UserData?
)

data class UserData(
    val id: Int,
    val firebase_uid: String,
    val name: String,
    val email: String
)