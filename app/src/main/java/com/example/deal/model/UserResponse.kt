package com.example.deal.model

data class ApiResponse(
    val success: Boolean,
    val message: String?,
    val data: UserData?,
    val image_path: String?
)

data class UserData(
    val id: Int,
    val firebase_uid: String,
    val name: String?,
    val email: String,
    val phone: String?,       // nullable
    val birthday: String?,    // nullable
    val address: String?,     // nullable
    val profile_image: String?
)