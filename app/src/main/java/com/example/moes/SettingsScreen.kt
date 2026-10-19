package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class SettingsScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var tvUserEmailSettings: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnAdminPanel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        tvUserEmailSettings = findViewById(R.id.tvUserEmailSettings)
        btnLogout = findViewById(R.id.btnLogout)
        btnAdminPanel = findViewById(R.id.btnAdminPanel)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            tvUserEmailSettings.text = "Logged in as: ${currentUser.email}"
            
            // Basic role check: show admin button only for a specific email or role
            // For now, we show it to everyone for development, or you can restrict it:
            // btnAdminPanel.visibility = if (currentUser.email == "admin@moes.com") View.VISIBLE else View.GONE
        }

        btnAdminPanel.setOnClickListener {
            startActivity(Intent(this, AdminScreen::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setupNavigation(R.id.nav_settings)
    }

    private fun setupNavigation(currentItemId: Int) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav?.selectedItemId = currentItemId
        bottomNav?.setOnItemSelectedListener { item ->
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
                R.id.nav_about -> {
                    startActivity(Intent(this, AboutUsScreen::class.java))
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
