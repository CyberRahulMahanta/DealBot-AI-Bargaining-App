package com.example.deal.model

data class NotificationItem(
    val id: Int,
    val user_id: String,
    val title: String,
    val message: String,
    val type: String,
    val is_read: Boolean,
    val created_at: String
)