package com.example.moes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// TattooAdapter for gallery and admin management
class TattooAdapter(
    private val isAdmin: Boolean = false,
    private val onAddTattooClick: (() -> Unit)? = null,
    private val onProductClick: ((Product) -> Unit)? = null,
    private val onOverflowClick: ((Product, View) -> Unit)? = null,
    private val onBookClick: ((Product) -> Unit)? = null,
    private val onWishlistClick: ((Product) -> Unit)? = null,
    private val onAddToCartClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items = listOf<Product>()

    fun submitList(list: List<Product>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_preview_item, parent, false)
        return TattooViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TattooViewHolder) {
            val product = items[position]
            holder.bind(product)
        }
    }

    inner class TattooViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvProductCategory)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val ivImage: ImageView = itemView.findViewById(R.id.ivProductImage)

        fun bind(product: Product) {
            tvName.text = product.name
            tvCategory.text = product.category
            tvPrice.text = "R${String.format("%.2f", product.price)}"

            Glide.with(itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.sparkle_motion)
                .into(ivImage)

            itemView.setOnClickListener {
                onProductClick?.invoke(product)
            }
        }
    }
}

// JewelleryAdapter
class JewelleryAdapter(
    private val isAdmin: Boolean = false,
    private val onAddJewelleryClick: (() -> Unit)? = null,
    private val onProductClick: ((Product) -> Unit)? = null,
    private val onOverflowClick: ((Product, View) -> Unit)? = null,
    private val onBookClick: ((Product) -> Unit)? = null,
    private val onWishlistClick: ((Product) -> Unit)? = null,
    private val onAddToCartClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items = listOf<Product>()

    fun submitList(list: List<Product>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_preview_item, parent, false)
        return JewelleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is JewelleryViewHolder) {
            holder.bind(items[position])
        }
    }

    inner class JewelleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvProductCategory)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val ivImage: ImageView = itemView.findViewById(R.id.ivProductImage)

        fun bind(product: Product) {
            tvName.text = product.name
            tvCategory.text = product.category
            tvPrice.text = "R${String.format("%.2f", product.price)}"

            Glide.with(itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.sparkle_motion)
                .into(ivImage)

            itemView.setOnClickListener {
                onProductClick?.invoke(product)
            }
        }
    }
}

// GalleryAdapter
class GalleryAdapter(
    private val isAdmin: Boolean = false,
    private val onProductClick: ((Product) -> Unit)? = null,
    private val onOverflowClick: ((Product, View) -> Unit)? = null,
    private val onBookClick: ((Product) -> Unit)? = null,
    private val onWishlistClick: ((Product) -> Unit)? = null,
    private val onAddToCartClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    private var items = listOf<Product>()

    fun submitList(list: List<Product>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_preview_item, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvProductCategory)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val ivImage: ImageView = itemView.findViewById(R.id.ivProductImage)

        fun bind(product: Product) {
            tvName.text = product.name
            tvCategory.text = product.category
            tvPrice.text = "R${String.format("%.2f", product.price)}"

            Glide.with(itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.sparkle_motion)
                .into(ivImage)

            itemView.setOnClickListener {
                onProductClick?.invoke(product)
            }
        }
    }
}

// BookingAdapter
class BookingAdapter(
    private val onConfirmClick: (Booking) -> Unit = {},
    private val onDeclineClick: (Booking) -> Unit = {},
    private val onCancelClick: (Booking) -> Unit = {},
    private val onViewDetailsClick: (Booking) -> Unit = {}
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    private var items = listOf<Booking>()

    fun submitList(list: List<Booking>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.booking_preview_item, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        private val tvServiceName: TextView = itemView.findViewById(R.id.tvServiceName)
        private val tvStatusBadge: TextView = itemView.findViewById(R.id.tvStatusBadge)

        fun bind(booking: Booking) {
            tvCustomerName.text = booking.customerName
            tvServiceName.text = "${booking.serviceName} • ${booking.bookingTime}"
            tvStatusBadge.text = booking.status

            itemView.setOnClickListener {
                onViewDetailsClick(booking)
            }
        }
    }
}

// ContactMessageAdapter
class ContactMessageAdapter(
    private val onViewClick: (ContactMessage) -> Unit = {},
    private val onMarkReadClick: (ContactMessage) -> Unit = {},
    private val onMarkResolvedClick: (ContactMessage) -> Unit = {}
) : RecyclerView.Adapter<ContactMessageAdapter.MessageViewHolder>() {

    private var items = listOf<ContactMessage>()

    fun submitList(list: List<ContactMessage>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_preview_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCustomerName: TextView = itemView.findViewById(R.id.tvMessageCustomer)
        private val tvSubject: TextView = itemView.findViewById(R.id.tvMessageSubject)
        private val tvStatusBadge: TextView = itemView.findViewById(R.id.tvMessageStatusBadge)

        fun bind(message: ContactMessage) {
            tvCustomerName.text = message.customerName
            tvSubject.text = message.subject
            tvStatusBadge.text = message.status.name

            itemView.setOnClickListener {
                onViewClick(message)
            }
        }
    }
}
