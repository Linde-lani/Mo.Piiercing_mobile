package com.example.moes

import androidx.annotation.DrawableRes

/**
 * Data model for horizontal Quick Statistics cards on the Admin Dashboard.
 */
data class DashboardStat(
    val id: String = "",
    @DrawableRes val iconRes: Int = R.drawable.ic_calendar_24,
    val title: String = "",
    val value: String = "0",
    val trend: String = "",
    val isPositiveTrend: Boolean = true
)

/**
 * Data model for horizontal Quick Action items.
 */
data class QuickActionItem(
    val id: String = "",
    @DrawableRes val iconRes: Int = R.drawable.ic_plus,
    val title: String = "",
    val actionType: ActionType = ActionType.NEW_BOOKING
)

enum class ActionType {
    NEW_BOOKING,
    ADD_TATTOO,
    ADD_JEWELLERY,
    VIEW_MESSAGES,
    VIEW_CUSTOMERS
}

/**
 * Data model for Today's Appointments & Pending Bookings preview cards.
 */
data class BookingPreviewItem(
    val bookingId: String = "",
    val customerName: String = "",
    val serviceName: String = "",
    val time: String = "",
    val status: String = "Pending",
    val customerAvatarUrl: String = ""
)

/**
 * Data model for Contact Messages preview items.
 */
data class MessagePreviewItem(
    val messageId: String = "",
    val customerName: String = "",
    val subject: String = "",
    val status: String = "NEW",
    val dateSubmitted: String = ""
)

/**
 * Data model for Recent Customer Registrations preview items.
 */
data class CustomerPreviewItem(
    val customerId: String = "",
    val customerName: String = "",
    val registrationDate: String = "",
    val avatarUrl: String = ""
)

/**
 * Data model for Gallery Product & Low Stock preview items.
 */
data class ProductPreviewItem(
    val productId: String = "",
    val productName: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val stock: Int = 0,
    val isLowStock: Boolean = false
)

/**
 * Data model for Business Insights section.
 */
data class BusinessInsights(
    val mostPopularTattoo: String = "Japanese Dragon",
    val mostPopularJewellery: String = "Gold Opal Helix Stud",
    val peakBookingDay: String = "Saturday (2 PM - 6 PM)",
    val averageCustomerRating: String = "4.9 / 5.0",
    val bestSellingProduct: String = "Titanium Septum Ring",
    val topSpendingCustomer: String = "Marcus Wright (R4,200)"
)
