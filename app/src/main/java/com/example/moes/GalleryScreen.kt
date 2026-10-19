package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class GalleryScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var cartCard: MaterialCardView
    private lateinit var tvCartSummary: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginScreen::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_gallery_screen)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cartCard = findViewById(R.id.cartCard)
        tvCartSummary = findViewById(R.id.tvCartSummary)

        setupNavigation(R.id.nav_gallery)
        initClickListeners()
        listenToCartUpdates()
        
        findViewById<Button>(R.id.btnViewCart).setOnClickListener {
            startActivity(Intent(this, cart::class.java))
        }
    }

    private fun initClickListeners() {
        // Horizontal Collection 1 (Flash Tattoos)
        setupServiceActions(R.id.btnBookH1_1, R.id.btnCartH1_1, "Flash Art A1", 650.0)
        setupServiceActions(R.id.btnBookH1_2, R.id.btnCartH1_2, "Flash Art A2", 750.0)
        setupServiceActions(R.id.btnBookH1_3, R.id.btnCartH1_3, "Flash Art A3", 850.0)
        setupServiceActions(R.id.btnBookH1_4, R.id.btnCartH1_4, "Flash Art A4", 950.0)
        setupServiceActions(R.id.btnBookH1_5, R.id.btnCartH1_5, "Flash Art A5", 1050.0)

        // Horizontal Collection 2 (Popular Piercings)
        setupServiceActions(R.id.btnBookH2_1, R.id.btnCartH2_1, "Piercing Set B1", 300.0)
        setupServiceActions(R.id.btnBookH2_2, R.id.btnCartH2_2, "Piercing Set B2", 350.0)
        setupServiceActions(R.id.btnBookH2_3, R.id.btnCartH2_3, "Piercing Set B3", 400.0)
        //setupServiceActions(R.id.btnBookH2_4, R.id.btnCartH2_4, "Piercing Set B4", 450.0)
        setupServiceActions(R.id.btnBookH2_5, R.id.btnCartH2_5, "Piercing Set B5", 500.0)

        // Vertical Services
        setupServiceActions(R.id.btnBooking1, R.id.btnAddToCart1, "Small Custom Tattoo", 800.0)
        setupServiceActions(R.id.btnBooking2, R.id.btnAddToCart2, "Ear Piercing", 300.0)
    }

    private fun setupServiceActions(bookId: Int, cartId: Int, name: String, price: Double) {
        findViewById<Button>(bookId)?.setOnClickListener {
            val intent = Intent(this, BookingScreen::class.java)
            intent.putExtra("SELECTED_SERVICE", name)
            startActivity(intent)
        }
        findViewById<Button>(cartId)?.setOnClickListener {
            addToCart(name, price)
        }
    }

    private fun addToCart(name: String, price: Double) {
        val userId = auth.currentUser?.uid ?: return
        val cartItem = hashMapOf(
            "name" to name,
            "price" to price,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(userId).collection("cart")
            .add(cartItem)
            .addOnSuccessListener {
                Toast.makeText(this, "$name added to cart", Toast.LENGTH_SHORT).show()
            }
    }

    private fun listenToCartUpdates() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("cart")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                
                if (snapshots == null || snapshots.isEmpty) {
                    cartCard.visibility = View.GONE
                    return@addSnapshotListener
                }

                val itemNames = snapshots.documents.mapNotNull { it.getString("name") }
                val totalPrice = snapshots.documents.sumOf { it.getDouble("price") ?: 0.0 }
                
                cartCard.visibility = View.VISIBLE
                
                // Show a comma-separated list of selected item names
                val summaryText = if (itemNames.size > 2) {
                    "${itemNames.take(2).joinToString(", ")} + ${itemNames.size - 2} more • Total: R$totalPrice"
                } else {
                    "${itemNames.joinToString(", ")} • Total: R$totalPrice"
                }
                
                tvCartSummary.text = summaryText
            }
    }

    private fun setupNavigation(currentItemId: Int) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav?.selectedItemId = currentItemId
        bottomNav?.setOnItemSelectedListener { item ->
            if (item.itemId == currentItemId) return@setOnItemSelectedListener true
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, MainActivity::class.java))
                R.id.nav_booking -> startActivity(Intent(this, BookingScreen::class.java))
                R.id.nav_gallery -> true
                R.id.nav_profile -> startActivity(Intent(this, AboutUsScreen::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsScreen::class.java))
            }
            finish()
            true
        }
    }
}
