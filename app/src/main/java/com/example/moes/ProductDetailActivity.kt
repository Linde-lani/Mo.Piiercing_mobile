package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * ProductDetailActivity displays detailed item information, specifications,
 * studio guarantees, and options to Add to Cart, Book Appointment, or Wishlist.
 */
class ProductDetailActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var currentProduct: Product? = null
    private var isWishlisted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product_detail)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        @Suppress("DEPRECATION")
        currentProduct = intent.getSerializableExtra("EXTRA_PRODUCT") as? Product

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnWishlist = findViewById<ImageButton>(R.id.btnWishlistToggle)
        val ivImage = findViewById<ImageView>(R.id.ivDetailProductImage)
        val tvCategory = findViewById<TextView>(R.id.tvDetailCategory)
        val tvName = findViewById<TextView>(R.id.tvDetailProductName)
        val tvPrice = findViewById<TextView>(R.id.tvDetailProductPrice)
        val tvDesc = findViewById<TextView>(R.id.tvDetailProductDescription)
        val btnBook = findViewById<MaterialButton>(R.id.btnBookAppointment)
        val btnAddToCart = findViewById<MaterialButton>(R.id.btnAddToCart)

        btnBack.setOnClickListener { finish() }

        currentProduct?.let { prod ->
            tvName.text = prod.name
            tvCategory.text = prod.category.ifEmpty { "Studio Item" }
            tvPrice.text = "R ${"%.2f".format(prod.price)}"
            tvDesc.text = prod.description.ifEmpty { "High quality studio piece designed by Mo Piercings & Co." }

            bindProductImage(this, prod.imageUrl, ivImage)

            checkWishlistState(prod)

            btnWishlist.setOnClickListener {
                toggleWishlist(prod, btnWishlist)
            }

            btnBook.setOnClickListener {
                val intent = Intent(this, BookingScreen::class.java)
                intent.putExtra("SERVICE_NAME", prod.name)
                startActivity(intent)
            }

            btnAddToCart.setOnClickListener {
                addProductToCart(prod)
            }
        } ?: run {
            Toast.makeText(this, "Product details unavailable", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun checkWishlistState(product: Product) {
        val userId = auth.currentUser?.uid ?: return
        if (product.productId.isEmpty()) return

        db.collection("users").document(userId).collection("wishlist").document(product.productId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    isWishlisted = true
                    findViewById<ImageButton>(R.id.btnWishlistToggle).setImageResource(R.drawable.ic_heart_filled)
                }
            }
    }

    private fun toggleWishlist(product: Product, btn: ImageButton) {
        val userId = auth.currentUser?.uid ?: return
        val docId = if (product.productId.isNotEmpty()) product.productId else product.name.replace(" ", "_")
        val ref = db.collection("users").document(userId).collection("wishlist").document(docId)

        if (isWishlisted) {
            ref.delete().addOnSuccessListener {
                isWishlisted = false
                btn.setImageResource(R.drawable.ic_heart_outline)
                Toast.makeText(this, "Removed from Wishlist", Toast.LENGTH_SHORT).show()
            }
        } else {
            ref.set(product).addOnSuccessListener {
                isWishlisted = true
                btn.setImageResource(R.drawable.ic_heart_filled)
                Toast.makeText(this, "Added to Wishlist!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addProductToCart(product: Product) {
        val userId = auth.currentUser?.uid ?: return
        val cartItem = hashMapOf(
            "productId" to product.productId,
            "name" to product.name,
            "price" to product.price,
            "imageUrl" to product.imageUrl,
            "quantity" to 1,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(userId).collection("cart")
            .add(cartItem)
            .addOnSuccessListener {
                Toast.makeText(this, "${product.name} added to Cart!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add to cart: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
