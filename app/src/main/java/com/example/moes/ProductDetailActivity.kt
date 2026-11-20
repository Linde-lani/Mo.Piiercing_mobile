package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.moes.databinding.ActivityProductDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity displaying detailed product information, booking & cart integration.
 */
class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var product: Product? = null
    private var isWishlisted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        product = intent.getSerializableExtra("EXTRA_PRODUCT") as? Product

        if (product == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindProductData()
        checkWishlistStatus()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnWishlistToggle.setOnClickListener { toggleWishlist() }
        binding.btnBookAppointment.setOnClickListener { bookAppointment() }
        binding.btnAddToCart.setOnClickListener { addToCart() }
    }

    private fun bindProductData() {
        val prod = product ?: return

        binding.tvDetailProductName.text = prod.name
        binding.tvDetailCategory.text = prod.category.uppercase()
        binding.tvDetailProductPrice.text = "R${String.format("%.2f", prod.price)}"
        binding.tvDetailProductDescription.text = prod.description.ifEmpty { "No description available." }

        Glide.with(this)
            .load(prod.imageUrl)
            .placeholder(R.drawable.jewelry_1)
            .error(R.drawable.jewelry_1)
            .into(binding.ivDetailProductImage)
    }

    private fun checkWishlistStatus() {
        val userId = auth.currentUser?.uid ?: return
        val prodId = product?.productId ?: return

        db.collection("users").document(userId).collection("wishlist")
            .document(prodId)
            .get()
            .addOnSuccessListener { doc ->
                isWishlisted = doc.exists()
                updateWishlistIcon()
            }
    }

    private fun toggleWishlist() {
        val userId = auth.currentUser?.uid ?: return
        val prod = product ?: return

        val ref = db.collection("users").document(userId).collection("wishlist").document(prod.productId)

        if (isWishlisted) {
            ref.delete().addOnSuccessListener {
                isWishlisted = false
                updateWishlistIcon()
                Toast.makeText(this, "Removed from Wishlist", Toast.LENGTH_SHORT).show()
            }
        } else {
            ref.set(prod).addOnSuccessListener {
                isWishlisted = true
                updateWishlistIcon()
                Toast.makeText(this, "Added to Wishlist!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateWishlistIcon() {
        if (isWishlisted) {
            binding.btnWishlistToggle.setImageResource(R.drawable.ic_heart_filled)
        } else {
            binding.btnWishlistToggle.setImageResource(R.drawable.ic_heart_outline)
        }
    }

    private fun bookAppointment() {
        val prod = product ?: return
        val intent = Intent(this, BookingScreen::class.java)
        intent.putExtra("SELECTED_SERVICE", prod.name)
        startActivity(intent)
    }

    private fun addToCart() {
        val userId = auth.currentUser?.uid ?: return
        val prod = product ?: return

        val cartItem = hashMapOf(
            "name" to prod.name,
            "price" to prod.price,
            "imageUrl" to prod.imageUrl,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(userId).collection("cart")
            .add(cartItem)
            .addOnSuccessListener {
                Toast.makeText(this, "${prod.name} added to Cart!", Toast.LENGTH_SHORT).show()
            }
    }
}
