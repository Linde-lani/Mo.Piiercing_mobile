package com.example.moes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moes.databinding.BookingPreviewItemBinding
import com.example.moes.databinding.CustomerPreviewItemBinding
import com.example.moes.databinding.DashboardStatItemBinding
import com.example.moes.databinding.MessagePreviewItemBinding
import com.example.moes.databinding.ProductPreviewItemBinding
import com.example.moes.databinding.QuickActionItemBinding
import java.util.Locale

/**
 * ListAdapter for horizontal Quick Statistics cards.
 */
class DashboardStatsAdapter(
    private val onItemClick: (DashboardStat) -> Unit = {}
) : ListAdapter<DashboardStat, DashboardStatsAdapter.StatViewHolder>(StatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatViewHolder {
        val binding = DashboardStatItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StatViewHolder(private val binding: DashboardStatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DashboardStat) {
            binding.tvStatTitle.text = item.title
            binding.tvStatValue.text = item.value
            binding.tvStatTrend.text = item.trend
            binding.ivStatIcon.setImageResource(item.iconRes)

            val trendColor = if (item.isPositiveTrend) {
                ContextCompat.getColor(itemView.context, android.R.color.holo_green_light)
            } else {
                ContextCompat.getColor(itemView.context, android.R.color.holo_red_light)
            }
            binding.tvStatTrend.setTextColor(trendColor)

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    class StatDiffCallback : DiffUtil.ItemCallback<DashboardStat>() {
        override fun areItemsTheSame(oldItem: DashboardStat, newItem: DashboardStat): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: DashboardStat, newItem: DashboardStat): Boolean =
            oldItem == newItem
    }
}

/**
 * ListAdapter for horizontal Quick Actions cards.
 */
class QuickActionsAdapter(
    private val onActionClick: (QuickActionItem) -> Unit
) : ListAdapter<QuickActionItem, QuickActionsAdapter.ActionViewHolder>(ActionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        val binding = QuickActionItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ActionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ActionViewHolder(private val binding: QuickActionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: QuickActionItem) {
            binding.tvActionTitle.text = item.title
            binding.ivActionIcon.setImageResource(item.iconRes)

            itemView.setOnClickListener { onActionClick(item) }
        }
    }

    class ActionDiffCallback : DiffUtil.ItemCallback<QuickActionItem>() {
        override fun areItemsTheSame(oldItem: QuickActionItem, newItem: QuickActionItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: QuickActionItem, newItem: QuickActionItem): Boolean =
            oldItem == newItem
    }
}

/**
 * ListAdapter for Today's Appointments & Pending Bookings preview cards.
 */
