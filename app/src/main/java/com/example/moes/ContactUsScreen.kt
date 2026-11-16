package com.example.moes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moes.databinding.ActivityContactUsScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Contact Us Activity for Mo Piercings.
 * Provides customer support contact information, website link, social links,
 * and direct message submission saved to Firestore 'messages' collection.
 */
class ContactUsScreen : AppCompatActivity() {

    private lateinit var binding: ActivityContactUsScreenBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

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
        binding = ActivityContactUsScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        populateUserData(currentUser.uid, currentUser.email ?: "")
        setupClickListeners()
        setupNavigation(R.id.nav_profile)
    }

    private fun populateUserData(userId: String, email: String) {
        binding.etContactEmail.setText(email)

        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("username") ?: doc.getString("fullName") ?: ""
                    val phone = doc.getString("phoneNumber") ?: doc.getString("phone") ?: ""
                    if (name.isNotEmpty()) binding.etContactName.setText(name)
                    if (phone.isNotEmpty()) binding.etContactPhone.setText(phone)
                }
            }
    }

    private fun setupClickListeners() {
        binding.btnOpenWebsite.setOnClickListener {
            openWebUrl("https://mopiercings.co.za")
        }

        binding.btnFacebook.setOnClickListener {
            openWebUrl("https://facebook.com/mopiercings")
        }

        binding.btnInstagram.setOnClickListener {
            openWebUrl("https://instagram.com/mopiercings")
        }

        binding.btnTiktok.setOnClickListener {
            openWebUrl("https://tiktok.com/@mopiercings")
        }

        binding.btnSubmitContact.setOnClickListener {
            submitMessageToFirestore()
        }
    }

    private fun openWebUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open link: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitMessageToFirestore() {
        val name = binding.etContactName.text.toString().trim()
        val email = binding.etContactEmail.text.toString().trim()
        val phone = binding.etContactPhone.text.toString().trim()
        val subject = binding.etContactSubject.text.toString().trim()
        val message = binding.etContactMessage.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || subject.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: ""
        val messageMap = hashMapOf(
            "userId" to userId,
            "customerName" to name,
            "email" to email,
            "phone" to phone,
            "subject" to subject,
            "message" to message,
            "status" to "NEW",
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("messages").add(messageMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Message submitted successfully!", Toast.LENGTH_LONG).show()
                binding.etContactSubject.setText("")
                binding.etContactMessage.setText("")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
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
