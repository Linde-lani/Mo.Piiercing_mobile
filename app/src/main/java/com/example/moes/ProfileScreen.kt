package com.example.moes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_screen)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupUserProfile()
        setupActionButtons()
        setupBottomNavigation()
        loadUserStats()
        loadUpcomingAppointments()

        findViewById<View>(R.id.btnEditProfile).setOnClickListener {
            Toast.makeText(this, "Profile settings updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            val name = user.displayName ?: user.email?.substringBefore("@") ?: "Studio Customer"
            findViewById<TextView>(R.id.tvProfileName).text = name
            findViewById<TextView>(R.id.tvProfileEmail).text = user.email ?: "email@mopiercings.com"
        }
    }

    private fun setupActionButtons() {
        findViewById<Button>(R.id.btnActionBookings).setOnClickListener {
            startActivity(Intent(this, my_bookings::class.java))
        }

        findViewById<Button>(R.id.btnActionWishlist).setOnClickListener {
            startActivity(Intent(this, WishlistActivity::class.java))
        }

        findViewById<Button>(R.id.btnActionCart).setOnClickListener {
            startActivity(Intent(this, cart::class.java))
        }

        findViewById<TextView>(R.id.tvViewAllAppointments).setOnClickListener {
            startActivity(Intent(this, my_bookings::class.java))
        }
    }

    private fun loadUserStats() {
        val user = auth.currentUser ?: return
        val userEmail = user.email ?: ""
        val userId = user.uid

        val tvBookings = findViewById<TextView>(R.id.tvStatBookingsCount)
        val tvCart = findViewById<TextView>(R.id.tvStatCartCount)
        val tvWishlist = findViewById<TextView>(R.id.tvStatWishlistCount)

        // Bookings count
        db.collection("bookings")
            .whereEqualTo("customerEmail", userEmail)
            .get()
            .addOnSuccessListener { docs ->
                tvBookings.text = docs.size().toString()
            }

        // Cart items count
        db.collection("users").document(userId).collection("cart")
            .get()
            .addOnSuccessListener { docs ->
                tvCart.text = docs.size().toString()
            }

        // Wishlist items count
        db.collection("users").document(userId).collection("wishlist")
            .get()
            .addOnSuccessListener { docs ->
                tvWishlist.text = docs.size().toString()
            }
    }

    private fun loadUpcomingAppointments() {
        val user = auth.currentUser ?: return
        val userEmail = user.email ?: ""
        val userId = user.uid
        val container = findViewById<LinearLayout>(R.id.appointmentContainer)

        db.collection("bookings")
            .get()
            .addOnSuccessListener { documents ->
                container.removeAllViews()

                val userBookings = documents.mapNotNull { doc ->
                    val booking = doc.toObject(Booking::class.java)
                    booking.bookingId = doc.id
                    val bEmail = doc.getString("customerEmail") ?: doc.getString("email") ?: ""
                    val bUid = doc.getString("userId") ?: ""

                    if (bEmail.equals(userEmail, ignoreCase = true) || bUid == userId) {
                        booking
                    } else {
                        null
                    }
                }.sortedByDescending { it.bookingDate }

                if (userBookings.isEmpty()) {
                    val noData = TextView(this).apply {
                        text = "No appointments booked yet."
                        setTextColor(Color.parseColor("#B0B0B0"))
                        setPadding(0, 20, 0, 20)
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                    }
                    container.addView(noData)
                    return@addOnSuccessListener
                }

                for (booking in userBookings.take(5)) {
                    val date = booking.bookingDate.ifEmpty { "Pending" }
                    val service = booking.serviceName.ifEmpty { "Piercing Session" }
                    val status = booking.status.ifEmpty { "Pending" }

                    addAppointmentRow(container, date, service, status)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading bookings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addAppointmentRow(container: LinearLayout, date: String, service: String, status: String) {
        val row = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 20, 0, 20)
        }

        val tvDate = TextView(this).apply {
            text = date
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }

        val tvService = TextView(this).apply {
            text = service
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f)
            setTextColor(ContextCompat.getColor(this@ProfileScreen, R.color.moes_gold))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setPadding(8, 0, 8, 0)
        }

        val tvStatus = TextView(this).apply {
            text = status
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            gravity = Gravity.END
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)

            when (status.lowercase()) {
                "confirmed" -> setTextColor(Color.parseColor("#4CAF50"))
                "pending" -> setTextColor(Color.parseColor("#FB8C00"))
                "cancelled" -> setTextColor(Color.parseColor("#E53935"))
                else -> setTextColor(Color.WHITE)
            }
        }

        row.addView(tvDate)
        row.addView(tvService)
        row.addView(tvStatus)
        container.addView(row)

        val line = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(Color.parseColor("#2A2A2A"))
        }
        container.addView(line)
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_profile

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.nav_profile) return@setOnItemSelectedListener true
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
