package com.example.moes // Ensure this matches your actual package name

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProfileScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_screen)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupUserProfile()
        setupBottomNavigation()
        loadUpcomingAppointments()

        findViewById<View>(R.id.btnEditProfile).setOnClickListener {
            // Logic to open Edit Profile activity
            Toast.makeText(this, "Edit Profile Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            findViewById<TextView>(R.id.tvProfileName).text = user.displayName ?: "User Name"
            findViewById<TextView>(R.id.tvProfileEmail).text = user.email ?: "email@example.com"
        }
    }

    private fun loadUpcomingAppointments() {
        val userId = auth.currentUser?.uid ?: return
        val container = findViewById<LinearLayout>(R.id.appointmentContainer)

        // Fetch bookings for the current user, ordered by date
        db.collection("bookings")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                container.removeAllViews() // Clear the "Loading..." state

                if (documents.isEmpty) {
                    val noData = TextView(this).apply {
                        text = "No upcoming appointments."
                        setPadding(0, 20, 0, 20)
                    }
                    container.addView(noData)
                    return@addOnSuccessListener
                }

                for (document in documents) {
                    val date = document.getString("date") ?: ""
                    val service = document.getString("service") ?: ""
                    val status = document.getString("status") ?: "Pending"

                    addAppointmentRow(container, date, service, status)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading bookings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addAppointmentRow(container: LinearLayout, date: String, service: String, status: String) {
        // Create a horizontal row
        val row = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 25, 0, 25)
        }

        // Date Column
        val tvDate = TextView(this).apply {
            text = date
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            // Use setTextSize with TypedValue to explicitly define SP
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
        }

// Piercing/Service Column
        val tvService = TextView(this).apply {
            text = service
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f)
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
            setPadding(10, 0, 10, 0)
        }

// Status Column with pill background
        val tvStatus = TextView(this).apply {
            text = status
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            gravity = Gravity.END
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 11f)
            setPadding(20, 8, 20, 8)

            // Modern way to get color (replaces deprecated method)
            setTextColor(androidx.core.content.ContextCompat.getColor(this@ProfileScreen, android.R.color.black))

            // Apply different backgrounds based on status
            setBackgroundResource(if (status.equals("confirmed", ignoreCase = true))
                R.drawable.bg_status_confirmed else R.drawable.bg_status_pending)
        }

        row.addView(tvDate)
        row.addView(tvService)
        row.addView(tvStatus)
        container.addView(row)

        // Add a thin separator line
        val line = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
            setBackgroundColor(android.graphics.Color.parseColor("#EEEEEE"))
        }
        container.addView(line)
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_profile // Highlight profile icon

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_booking -> {
                    startActivity(Intent(this, BookingScreen::class.java))
                    true
                }
                R.id.nav_profile -> true // Already here
                else -> false
            }
        }
    }
}