package com.example.moes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class CustomerBookingsAdapter(
    private val onDetailsClick: (Booking) -> Unit,
    private val onCancelClick: (Booking) -> Unit
) : RecyclerView.Adapter<CustomerBookingsAdapter.ViewHolder>() {

    private var items = listOf<Booking>()

    fun submitList(newItems: List<Booking>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dialog_booking_details, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvId: TextView = itemView.findViewById(R.id.dialogBookingId)
        private val tvCustomer: TextView = itemView.findViewById(R.id.dialogCustomerName)
        private val tvService: TextView = itemView.findViewById(R.id.dialogServiceName)
        private val tvDate: TextView = itemView.findViewById(R.id.dialogAppointmentDate)
        private val tvTime: TextView = itemView.findViewById(R.id.dialogAppointmentTime)
        private val tvStatus: TextView = itemView.findViewById(R.id.dialogStatusBadge)
        private val btnCancel: MaterialButton = itemView.findViewById(R.id.btnDialogCancel)
        private val btnClose: MaterialButton = itemView.findViewById(R.id.btnDialogClose)

        fun bind(booking: Booking) {
            tvId.text = "Booking #${booking.bookingId.take(8)}"
            tvCustomer.text = booking.customerName.ifEmpty { "Client" }
            tvService.text = booking.serviceName
            tvDate.text = "Date: ${booking.bookingDate}"
            tvTime.text = "Time: ${booking.bookingTime}"
            tvStatus.text = booking.status

            val isCancelable = booking.status.equals("Pending", ignoreCase = true) ||
                    booking.status.equals("Confirmed", ignoreCase = true)
            btnCancel.visibility = if (isCancelable) View.VISIBLE else View.GONE

            btnClose.text = "Details"
            btnClose.setOnClickListener { onDetailsClick(booking) }
            btnCancel.setOnClickListener { onCancelClick(booking) }
        }
    }
}
