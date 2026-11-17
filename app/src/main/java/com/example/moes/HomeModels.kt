package com.example.moes

import java.io.Serializable

data class Review(
    var reviewId: String = "",
    var reviewerName: String = "",
    var reviewText: String = "",
    var rating: Float = 5.0f,
    var date: String = "",
    var createdAt: Long = System.currentTimeMillis()
) : Serializable

data class QuickAction(
    val title: String,
    val iconResId: Int,
    val actionType: String
)

data class ServiceHighlight(
    val serviceId: String,
    val title: String,
    val description: String,
    val startingPrice: String,
    val imageResId: Int
)

data class StatCardItem(
    val title: String,
    val value: String,
    val iconResId: Int
)
