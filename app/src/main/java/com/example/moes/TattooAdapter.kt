package com.example.moes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moes.databinding.ItemAdminAddTattooBinding
import com.example.moes.databinding.ItemAdminProductBinding
import com.example.moes.databinding.ItemCustomerProductBinding

/**
 * RecyclerView Adapter for Tattoos sections in both Admin and Customer views.
 *
 * In Admin mode (isAdmin = true), position 0 is always the "+ Add New Tattoo" card.
 */
class TattooAdapter(
    private val isAdmin: Boolean = false,
    private val onAddTattooClick: () -> Unit = {},
    private val onProductClick: (Product) -> Unit = {},
    private val onOverflowClick: (Product, View) -> Unit = { _, _ -> },
    private val onBookClick: (Product) -> Unit = {},
    private val onAddToCartClick: (Product) -> Unit = {},
    private val onWishlistClick: (Product) -> Unit = {}
) : ListAdapter<Product, RecyclerView.ViewHolder>(ProductDiffCallback()) {

    companion object {
        private const val TYPE_ADD_CARD = 0
        private const val TYPE_PRODUCT_CARD = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (isAdmin && position == 0) TYPE_ADD_CARD else TYPE_PRODUCT_CARD
    }

    override fun getItemCount(): Int {
        return if (isAdmin) super.getItemCount() + 1 else super.getItemCount()
    }

    private fun getProductAt(position: Int): Product {
        return getItem(if (isAdmin) position - 1 else position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ADD_CARD) {
            val binding = ItemAdminAddTattooBinding.inflate(inflater, parent, false)
            AddTattooViewHolder(binding)
        } else if (isAdmin) {
            val binding = ItemAdminProductBinding.inflate(inflater, parent, false)
            AdminTattooViewHolder(binding)
        } else {
            val binding = ItemCustomerProductBinding.inflate(inflater, parent, false)
            CustomerTattooViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AddTattooViewHolder) {
            holder.bind()
        } else if (holder is AdminTattooViewHolder) {
            holder.bind(getProductAt(position))
        } else if (holder is CustomerTattooViewHolder) {
            holder.bind(getProductAt(position))
        }
    }

    inner class AddTattooViewHolder(private val binding: ItemAdminAddTattooBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.cardAddTattoo.setOnClickListener { onAddTattooClick() }
            binding.btnAddTattooAction.setOnClickListener { onAddTattooClick() }
        }
    }

    inner class AdminTattooViewHolder(private val binding: ItemAdminProductBinding) :
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
                .placeholder(R.drawable.tatto_1)
                .error(R.drawable.tatto_1)
                .into(binding.ivProductImage)

            binding.root.setOnClickListener { onProductClick(product) }
            binding.btnOverflowMenu.setOnClickListener { view -> onOverflowClick(product, view) }
        }
    }

    inner class CustomerTattooViewHolder(private val binding: ItemCustomerProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductDesc.text = product.description
            binding.tvProductPrice.text = "R${String.format("%.2f", product.price)}"

            binding.badgeFeatured.visibility = if (product.featured) View.VISIBLE else View.GONE
            binding.badgeNew.visibility = if (product.isNew) View.VISIBLE else View.GONE

            Glide.with(binding.root.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.tatto_1)
                .error(R.drawable.tatto_1)
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
