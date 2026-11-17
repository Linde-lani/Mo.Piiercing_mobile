package com.example.moes

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moes.databinding.ActivityAdminMessagesBinding
import com.example.moes.databinding.DialogMessageDetailsBinding

/**
 * AdminMessagesActivity provides the Admin interface for managing customer contact messages.
 */
class AdminMessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMessagesBinding
    private lateinit var adapter: ContactMessageAdapter
    private val allMessages = mutableListOf<ContactMessage>()

    private var currentSearchQuery: String = ""
    private var currentFilterStatus: String = "All Messages"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAdminMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadDummyMessagesData()
        setupFilterDropdown()
        setupSearchBar()
        setupRecyclerView()
        applyFilterAndSearch()
    }

    private fun loadDummyMessagesData() {
        allMessages.clear()
        allMessages.addAll(
            listOf(
                ContactMessage(
                    id = "MSG-1001",
                    customerName = "Sarah Connor",
                    email = "sarah.connor@example.com",
                    phone = "+1 (555) 019-2834",
                    subject = "Septum Piercing Aftercare Question",
                    message = "Hi there!\n\nI got my septum pierced at your studio 3 days ago. I was wondering if I should keep cleaning it with saline spray twice a day, or if I should transition to warm water rinses? Also, is slight swelling normal during the first week?\n\nThanks,\nSarah",
                    dateSubmitted = "Oct 24, 2023 • 10:30 AM",
                    status = MessageStatus.NEW
                ),
                ContactMessage(
                    id = "MSG-1002",
                    customerName = "Marcus Wright",
                    email = "marcus.w@example.com",
                    phone = "+1 (555) 014-9821",
                    subject = "Custom Sleeve Tattoo Consultation",
                    message = "Hello Mo Piercings team,\n\nI would like to book a consultation appointment to discuss a custom dark-realism arm sleeve. Do you have availability next Saturday afternoon? What are your deposit requirements?\n\nBest regards,\nMarcus",
                    dateSubmitted = "Oct 23, 2023 • 03:45 PM",
                    status = MessageStatus.READ
                ),
                ContactMessage(
                    id = "MSG-1003",
                    customerName = "Elena Rostova",
                    email = "elena.r@example.com",
                    phone = "+1 (555) 018-7712",
                    subject = "Double Helix Piercing & Titanium Jewelry",
                    message = "Hi, I have a nickel allergy and want to verify if your helix earrings are implant-grade titanium or 14k gold before booking my double helix appointment.\n\nThank you!",
                    dateSubmitted = "Oct 22, 2023 • 11:15 AM",
                    status = MessageStatus.RESOLVED
                ),
                ContactMessage(
                    id = "MSG-1004",
                    customerName = "David Miller",
                    email = "david.m@example.com",
                    phone = "+1 (555) 012-3344",
                    subject = "Rescheduling Appointment #1042",
                    message = "Hello,\n\nSomething came up at work and I won't be able to make my appointment tomorrow at 2 PM. Can we reschedule to Thursday at the same time?\n\nThanks,\nDavid",
                    dateSubmitted = "Oct 24, 2023 • 08:20 AM",
                    status = MessageStatus.NEW
                ),
                ContactMessage(
                    id = "MSG-1005",
                    customerName = "Jessica Taylor",
                    email = "jess.taylor@example.com",
                    phone = "+1 (555) 017-5588",
                    subject = "Nose Ring Sizing & Anodizing Services",
                    message = "Hi! Do you offer in-house anodizing for titanium nostril screws? I have an 18g ring that I'd love to get anodized to rose gold or bronze.\n\nThanks,\nJess",
                    dateSubmitted = "Oct 21, 2023 • 05:00 PM",
                    status = MessageStatus.READ
                )
            )
        )
    }

    private fun setupFilterDropdown() {
        val filterOptions = arrayOf("All Messages", "New", "Read", "Resolved")
        val dropdownAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, filterOptions)

        binding.actvFilter.setAdapter(dropdownAdapter)
        binding.actvFilter.setOnItemClickListener { parent, _, position, _ ->
            currentFilterStatus = parent.getItemAtPosition(position) as String
            applyFilterAndSearch()
        }
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                applyFilterAndSearch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = ContactMessageAdapter(
            onViewClick = { message -> showMessageDetailsDialog(message) },
            onMarkReadClick = { message -> updateMessageStatus(message, MessageStatus.READ) },
            onMarkResolvedClick = { message -> updateMessageStatus(message, MessageStatus.RESOLVED) }
        )

        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = adapter
    }

    private fun applyFilterAndSearch() {
        val filteredList = allMessages.filter { message ->
            val matchesStatus = when (currentFilterStatus) {
                "New" -> message.status == MessageStatus.NEW
                "Read" -> message.status == MessageStatus.READ
                "Resolved" -> message.status == MessageStatus.RESOLVED
                else -> true
            }

            val matchesSearch = if (currentSearchQuery.isEmpty()) {
                true
            } else {
                message.customerName.contains(currentSearchQuery, ignoreCase = true) ||
                        message.email.contains(currentSearchQuery, ignoreCase = true) ||
                        message.subject.contains(currentSearchQuery, ignoreCase = true) ||
                        message.message.contains(currentSearchQuery, ignoreCase = true)
            }

            matchesStatus && matchesSearch
        }

        adapter.submitList(filteredList)

        if (filteredList.isEmpty()) {
            binding.rvMessages.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvMessages.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
        }
    }

    private fun updateMessageStatus(message: ContactMessage, newStatus: MessageStatus) {
        val index = allMessages.indexOfFirst { it.id == message.id }
        if (index != -1) {
            allMessages[index].status = newStatus
            applyFilterAndSearch()

            val statusLabel = when (newStatus) {
                MessageStatus.READ -> "Read"
                MessageStatus.RESOLVED -> "Resolved"
                MessageStatus.NEW -> "New"
            }
            Toast.makeText(this, "Message marked as $statusLabel", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMessageDetailsDialog(message: ContactMessage) {
        if (message.status == MessageStatus.NEW) {
            updateMessageStatus(message, MessageStatus.READ)
        }

        val dialog = Dialog(this)
        val dialogBinding = DialogMessageDetailsBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.dialogCustomerName.text = message.customerName
        dialogBinding.dialogMessageDate.text = "Date: ${message.dateSubmitted}"
        dialogBinding.dialogEmail.text = "Email: ${message.email}"
        dialogBinding.dialogPhone.text = "Phone: ${message.phone}"
        dialogBinding.dialogSubject.text = "Subject: ${message.subject}"
        dialogBinding.dialogFullMessage.text = message.message

        when (message.status) {
            MessageStatus.NEW -> {
                dialogBinding.dialogStatusBadge.text = "NEW"
                dialogBinding.dialogStatusBadge.background = ContextCompat.getDrawable(this, R.drawable.bg_badge_new)
            }
            MessageStatus.READ -> {
                dialogBinding.dialogStatusBadge.text = "READ"
                dialogBinding.dialogStatusBadge.background = ContextCompat.getDrawable(this, R.drawable.bg_badge_read)
            }
            MessageStatus.RESOLVED -> {
                dialogBinding.dialogStatusBadge.text = "RESOLVED"
                dialogBinding.dialogStatusBadge.background = ContextCompat.getDrawable(this, R.drawable.bg_badge_resolved)
            }
        }

        dialogBinding.btnDialogMarkRead.setOnClickListener {
            updateMessageStatus(message, MessageStatus.READ)
            dialog.dismiss()
        }

        dialogBinding.btnDialogMarkResolved.setOnClickListener {
            updateMessageStatus(message, MessageStatus.RESOLVED)
            dialog.dismiss()
        }

        dialogBinding.btnDialogClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
