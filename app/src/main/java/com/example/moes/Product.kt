package com.example.moes

import java.io.Serializable

/**
 * Data model for products and services (Tattoos & Jewellery) in Mo Piercings.
 * Maps directly to the Firestore `products` collection documents.
 */
data class Product(
    var productId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "Tattoos",
    val stock: Int = 10,
    val featured: Boolean = false,
    val active: Boolean = true,
    val isNew: Boolean = false,
    val bestSeller: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

/**
 * Data model for customer ratings & reviews attached to products.
 */
data class ProductReview(
    var reviewId: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Float = 5.0f,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
