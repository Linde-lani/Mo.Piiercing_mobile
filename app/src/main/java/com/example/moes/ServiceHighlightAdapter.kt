package com.example.moes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class ServiceHighlightAdapter(
    private val onBookClick: (ServiceHighlight) -> Unit
) : RecyclerView.Adapter<ServiceHighlightAdapter.ViewHolder>() {

    private var items = listOf<ServiceHighlight>()

    fun submitList(newItems: List<ServiceHighlight>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_highlight, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivServiceImage)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvServiceTitle)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvServiceDesc)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvStartingPrice)
        private val btnBook: MaterialButton = itemView.findViewById(R.id.btnBookService)

        fun bind(service: ServiceHighlight) {
            tvTitle.text = service.title
            tvDesc.text = service.description
            tvPrice.text = service.startingPrice
            ivImage.setImageResource(service.imageResId)
            btnBook.setOnClickListener { onBookClick(service) }
        }
    }
}
