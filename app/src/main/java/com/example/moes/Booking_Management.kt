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
import com.example.moes.databinding.ActivityBookingManagementBinding
import com.example.moes.databinding.DialogBookingDetailsBinding
import java.io.File

/**
 * Booking_Management Activity handles appointment management for the Mo Piercings Admin Portal.
 *
 * Includes statistics summary cards, status filtering, real-time search, swipe-to-refresh,
 * booking status updates (Confirm, Decline, Cancel), and detailed modal dialog inspect.
 */
class Booking_Management : AppCompatActivity() {

    private lateinit var binding: ActivityBookingManagementBinding
    private lateinit var adapter: BookingAdapter
    private val allBookings = mutableListOf<Booking>()

    private var currentSearchQuery: String = ""
    private var currentFilterStatus: String = "All Bookings"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflate layout using ViewBinding
        binding = ActivityBookingManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Window Insets for edge-to-edge support
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize components
        loadSampleBookingsData()
        setupFilterDropdown()
        setupSearchBar()
        setupSwipeToRefresh()
        setupRecyclerView()

        // Initial render & count update
        updateStatisticsCards()
        applyFilterAndSearch()
    }

    /**
     * Populates initial sample booking records (10 sample bookings).
     */
    private fun loadSampleBookingsData() {
        allBookings.clear()
        allBookings.addAll(
            listOf(
                Booking(
                    bookingId = "#BK-1001",
                    customerName = "Samantha Reed",
                    customerEmail = "samantha.reed@example.com",
                    customerPhone = "+1 (555) 234-5678",
                    serviceName = "Septum Piercing & Titanium Ring",
                    bookingDate = "Oct 26, 2023",
                    bookingTime = "02:30 PM",
                    bookingNotes = "First time getting a septum piercing. Prefer gold anodized titanium jewelry if available.",
                    status = "Pending"
                ),
                Booking(
                    bookingId = "#BK-1002",
                    customerName = "Alexander Vance",
                    customerEmail = "alex.vance@example.com",
                    customerPhone = "+1 (555) 876-5432",
                    serviceName = "Custom Forearm Tattoo Consultation",
                    bookingDate = "Oct 26, 2023",
                    bookingTime = "04:00 PM",
                    bookingNotes = "Bringing reference sketches for a geometric wolf forearm tattoo.",
                    status = "Confirmed"
                ),
                Booking(
                    bookingId = "#BK-1003",
                    customerName = "Chloe Bennett",
                    customerEmail = "chloe.b@example.com",
                    customerPhone = "+1 (555) 345-6789",
                    serviceName = "Double Helix Piercing",
                    bookingDate = "Oct 25, 2023",
                    bookingTime = "11:00 AM",
                    bookingNotes = "Sensitive to nickel; requested implant-grade titanium studs.",
                    status = "Pending"
                ),
                Booking(
                    bookingId = "#BK-1004",
                    customerName = "Jordan Smith",
                    customerEmail = "jordan.s@example.com",
                    customerPhone = "+1 (555) 987-6543",
                    serviceName = "Nose Nostril Piercing & Gold Stud",
                    bookingDate = "Oct 25, 2023",
                    bookingTime = "01:15 PM",
                    bookingNotes = "Customer called to cancel due to schedule conflict.",
                    status = "Cancelled"
                ),
                Booking(
                    bookingId = "#BK-1005",
                    customerName = "Amanda Lopez",
                    customerEmail = "amanda.l@example.com",
                    customerPhone = "+1 (555) 456-7890",
                    serviceName = "Navel / Belly Button Piercing",
                    bookingDate = "Oct 24, 2023",
                    bookingTime = "03:30 PM",
                    bookingNotes = "Returning customer for second piercing.",
                    status = "Confirmed"
                ),
                Booking(
                    bookingId = "#BK-1006",
                    customerName = "Tyler Durden",
                    customerEmail = "tyler.d@example.com",
                    customerPhone = "+1 (555) 654-3210",
                    serviceName = "Blackwork Arm Sleeve Session",
                    bookingDate = "Oct 24, 2023",
                    bookingTime = "05:00 PM",
                    bookingNotes = "Declined: Requested artist unavailable at chosen time slot.",
                    status = "Declined"
                ),
                Booking(
                    bookingId = "#BK-1007",
                    customerName = "Rachel Green",
                    customerEmail = "rachel.g@example.com",
                    customerPhone = "+1 (555) 567-8901",
                    serviceName = "Ear Lobe Piercing (Pair)",
                    bookingDate = "Oct 23, 2023",
                    bookingTime = "10:00 AM",
                    bookingNotes = "Standard earlobe piercing with cubic zirconia studs.",
                    status = "Confirmed"
                ),
                Booking(
                    bookingId = "#BK-1008",
                    customerName = "Ethan Hunt",
                    customerEmail = "ethan.h@example.com",
                    customerPhone = "+1 (555) 765-4321",
                    serviceName = "Tongue Piercing Consultation",
                    bookingDate = "Oct 23, 2023",
                    bookingTime = "02:00 PM",
                    bookingNotes = "Inquiring about anatomy check and downsizing timeline.",
                    status = "Pending"
                ),
                Booking(
                    bookingId = "#BK-1009",
                    customerName = "Sophia Loren",
                    customerEmail = "sophia.l@example.com",
                    customerPhone = "+1 (555) 678-9012",
                    serviceName = "Industrial Scaffolding Piercing",
                    bookingDate = "Oct 22, 2023",
                    bookingTime = "11:30 AM",
                    bookingNotes = "Confirmed appointment with Senior Piercer.",
                    status = "Confirmed"
                ),
                Booking(
                    bookingId = "#BK-1010",
                    customerName = "Lucas Scott",
                    customerEmail = "lucas.s@example.com",
                    customerPhone = "+1 (555) 890-1234",
                    serviceName = "Tooth Gem Application",
                    bookingDate = "Oct 21, 2023",
                    bookingTime = "04:45 PM",
                    bookingNotes = "Cancelled by client via SMS.",
                    status = "Cancelled"
                )
            )
        )
    }

    /**
     * Calculates total, pending, confirmed, and cancelled counts for statistics cards.
     */
    private fun updateStatisticsCards() {
        val totalCount = allBookings.size
        val pendingCount = allBookings.count { it.status.equals("Pending", ignoreCase = true) }
        val confirmedCount = allBookings.count { it.status.equals("Confirmed", ignoreCase = true) }
        val cancelledCount = allBookings.count { it.status.equals("Cancelled", ignoreCase = true) }

        binding.tvCountTotal.text = totalCount.toString()
        binding.tvCountPending.text = pendingCount.toString()
        binding.tvCountConfirmed.text = confirmedCount.toString()
        binding.tvCountCancelled.text = cancelledCount.toString()
    }

    /**
     * Configures status filter dropdown menu (All Bookings, Pending, Confirmed, Cancelled, Declined).
     */
    private fun setupFilterDropdown() {
        val options = arrayOf("All Bookings", "Pending", "Confirmed", "Cancelled", "Declined")
        val dropdownAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options)

        binding.actvFilter.setAdapter(dropdownAdapter)
        binding.actvFilter.setOnItemClickListener { parent, _, position, _ ->
            currentFilterStatus = parent.getItemAtPosition(position) as String
            applyFilterAndSearch()
        }
    }

    /**
     * Configures real-time search input listener.
     */
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

    /**
     * Configures SwipeRefreshLayout for pulling to refresh booking list.
     */
    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(this, R.color.moes_gold),
            ContextCompat.getColor(this, R.color.moes_forest_green)
        )
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(this, R.color.dark_surface)
        )

        binding.swipeRefreshLayout.setOnRefreshListener {
            // Re-sort bookings newest first & re-apply filters
            allBookings.sortByDescending { it.bookingId }
            updateStatisticsCards()
            applyFilterAndSearch()

            binding.swipeRefreshLayout.isRefreshing = false
            Toast.makeText(this, "Bookings list refreshed", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Initializes RecyclerView and adapter callbacks.
     */
    private fun setupRecyclerView() {
        adapter = BookingAdapter(
            onConfirmClick = { booking -> confirmBooking(booking) },
            onDeclineClick = { booking -> declineBooking(booking) },
            onCancelClick = { booking -> cancelBooking(booking) },
            onViewDetailsClick = { booking -> showBookingDetailsDialog(booking) }
        )

        binding.rvBookings.layoutManager = LinearLayoutManager(this)
        binding.rvBookings.adapter = adapter
    }

    /**
     * Filters list based on search text and status dropdown selection, updating adapter & empty state.
     */
    private fun applyFilterAndSearch() {
        val filteredList = allBookings.filter { booking ->
            val matchesStatus = when (currentFilterStatus) {
                "Pending" -> booking.status.equals("Pending", ignoreCase = true)
                "Confirmed" -> booking.status.equals("Confirmed", ignoreCase = true)
                "Cancelled" -> booking.status.equals("Cancelled", ignoreCase = true)
                "Declined" -> booking.status.equals("Declined", ignoreCase = true)
                else -> true // "All Bookings"
            }

            val matchesSearch = if (currentSearchQuery.isEmpty()) {
                true
            } else {
                booking.bookingId.contains(currentSearchQuery, ignoreCase = true) ||
                        booking.customerName.contains(currentSearchQuery, ignoreCase = true) ||
                        booking.customerEmail.contains(currentSearchQuery, ignoreCase = true) ||
                        booking.customerPhone.contains(currentSearchQuery, ignoreCase = true) ||
                        booking.serviceName.contains(currentSearchQuery, ignoreCase = true)
            }

            matchesStatus && matchesSearch
        }

        adapter.submitList(filteredList)

        if (filteredList.isEmpty()) {
            binding.rvBookings.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvBookings.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
        }
    }

    /**
     * Confirms a booking, updating status to "Confirmed".
     */
    private fun confirmBooking(booking: Booking) {
        val index = allBookings.indexOfFirst { it.bookingId == booking.bookingId }
        if (index != -1) {
            allBookings[index].status = "Confirmed"
            updateStatisticsCards()
            applyFilterAndSearch()
            Toast.makeText(this, "Booking ${booking.bookingId} Confirmed!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Declines a booking, updating status to "Declined".
     */
    private fun declineBooking(booking: Booking) {
        val index = allBookings.indexOfFirst { it.bookingId == booking.bookingId }
        if (index != -1) {
            allBookings[index].status = "Declined"
            updateStatisticsCards()
            applyFilterAndSearch()
            Toast.makeText(this, "Booking ${booking.bookingId} Declined.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Cancels a booking, updating status to "Cancelled".
     */
    private fun cancelBooking(booking: Booking) {
        val index = allBookings.indexOfFirst { it.bookingId == booking.bookingId }
        if (index != -1) {
            allBookings[index].status = "Cancelled"
            updateStatisticsCards()
            applyFilterAndSearch()
            Toast.makeText(this, "Booking ${booking.bookingId} Cancelled.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Displays modal View Details dialog for inspecting full booking details.
     */
    private fun showBookingDetailsDialog(booking: Booking) {
        val dialog = Dialog(this)
        val dialogBinding = DialogBookingDetailsBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Bind data
        dialogBinding.dialogBookingId.text = "Booking ID: ${booking.bookingId}"
        dialogBinding.dialogCustomerName.text = booking.customerName
        dialogBinding.dialogServiceName.text = "Service: ${booking.serviceName}"
        dialogBinding.dialogAppointmentDate.text = "Date: ${booking.bookingDate}"
        dialogBinding.dialogAppointmentTime.text = "Time: ${booking.bookingTime}"
        dialogBinding.dialogEmail.text = booking.customerEmail
        dialogBinding.dialogPhone.text = booking.customerPhone
        dialogBinding.dialogBookingNotes.text = booking.bookingNotes.ifEmpty { "No special notes provided." }

        // Configure dialog status badge
        when (booking.status.lowercase()) {
            "pending" -> {
                dialogBinding.dialogStatusBadge.text = "PENDING"
                dialogBinding.dialogStatusBadge.background = ContextCompat.getDrawable(this, R.drawable.bg_badge_pending)
            }
            "confirmed" -> {
                dialogBinding.dialogStatusBadge.text = "CONFIRMED"
                dialogBinding.dialogStatusBadge.background = ContextCompat.getDrawable(this, R.drawable.bg_badge_confirmed)
            }
            "cancelled" -> {
                dialogBinding.dialogStatusBadge.text = "CANCELLED"
                dialogBinding.dialogStatusBadge.background = ContextCompat.getDrawable(this, R.drawable.bg_badge_cancelled)
            }
            "declined" -> {
                dialogBinding.dialogStatusBadge.text = "DECLINED"
                dialogBinding.dialogStatusBadge.background = ContextCompat.getDrawable(this, R.drawable.bg_badge_declined)
            }
        }

        // Configure Dialog action button visibility based on status
        when (booking.status.lowercase()) {
            "pending" -> {
                dialogBinding.btnDialogConfirm.visibility = View.VISIBLE
                dialogBinding.btnDialogDecline.visibility = View.VISIBLE
                dialogBinding.btnDialogCancel.visibility = View.GONE
            }
            "confirmed" -> {
                dialogBinding.btnDialogConfirm.visibility = View.GONE
                dialogBinding.btnDialogDecline.visibility = View.GONE
                dialogBinding.btnDialogCancel.visibility = View.VISIBLE
            }
            else -> {
                dialogBinding.btnDialogConfirm.visibility = View.GONE
                dialogBinding.btnDialogDecline.visibility = View.GONE
                dialogBinding.btnDialogCancel.visibility = View.GONE
            }
        }

        // Action Click Listeners
        dialogBinding.btnDialogConfirm.setOnClickListener {
            confirmBooking(booking)
            dialog.dismiss()
        }

        dialogBinding.btnDialogDecline.setOnClickListener {
            declineBooking(booking)
            dialog.dismiss()
        }

        dialogBinding.btnDialogCancel.setOnClickListener {
            cancelBooking(booking)
            dialog.dismiss()
        }

        dialogBinding.btnDialogClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}

private fun Dialog.setContentView(root: File) {
    TODO("Not yet implemented")
}
