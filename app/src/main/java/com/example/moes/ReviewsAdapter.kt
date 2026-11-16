package com.example.moes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReviewsAdapter : RecyclerView.Adapter<ReviewsAdapter.ViewHolder>() {

    private var items = listOf<Review>()

    fun submitList(newItems: List<Review>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvReviewerName)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val tvText: TextView = itemView.findViewById(R.id.tvReviewText)
        private val tvDate: TextView = itemView.findViewById(R.id.tvReviewDate)

        fun bind(review: Review) {
            tvName.text = review.reviewerName.ifEmpty { "Anonymous Studio Client" }
            ratingBar.rating = review.rating
            tvText.text = review.reviewText
            tvDate.text = review.date.ifEmpty { "Recently" }
        }
    }
}