class BookingPreviewAdapter(
    private val onItemClick: (BookingPreviewItem) -> Unit = {}
) : ListAdapter<BookingPreviewItem, BookingPreviewAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = BookingPreviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookingViewHolder(private val binding: BookingPreviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BookingPreviewItem) {
            binding.tvCustomerName.text = item.customerName
            binding.tvServiceName.text = "${item.serviceName} • ${item.time}"
            binding.tvStatusBadge.text = item.status

            val (bgDrawable, textColor) = when (item.status.lowercase(Locale.ROOT)) {
                "confirmed" -> Pair(R.drawable.bg_badge_confirmed, "#4CAF50")
                "cancelled" -> Pair(R.drawable.bg_badge_cancelled, "#E53935")
                "declined" -> Pair(R.drawable.bg_badge_declined, "#757575")
                else -> Pair(R.drawable.bg_badge_pending, "#FB8C00")
            }

            binding.tvStatusBadge.setBackgroundResource(bgDrawable)
            binding.tvStatusBadge.setTextColor(android.graphics.Color.parseColor(textColor))

            if (item.customerAvatarUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(item.customerAvatarUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(binding.ivBookingAvatar)
            } else {
                binding.ivBookingAvatar.setImageResource(R.drawable.ic_profile_placeholder)
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    class BookingDiffCallback : DiffUtil.ItemCallback<BookingPreviewItem>() {
        override fun areItemsTheSame(oldItem: BookingPreviewItem, newItem: BookingPreviewItem): Boolean =
            oldItem.bookingId == newItem.bookingId

        override fun areContentsTheSame(oldItem: BookingPreviewItem, newItem: BookingPreviewItem): Boolean =
            oldItem == newItem
    }
}

/**
 * ListAdapter for Contact Messages preview list.
 */
class MessagePreviewAdapter(
    private val onItemClick: (MessagePreviewItem) -> Unit = {}
) : ListAdapter<MessagePreviewItem, MessagePreviewAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = MessagePreviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(private val binding: MessagePreviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MessagePreviewItem) {
            binding.tvMessageCustomer.text = item.customerName
            binding.tvMessageSubject.text = item.subject
            binding.tvMessageStatusBadge.text = item.status

            val (bgDrawable, textColor) = when (item.status.uppercase(Locale.ROOT)) {
                "READ" -> Pair(R.drawable.bg_badge_read, "#FB8C00")
                "RESOLVED" -> Pair(R.drawable.bg_badge_resolved, "#4CAF50")
                else -> Pair(R.drawable.bg_badge_new, "#1E88E5")
            }

            binding.tvMessageStatusBadge.setBackgroundResource(bgDrawable)
            binding.tvMessageStatusBadge.setTextColor(android.graphics.Color.parseColor(textColor))

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<MessagePreviewItem>() {
        override fun areItemsTheSame(oldItem: MessagePreviewItem, newItem: MessagePreviewItem): Boolean =
            oldItem.messageId == newItem.messageId

        override fun areContentsTheSame(oldItem: MessagePreviewItem, newItem: MessagePreviewItem): Boolean =
            oldItem == newItem
    }
}

/**
 * ListAdapter for Recent Customer Registrations preview list.
 */
class CustomerPreviewAdapter(
    private val onItemClick: (CustomerPreviewItem) -> Unit = {}
) : ListAdapter<CustomerPreviewItem, CustomerPreviewAdapter.CustomerViewHolder>(CustomerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val binding = CustomerPreviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CustomerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CustomerViewHolder(private val binding: CustomerPreviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CustomerPreviewItem) {
            binding.tvCustomerName.text = item.customerName
            binding.tvRegistrationDate.text = item.registrationDate

            if (item.avatarUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(item.avatarUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(binding.ivCustomerProfile)
            } else {
                binding.ivCustomerProfile.setImageResource(R.drawable.ic_profile_placeholder)
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    class CustomerDiffCallback : DiffUtil.ItemCallback<CustomerPreviewItem>() {
        override fun areItemsTheSame(oldItem: CustomerPreviewItem, newItem: CustomerPreviewItem): Boolean =
            oldItem.customerId == newItem.customerId

        override fun areContentsTheSame(oldItem: CustomerPreviewItem, newItem: CustomerPreviewItem): Boolean =
            oldItem == newItem
    }
}

/**
 * ListAdapter for Product Gallery and Low Stock preview list.
 */
class ProductPreviewAdapter(
    private val onItemClick: (ProductPreviewItem) -> Unit = {}
) : ListAdapter<ProductPreviewItem, ProductPreviewAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProductPreviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(private val binding: ProductPreviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProductPreviewItem) {
            binding.tvProductName.text = item.productName
            binding.tvProductCategory.text = item.category
            binding.tvProductPrice.text = "R${String.format(Locale.US, "%.2f", item.price)}"

            if (item.isLowStock || item.stock <= 5) {
                binding.tvLowStockBadge.visibility = View.VISIBLE
                binding.tvLowStockBadge.text = "LOW (${item.stock})"
            } else {
                binding.tvLowStockBadge.visibility = View.GONE
            }

            Glide.with(itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.sparkle_motion)
                .into(binding.ivProductImage)

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<ProductPreviewItem>() {
        override fun areItemsTheSame(oldItem: ProductPreviewItem, newItem: ProductPreviewItem): Boolean =
            oldItem.productId == newItem.productId

        override fun areContentsTheSame(oldItem: ProductPreviewItem, newItem: ProductPreviewItem): Boolean =
            oldItem == newItem
    }
}
