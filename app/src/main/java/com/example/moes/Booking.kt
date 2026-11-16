package com.example.moes

import java.io.Serializable

data class Booking(
    var bookingId: String = "",
    var userId: String = "",
    var customerName: String = "",
    var customerEmail: String = "",
    var customerPhone: String = "",
    var serviceName: String = "",
    var bookingDate: String = "",
    var bookingTime: String = "",
    var bookingNotes: String = "",
    var status: String = "Pending",
    var timestamp: Long = System.currentTimeMillis()
) : Serializable
