package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import java.text.SimpleDateFormat
import java.util.*

class BookingScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // UI References
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var etNotes: TextInputEditText
    private lateinit var spinnerService: AutoCompleteTextView
    private lateinit var chipGroupTime: ChipGroup
    private lateinit var cbAgree: CheckBox
    private lateinit var btnConfirm: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Auth Check
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginScreen::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_booking_screen)

        setupWindowInsets()
        initViews()
        populateUserData()
        setupServiceDropdown()
        setupDatePicker()
        setupNavigation(R.id.nav_booking)

        btnConfirm.setOnClickListener {
            handleBooking()
        }
    }

    private fun initViews() {
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etDate = findViewById(R.id.etDate)
        etNotes = findViewById(R.id.etNotes)
        spinnerService = findViewById(R.id.spinnerService)
        chipGroupTime = findViewById(R.id.chipGroupTime)
        cbAgree = findViewById(R.id.cbAgree)
        btnConfirm = findViewById(R.id.btnConfirmBooking)

    }

    /**
     * Corresponds to DOMContentLoaded: populate saved user data
     */
    private fun populateUserData() {
        val user = auth.currentUser
        etName.setText(user?.displayName)
        etEmail.setText(user?.email)

        // Fetch phone from Firestore if it's not in Auth
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    etPhone.setText(doc.getString("phone"))
                }
            }
        }
    }

    private fun setupServiceDropdown() {
        val services = arrayOf(
            "Ear Piercing - R300", "Nose Piercing - R450",
            "Small Tattoo - R800", "Medium Tattoo - R1500",
            "Single Tooth Gem - R250"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, services)
        spinnerService.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        etDate.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()
            val picker = builder.build()
            picker.show(supportFragmentManager, "DATE_PICKER")
            picker.addOnPositiveButtonClickListener { selection ->
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = selection
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = format.format(calendar.time)
                etDate.setText(formattedDate)
                checkBookedSlots(formattedDate)
            }
        }
    }

    private fun checkBookedSlots(date: String) {
        // Reset all chips to enabled and full opacity
        for (i in 0 until chipGroupTime.childCount) {
            val chip = chipGroupTime.getChildAt(i) as? Chip
            chip?.isEnabled = true
            chip?.alpha = 1.0f
        }

        // Query Firestore for bookings on this date
        db.collection("bookingSlots")
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { documents ->
                val bookedTimes = mutableSetOf<String>()
                for (doc in documents) {
                    val time = doc.getString("time")
                    if (time != null) {
                        bookedTimes.add(time)
                    }
                }

                // Disable chips that match booked times
                for (i in 0 until chipGroupTime.childCount) {
                    val chip = chipGroupTime.getChildAt(i) as? Chip
                    if (chip != null && bookedTimes.contains(chip.text.toString())) {
                        chip.isEnabled = false
                        chip.alpha = 0.4f
                        if (chip.isChecked) {
                            chip.isChecked = false
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error checking availability: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Replaces JS handleBooking(event)
     */
    private fun handleBooking() {
        val selectedChipId = chipGroupTime.checkedChipId

        if (etDate.text.isNullOrBlank()) {
            Toast.makeText(this, "Please select a preferred date first", Toast.LENGTH_SHORT).show()
            return
        }
        // Validation logic
        if (etPhone.text.isNullOrBlank()) {
            etPhone.error = "Phone number required"
            return
        }
        if (selectedChipId == -1) {
            Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show()
            return
        }
        if (!cbAgree.isChecked) {
            Toast.makeText(this, "Please agree to aftercare instructions", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedTime = findViewById<Chip>(selectedChipId).text.toString()
        saveBookingToFirestore(selectedTime)
    }

    private fun saveBookingToFirestore(time: String) {
        val userId = auth.currentUser?.uid ?: return
        val userName = etName.text.toString()
        val date = etDate.text.toString()

        // Double check availability before saving to prevent race conditions
        db.collection("bookingSlots")
            .whereEqualTo("date", date)
            .whereEqualTo("time", time)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    Toast.makeText(this, "Sorry, this time slot was just booked by someone else. Please choose another.", Toast.LENGTH_LONG).show()
                    checkBookedSlots(date)
                    return@addOnSuccessListener
                }

                val booking = hashMapOf(
                    "userId" to userId,
                    "name" to userName,
                    "email" to etEmail.text.toString(),
                    "phone" to etPhone.text.toString(),
                    "service" to spinnerService.text.toString(),
                    "date" to date,
                    "time" to time,
                    "notes" to etNotes.text.toString(),
                    "status" to "Pending",
                    "timestamp" to com.google.firebase.Timestamp.now()
                )

                db.collection("bookings").add(booking)
                    .addOnSuccessListener {
                        val intent = Intent(this, BookingConfirmationActivity::class.java)
                        intent.putExtra("USER_NAME", userName)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupNavigation(currentItemId: Int) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav?.selectedItemId = currentItemId
        bottomNav?.setOnItemSelectedListener { item ->
            if (item.itemId == currentItemId) return@setOnItemSelectedListener true
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, MainActivity::class.java))
                R.id.nav_booking -> return@setOnItemSelectedListener true
                R.id.nav_gallery -> startActivity(Intent(this, GalleryScreen::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileScreen::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsScreen::class.java))
            }
            finish()
            true
        }
    }
}