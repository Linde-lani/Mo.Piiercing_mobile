package com.example.moes

import java.io.Serializable

data class Product(
    var productId: String = "",
    var name: String = "",
    var description: String = "",
    var price: Double = 0.0,
    var imageUrl: String = "",
    var category: String = "",
    var stock: Int = 0,
    var featured: Boolean = false,
    var active: Boolean = true,
    var isNew: Boolean = false,
    var bestSeller: Boolean = false,
    var createdAt: Long = System.currentTimeMillis()
) : Serializable