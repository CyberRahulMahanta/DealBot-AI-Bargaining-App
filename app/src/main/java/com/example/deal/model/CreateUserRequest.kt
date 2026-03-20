package com.example.deal.model

data class CreateUserRequest(
    val firebase_uid: String,
    val name: String,
    val email: String
)