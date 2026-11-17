package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.moes.databinding.ActivityWishlistBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity displaying customer's saved Wishlist / Favourite items from Firebase Firestore.
 */
class WishlistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWishlistBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: GalleryAdapter

    private val wishlistProducts = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadWishlistFromFirebase()
    }

    private fun setupRecyclerView() {
        adapter = GalleryAdapter(
            isAdmin = false,
            onProductClick = { product -> openProductDetail(product) },
            onBookClick = { product -> bookProduct(product) },
            onAddToCartClick = { product -> addToCart(product) },
            onWishlistClick = { product -> removeFromWishlist(product) }
        )

        binding.rvWishlist.layoutManager = GridLayoutManager(this, 2)
        binding.rvWishlist.adapter = adapter
    }

    private fun loadWishlistFromFirebase() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("wishlist")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading wishlist", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val items = snapshots?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.apply { productId = doc.id }
                } ?: emptyList()

                wishlistProducts.clear()
                wishlistProducts.addAll(items)
                adapter.submitList(wishlistProducts)

                binding.tvWishlistCount.text = "${wishlistProducts.size} saved items"

                if (wishlistProducts.isEmpty()) {
                    binding.rvWishlist.visibility = View.GONE
                    binding.tvEmptyWishlist.visibility = View.VISIBLE
                } else {
                    binding.rvWishlist.visibility = View.VISIBLE
                    binding.tvEmptyWishlist.visibility = View.GONE
                }
            }
    }

    private fun removeFromWishlist(product: Product) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("wishlist")
            .document(product.productId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "${product.name} removed from Wishlist", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addToCart(product: Product) {
        val userId = auth.currentUser?.uid ?: return
        val cartItem = hashMapOf(
            "name" to product.name,
            "price" to product.price,
            "imageUrl" to product.imageUrl,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(userId).collection("cart")
            .add(cartItem)
            .addOnSuccessListener {
                Toast.makeText(this, "${product.name} added to Cart!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun bookProduct(product: Product) {
        val intent = Intent(this, BookingScreen::class.java)
        intent.putExtra("SELECTED_SERVICE", product.name)
        startActivity(intent)
    }

    private fun openProductDetail(product: Product) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("EXTRA_PRODUCT", product)
        startActivity(intent)
    }
}
