package com.example.moes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomerMessagesAdapter(
    private val onItemClick: (ContactMessage) -> Unit
) : RecyclerView.Adapter<CustomerMessagesAdapter.ViewHolder>() {

    private var items = listOf<ContactMessage>()

    fun submitList(newItems: List<ContactMessage>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_preview_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSubject: TextView = itemView.findViewById(R.id.tvMessageSubject)
        private val tvCustomer: TextView = itemView.findViewById(R.id.tvMessageCustomer)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvMessageStatusBadge)

        fun bind(message: ContactMessage) {
            tvSubject.text = message.subject
            tvCustomer.text = message.customerName.ifEmpty { message.email }
            tvStatus.text = message.status.name
            itemView.setOnClickListener { onItemClick(message) }
        }
    }
}
