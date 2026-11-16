package com.example.moes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomerAdapter(
    private val onItemClick: (Customer) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.ViewHolder>() {

    private var items = listOf<Customer>()

    fun submitList(newItems: List<Customer>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.customer_preview_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvCustomerName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvRegistrationDate)

        fun bind(customer: Customer) {
            tvName.text = customer.customerName.ifEmpty { "Client #${customer.customerId.take(6)}" }
            tvDate.text = customer.email
            itemView.setOnClickListener { onItemClick(customer) }
        }
    }
}
