package com.example.moes

import java.io.Serializable

/**
 * Represents the status of a customer message in the admin system.
 */
enum class MessageStatus {
    NEW,
    READ,
    RESOLVED
}

/**
 * Data model for a customer contact message submitted through the Contact Us screen.
 *
 * @param id Unique message identifier
 * @param customerName Full name of the customer
 * @param email Customer's contact email address
 * @param phone Customer's phone number
 * @param subject Subject title of the inquiry
 * @param message Complete body message
 * @param dateSubmitted Formatted date/time when message was received
 * @param status Current processing status (NEW, READ, or RESOLVED)
 */
data class ContactMessage(
    val id: String,
    val customerName: String,
    val email: String,
    val phone: String,
    val subject: String,
    val message: String,
    val dateSubmitted: String,
    var status: MessageStatus = MessageStatus.NEW
) : Serializable
