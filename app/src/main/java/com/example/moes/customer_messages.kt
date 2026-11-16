package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moes.databinding.ActivityCustomerMessagesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity for customers to view their submitted contact enquiries and response statuses.
 */
class customer_messages : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerMessagesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: CustomerMessagesAdapter

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
        binding = ActivityCustomerMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupClickListeners()
        setupNavigation(R.id.nav_profile)

        loadUserMessages(currentUser.email ?: "")
    }

    private fun setupRecyclerView() {
        adapter = CustomerMessagesAdapter { message ->
            Toast.makeText(this, "Subject: ${message.subject}\nStatus: ${message.status.name}", Toast.LENGTH_LONG).show()
        }
        binding.rvCustomerMessages.layoutManager = LinearLayoutManager(this)
        binding.rvCustomerMessages.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnNewEnquiry.setOnClickListener {
            startActivity(Intent(this, ContactUsScreen::class.java))
        }
    }

    private fun loadUserMessages(userEmail: String) {
        db.collection("messages")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                val list = mutableListOf<ContactMessage>()
                for (doc in documents) {
                    val subject = doc.getString("subject") ?: "Enquiry"
                    val msgText = doc.getString("message") ?: ""
                    val name = doc.getString("customerName") ?: userEmail
                    val statusStr = doc.getString("status") ?: "NEW"
                    val status = try { MessageStatus.valueOf(statusStr.uppercase()) } catch (e: Exception) { MessageStatus.NEW }

                    list.add(
                        ContactMessage(
                            id = doc.id,
                            customerName = name,
                            email = userEmail,
                            subject = subject,
                            message = msgText,
                            status = status
                        )
                    )
                }

                adapter.submitList(list)
                if (list.isEmpty()) {
                    binding.layoutEmptyMessages.visibility = View.VISIBLE
                    binding.rvCustomerMessages.visibility = View.GONE
                } else {
                    binding.layoutEmptyMessages.visibility = View.GONE
                    binding.rvCustomerMessages.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                binding.layoutEmptyMessages.visibility = View.VISIBLE
                binding.rvCustomerMessages.visibility = View.GONE
            }
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
