package com.example.moes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moes.databinding.ItemAdminProductBinding
import com.example.moes.databinding.ItemCustomerProductBinding

/**
 * Universal RecyclerView Adapter for Customer & Admin Product Lists.
 */
class GalleryAdapter(
    private val isAdmin: Boolean = false,
    private val onProductClick: (Product) -> Unit = {},
    private val onOverflowClick: (Product, View) -> Unit = { _, _ -> },
    private val onBookClick: (Product) -> Unit = {},
    private val onAddToCartClick: (Product) -> Unit = {},
    private val onWishlistClick: (Product) -> Unit = {}
) : ListAdapter<Product, RecyclerView.ViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (isAdmin) {
            val binding = ItemAdminProductBinding.inflate(inflater, parent, false)
            AdminViewHolder(binding)
        } else {
            val binding = ItemCustomerProductBinding.inflate(inflater, parent, false)
            CustomerViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val product = getItem(position)
        if (holder is AdminViewHolder) {
            holder.bind(product)
        } else if (holder is CustomerViewHolder) {
            holder.bind(product)
        }
    }

    inner class AdminViewHolder(private val binding: ItemAdminProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductCategory.text = product.category
            binding.tvProductPrice.text = "R${String.format("%.2f", product.price)}"
            binding.tvStockCount.text = "Stock: ${product.stock}"

            binding.badgeFeatured.visibility = if (product.featured) View.VISIBLE else View.GONE
            binding.badgeLowStock.visibility = if (product.stock <= 5) View.VISIBLE else View.GONE
            binding.badgeHidden.visibility = if (!product.active) View.VISIBLE else View.GONE

            Glide.with(binding.root.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.septum)
                .error(R.drawable.septum)
                .into(binding.ivProductImage)

            binding.root.setOnClickListener { onProductClick(product) }
            binding.btnOverflowMenu.setOnClickListener { view -> onOverflowClick(product, view) }
        }
    }

    inner class CustomerViewHolder(private val binding: ItemCustomerProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductDesc.text = product.description
            binding.tvProductPrice.text = "R${String.format("%.2f", product.price)}"

            binding.badgeFeatured.visibility = if (product.featured) View.VISIBLE else View.GONE
            binding.badgeNew.visibility = if (product.isNew) View.VISIBLE else View.GONE

            Glide.with(binding.root.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.septum)
                .error(R.drawable.septum)
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
