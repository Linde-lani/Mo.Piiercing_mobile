package com.example.moes

import java.io.Serializable

/**
 * Data model for an appointment booking in the Mo Piercings system.
 *
 * @param bookingId Unique identifier (e.g. #BK-1001)
 * @param customerName Customer's full name
 * @param customerEmail Customer's email address
 * @param customerPhone Customer's phone number
 * @param serviceName Booked service/piercing name
 * @param bookingDate Scheduled appointment date
 * @param bookingTime Scheduled appointment time slot
 * @param bookingNotes Additional customer instructions or notes
 * @param status Current booking status ("Pending", "Confirmed", "Cancelled", "Declined")
 */
data class Booking(
    val bookingId: String,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val serviceName: String,
    val bookingDate: String,
    val bookingTime: String,
    val bookingNotes: String,
    var status: String
) : Serializable
