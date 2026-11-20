package com.example.moes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moes.databinding.ItemCustomerProductBinding

/**
 * Adapter for Featured Products horizontal lists.
 */
class FeaturedProductsAdapter(
    private val onProductClick: (Product) -> Unit = {},
    private val onBookClick: (Product) -> Unit = {},
    private val onAddToCartClick: (Product) -> Unit = {},
    private val onWishlistClick: (Product) -> Unit = {}
) : ListAdapter<Product, FeaturedProductsAdapter.FeaturedViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedViewHolder {
        val binding = ItemCustomerProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FeaturedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeaturedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FeaturedViewHolder(private val binding: ItemCustomerProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductDesc.text = product.description
            binding.tvProductPrice.text = "R${String.format("%.2f", product.price)}"

            binding.badgeFeatured.visibility = if (product.featured) View.VISIBLE else View.GONE
            binding.badgeNew.visibility = if (product.isNew) View.VISIBLE else View.GONE

            Glide.with(binding.root.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.jewelry_1)
                .error(R.drawable.jewelry_1)
                .into(binding.ivProductImage)

            binding.root.setOnClickListener { onProductClick(product) }
            binding.btnBook.setOnClickListener { onBookClick(product) }
            binding.btnAddToCart.setOnClickListener { onAddToCartClick(product) }
            binding.btnWishlistHeart.setOnClickListener { onWishlistClick(product) }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem.productId == newItem.productId
        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem == newItem
    }
}
