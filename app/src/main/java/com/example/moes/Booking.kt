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
    var timestamp: Any? = System.currentTimeMillis()
) : Serializable {
    val timestampLong: Long
        get() = when (timestamp) {
            is Long -> timestamp as Long
            is com.google.firebase.Timestamp -> (timestamp as com.google.firebase.Timestamp).toDate().time
            is java.util.Date -> (timestamp as java.util.Date).time
            is Number -> (timestamp as Number).toLong()
            else -> System.currentTimeMillis()
        }
}
