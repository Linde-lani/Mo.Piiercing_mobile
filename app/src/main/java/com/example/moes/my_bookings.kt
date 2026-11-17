package com.example.moes

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moes.databinding.ActivityMyBookingsBinding
import com.example.moes.databinding.DialogBookingDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Customer My Bookings activity.
 * Displays all customer appointments filtered by status tabs,
 * supports booking detail inspection dialog and booking cancellation.
 */
class my_bookings : AppCompatActivity() {

    private lateinit var binding: ActivityMyBookingsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var adapter: CustomerBookingsAdapter
    private val allBookings = mutableListOf<Booking>()
    private var currentFilterStatus: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginScreen::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        binding = ActivityMyBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupFilterChips()
        setupClickListeners()
        setupNavigation(R.id.nav_booking)

        loadCustomerBookings(currentUser.email ?: "")
    }

    private fun setupRecyclerView() {
        adapter = CustomerBookingsAdapter(
            onDetailsClick = { booking -> showBookingDetailsModal(booking) },
            onCancelClick = { booking -> cancelBooking(booking) }
        )
        binding.rvMyBookings.layoutManager = LinearLayoutManager(this)
        binding.rvMyBookings.adapter = adapter
    }

    private fun setupFilterChips() {
        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilterStatus = when {
                checkedIds.contains(R.id.chipPending) -> "Pending"
                checkedIds.contains(R.id.chipConfirmed) -> "Confirmed"
                checkedIds.contains(R.id.chipCompleted) -> "Completed"
                checkedIds.contains(R.id.chipCancelled) -> "Cancelled"
                else -> "All"
            }
            applyFilter()
        }
    }

    private fun setupClickListeners() {
        binding.btnNewBooking.setOnClickListener {
            startActivity(Intent(this, BookingScreen::class.java))
        }

        binding.btnEmptyBookNow.setOnClickListener {
            startActivity(Intent(this, BookingScreen::class.java))
        }
    }

    private fun loadCustomerBookings(userEmail: String) {
        db.collection("bookings")
            .whereEqualTo("customerEmail", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                allBookings.clear()
                for (doc in documents) {
                    val booking = doc.toObject(Booking::class.java)
                    booking.bookingId = doc.id
                    allBookings.add(booking)
                }
                allBookings.sortByDescending { it.bookingDate }
                applyFilter()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load bookings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun applyFilter() {
        val filtered = if (currentFilterStatus == "All") {
            allBookings
        } else {
            allBookings.filter { it.status.equals(currentFilterStatus, ignoreCase = true) }
        }

        adapter.submitList(filtered)
        if (filtered.isEmpty()) {
            binding.layoutEmptyBookings.visibility = View.VISIBLE
            binding.rvMyBookings.visibility = View.GONE
        } else {
            binding.layoutEmptyBookings.visibility = View.GONE
            binding.rvMyBookings.visibility = View.VISIBLE
        }
    }

    private fun cancelBooking(booking: Booking) {
        db.collection("bookings").document(booking.bookingId)
            .update("status", "Cancelled")
            .addOnSuccessListener {
                Toast.makeText(this, "Booking Cancelled.", Toast.LENGTH_SHORT).show()
                booking.status = "Cancelled"
                applyFilter()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to cancel: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showBookingDetailsModal(booking: Booking) {
        val dialog = Dialog(this)
        val dialogBinding = DialogBookingDetailsBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.dialogBookingId.text = "Booking ID: ${booking.bookingId}"
        dialogBinding.dialogCustomerName.text = booking.customerName
        dialogBinding.dialogServiceName.text = "Service: ${booking.serviceName}"
        dialogBinding.dialogAppointmentDate.text = "Date: ${booking.bookingDate}"
        dialogBinding.dialogAppointmentTime.text = "Time: ${booking.bookingTime}"
        dialogBinding.dialogEmail.text = booking.customerEmail
        dialogBinding.dialogPhone.text = booking.customerPhone
        dialogBinding.dialogBookingNotes.text = booking.bookingNotes.ifEmpty { "No special notes provided." }
        dialogBinding.dialogStatusBadge.text = booking.status

        dialogBinding.btnDialogClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnDialogCancel.setOnClickListener {
            dialog.dismiss()
            cancelBooking(booking)
        }

        dialog.show()
    }

    private fun setupNavigation(currentItemId: Int) {
        binding.bottomNavigation.selectedItemId = currentItemId
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == currentItemId) return@setOnItemSelectedListener true
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_booking -> {
                    startActivity(Intent(this, BookingScreen::class.java))
                    finish()
                    true
                }
                R.id.nav_gallery -> {
                    startActivity(Intent(this, GalleryScreen::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileScreen::class.java))
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsScreen::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
