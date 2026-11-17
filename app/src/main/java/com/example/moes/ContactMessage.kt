package com.example.moes

import java.io.Serializable

enum class MessageStatus {
    NEW,
    READ,
    RESOLVED
}

data class ContactMessage(
    var id: String = "",
    var customerName: String = "",
    var email: String = "",
    var phone: String = "",
    var subject: String = "",
    var message: String = "",
    var dateSubmitted: String = "",
    var status: MessageStatus = MessageStatus.NEW
) : Serializable