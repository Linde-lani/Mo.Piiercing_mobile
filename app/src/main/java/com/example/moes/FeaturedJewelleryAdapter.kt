package com.example.moes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class FeaturedJewelleryAdapter(
    private val onBookClick: (Product) -> Unit,
    private val onAddToCartClick: (Product) -> Unit
) : RecyclerView.Adapter<FeaturedJewelleryAdapter.ViewHolder>() {

    private var items = listOf<Product>()

    fun submitList(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_featured_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ShapeableImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvBadge: TextView = itemView.findViewById(R.id.tvBadge)
        private val tvCategoryBadge: TextView = itemView.findViewById(R.id.tvCategoryBadge)
        private val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvProductDesc)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val btnBook: MaterialButton = itemView.findViewById(R.id.btnBookProduct)
        private val btnAddToCart: MaterialButton = itemView.findViewById(R.id.btnAddToCart)

        fun bind(product: Product) {
            tvName.text = product.name
            tvDesc.text = product.description.ifEmpty { "Premium body jewellery piece." }
            tvPrice.text = "R ${"%.2f".format(product.price)}"
            tvCategoryBadge.text = product.category.ifEmpty { "Jewellery" }

            if (product.bestSeller) {
                tvBadge.text = "BEST SELLER"
                tvBadge.visibility = View.VISIBLE
            } else if (product.featured) {
                tvBadge.text = "FEATURED"
                tvBadge.visibility = View.VISIBLE
            } else {
                tvBadge.visibility = View.GONE
            }

            if (product.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.sparkle_motion)
                    .error(R.drawable.sparkle_motion)
                    .into(ivImage)
            } else {
                ivImage.setImageResource(R.drawable.sparkle_motion)
            }

            btnBook.setOnClickListener { onBookClick(product) }
            btnAddToCart.setOnClickListener { onAddToCartClick(product) }
        }
    }
}
