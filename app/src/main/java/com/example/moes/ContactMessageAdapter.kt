package com.example.moes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moes.databinding.MessageItemBinding

/**
 * RecyclerView Adapter for displaying customer contact messages in the Admin portal.
 * Uses ViewBinding (MessageItemBinding) and ListAdapter with DiffUtil for optimal performance.
 *
 * @param onViewClick Callback invoked when admin clicks View Message or the card
 * @param onMarkReadClick Callback invoked when admin clicks Mark as Read
 * @param onMarkResolvedClick Callback invoked when admin clicks Mark as Resolved
 */
class ContactMessageAdapter(
    private val onViewClick: (ContactMessage) -> Unit,
    private val onMarkReadClick: (ContactMessage) -> Unit,
    private val onMarkResolvedClick: (ContactMessage) -> Unit
) : ListAdapter<ContactMessage, ContactMessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = MessageItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(
        private val binding: MessageItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ContactMessage) {
            val context = binding.root.context

            // Bind basic text details
            binding.tvCustomerName.text = message.customerName
            binding.tvDateSubmitted.text = message.dateSubmitted
            binding.tvSubject.text = "Subject: ${message.subject}"
            binding.tvEmail.text = message.email
            binding.tvPhone.text = message.phone
            binding.tvMessagePreview.text = message.message

            // Configure Status Badge (New: Blue, Read: Orange, Resolved: Green)
            setupStatusBadge(context, message.status)

            // Configure Action Buttons based on status
            setupActionButtons(message)

            // Set click listeners
            binding.root.setOnClickListener { onViewClick(message) }
            binding.btnViewMessage.setOnClickListener { onViewClick(message) }
            binding.btnMarkRead.setOnClickListener { onMarkReadClick(message) }
            binding.btnMarkResolved.setOnClickListener { onMarkResolvedClick(message) }
        }

        private fun setupStatusBadge(context: Context, status: MessageStatus) {
            when (status) {
                MessageStatus.NEW -> {
                    binding.tvStatusBadge.text = "NEW"
                    binding.tvStatusBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_new)
                }
                MessageStatus.READ -> {
                    binding.tvStatusBadge.text = "READ"
                    binding.tvStatusBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_read)
                }
                MessageStatus.RESOLVED -> {
                    binding.tvStatusBadge.text = "RESOLVED"
                    binding.tvStatusBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_resolved)
                }
            }
        }

        private fun setupActionButtons(message: ContactMessage) {
            when (message.status) {
                MessageStatus.NEW -> {
                    binding.btnMarkRead.visibility = View.VISIBLE
                    binding.btnMarkRead.isEnabled = true
                    binding.btnMarkResolved.visibility = View.VISIBLE
                    binding.btnMarkResolved.isEnabled = true
                }
                MessageStatus.READ -> {
                    // Already read, hide/disable Mark Read button
                    binding.btnMarkRead.visibility = View.GONE
                    binding.btnMarkResolved.visibility = View.VISIBLE
                    binding.btnMarkResolved.isEnabled = true
                }
                MessageStatus.RESOLVED -> {
                    // Already resolved, hide quick state buttons
                    binding.btnMarkRead.visibility = View.GONE
                    binding.btnMarkResolved.visibility = View.GONE
                }
            }
        }
    }

    /**
     * DiffUtil Callback for efficiently updating message items without full rebinds.
     */
    class MessageDiffCallback : DiffUtil.ItemCallback<ContactMessage>() {
        override fun areItemsTheSame(oldItem: ContactMessage, newItem: ContactMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ContactMessage, newItem: ContactMessage): Boolean {
            return oldItem == newItem
        }
    }
}
