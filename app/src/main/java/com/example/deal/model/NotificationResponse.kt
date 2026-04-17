package com.example.deal.model

data class NotificationResponse(
    val success: Boolean,
    val data: List<NotificationItem>
)