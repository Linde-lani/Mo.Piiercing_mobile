package com.example.moes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moes.databinding.BookingItemBinding

/**
 * RecyclerView Adapter for displaying and managing customer bookings in the Admin portal.
 * Uses ViewBinding (BookingItemBinding) and ListAdapter with DiffUtil for optimal performance.
 *
 * @param onConfirmClick Callback invoked when admin confirms a pending booking
 * @param onDeclineClick Callback invoked when admin declines a pending booking
 * @param onCancelClick Callback invoked when admin cancels a confirmed booking
 * @param onViewDetailsClick Callback invoked when admin clicks View Details or the card
 */
class BookingAdapter(
    private val onConfirmClick: (Booking) -> Unit,
    private val onDeclineClick: (Booking) -> Unit,
    private val onCancelClick: (Booking) -> Unit,
    private val onViewDetailsClick: (Booking) -> Unit
) : ListAdapter<Booking, BookingAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = BookingItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookingViewHolder(
        private val binding: BookingItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            val context = binding.root.context

            // Bind basic booking & customer details
            binding.tvBookingId.text = booking.bookingId
            binding.tvCustomerName.text = booking.customerName
            binding.tvDateTime.text = "${booking.bookingDate} • ${booking.bookingTime}"
            binding.tvServiceName.text = "Service: ${booking.serviceName}"
            binding.tvCustomerEmail.text = booking.customerEmail
            binding.tvCustomerPhone.text = booking.customerPhone

            // Setup Status Badge (Pending: Orange, Confirmed: Green, Cancelled: Red, Declined: Gray)
            setupStatusBadge(context, booking.status)

            // Setup Action Buttons based on status requirements
            setupActionButtons(booking)

            // Set Click Listeners
            binding.root.setOnClickListener { onViewDetailsClick(booking) }
            binding.btnViewDetails.setOnClickListener { onViewDetailsClick(booking) }
            binding.btnConfirm.setOnClickListener { onConfirmClick(booking) }
            binding.btnDecline.setOnClickListener { onDeclineClick(booking) }
            binding.btnCancelBooking.setOnClickListener { onCancelClick(booking) }
        }

        private fun setupStatusBadge(context: Context, status: String) {
            when (status.lowercase()) {
                "pending" -> {
                    binding.tvStatusBadge.text = "PENDING"
                    binding.tvStatusBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_pending)
                }
                "confirmed" -> {
                    binding.tvStatusBadge.text = "CONFIRMED"
                    binding.tvStatusBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_confirmed)
                }
                "cancelled" -> {
                    binding.tvStatusBadge.text = "CANCELLED"
                    binding.tvStatusBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_cancelled)
                }
                "declined" -> {
                    binding.tvStatusBadge.text = "DECLINED"
                    binding.tvStatusBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_declined)
                }
                else -> {
                    binding.tvStatusBadge.text = status.uppercase()
                    binding.tvStatusBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_declined)
                }
            }
        }

        private fun setupActionButtons(booking: Booking) {
            when (booking.status.lowercase()) {
                "pending" -> {
                    binding.btnConfirm.visibility = View.VISIBLE
                    binding.btnDecline.visibility = View.VISIBLE
                    binding.btnCancelBooking.visibility = View.GONE
                    binding.btnViewDetails.visibility = View.GONE
                }
                "confirmed" -> {
                    binding.btnConfirm.visibility = View.GONE
                    binding.btnDecline.visibility = View.GONE
                    binding.btnCancelBooking.visibility = View.VISIBLE
                    binding.btnViewDetails.visibility = View.GONE
                }
                "declined", "cancelled" -> {
                    binding.btnConfirm.visibility = View.GONE
                    binding.btnDecline.visibility = View.GONE
                    binding.btnCancelBooking.visibility = View.GONE
                    binding.btnViewDetails.visibility = View.VISIBLE
                }
                else -> {
                    binding.btnConfirm.visibility = View.GONE
                    binding.btnDecline.visibility = View.GONE
                    binding.btnCancelBooking.visibility = View.GONE
                    binding.btnViewDetails.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * DiffUtil Callback for calculating item differences efficiently.
     */
    class BookingDiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean {
            return oldItem.bookingId == newItem.bookingId
        }

        override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean {
            return oldItem == newItem
        }
    }
}
