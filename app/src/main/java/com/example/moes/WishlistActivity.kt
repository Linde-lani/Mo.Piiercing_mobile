package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * WishlistActivity displays customer's saved items retrieved from `users/{userId}/wishlist`.
 */
class WishlistActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var adapter: GalleryAdapter
    private lateinit var rvWishlist: RecyclerView
    private lateinit var layoutEmpty: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_wishlist)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginScreen::class.java))
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        rvWishlist = findViewById(R.id.rvWishlist)
        layoutEmpty = findViewById(R.id.layoutEmptyWishlist)

        adapter = GalleryAdapter(
            isAdmin = false,
            onProductClick = { product ->
                val intent = Intent(this, ProductDetailActivity::class.java)
                intent.putExtra("EXTRA_PRODUCT", product)
                startActivity(intent)
            }
        )

        rvWishlist.layoutManager = GridLayoutManager(this, 2)
        rvWishlist.adapter = adapter

        loadWishlistData()
    }

    private fun loadWishlistData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("wishlist")
            .get()
            .addOnSuccessListener { documents ->
                val items = documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java).apply {
                        if (productId.isEmpty()) productId = doc.id
                    }
                }

                adapter.submitList(items)

                if (items.isEmpty()) {
                    layoutEmpty.visibility = View.VISIBLE
                    rvWishlist.visibility = View.GONE
                } else {
                    layoutEmpty.visibility = View.GONE
                    rvWishlist.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load wishlist: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
