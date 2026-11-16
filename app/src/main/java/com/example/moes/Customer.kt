package com.example.moes

import java.io.Serializable

data class Customer(
    var customerId: String = "",
    var customerName: String = "",
    var email: String = "",
    var phone: String = "",
    var registrationDate: String = "",
    var avatarUrl: String = "",
    var totalBookings: Int = 0,
    var totalSpent: Double = 0.0
) : Serializable