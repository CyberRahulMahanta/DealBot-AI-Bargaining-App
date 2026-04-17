package com.example.deal.model

data class NotificationRequest(
    val user_id: String,
    val title: String,
    val message: String,
    val type: String
)