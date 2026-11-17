package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moes.databinding.ActivityProductDetailBinding
import com.example.moes.databinding.ItemProductReviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Activity displaying detailed product information, customer reviews, booking & cart integration.
 */
class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var product: Product? = null
    private var isWishlisted: Boolean = false
    private val reviewsList = mutableListOf<ProductReview>()
    private lateinit var reviewAdapter: ReviewAdapter

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
        setupReviewsList()
        checkWishlistStatus()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnWishlistToggle.setOnClickListener { toggleWishlist() }
        binding.btnBookAppointment.setOnClickListener { bookAppointment() }
        binding.btnAddToCart.setOnClickListener { addToCart() }
        binding.btnSubmitReview.setOnClickListener { submitReview() }
    }

    private fun bindProductData() {
        val prod = product ?: return

        binding.tvDetailName.text = prod.name
        binding.tvDetailCategory.text = prod.category.uppercase()
        binding.tvDetailPrice.text = "R${String.format("%.2f", prod.price)}"
        binding.tvDetailDescription.text = prod.description.ifEmpty { "No description available." }
        binding.tvDetailStock.text = if (prod.stock > 0) "In Stock (${prod.stock} items available)" else "Out of Stock"

        Glide.with(this)
            .load(prod.imageUrl)
            .placeholder(R.drawable.septum)
            .error(R.drawable.septum)
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

    private fun setupReviewsList() {
        reviewAdapter = ReviewAdapter(reviewsList)
        binding.rvReviews.layoutManager = LinearLayoutManager(this)
        binding.rvReviews.adapter = reviewAdapter

        val prodId = product?.productId ?: return

        db.collection("products").document(prodId).collection("reviews")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, _ ->
                val reviews = snapshots?.documents?.mapNotNull { doc ->
                    doc.toObject(ProductReview::class.java)?.apply { reviewId = doc.id }
                } ?: emptyList()

                reviewsList.clear()
                reviewsList.addAll(reviews)
                reviewAdapter.notifyDataSetChanged()

                binding.tvReviewCount.text = "(${reviewsList.size} reviews)"
            }
    }

    private fun submitReview() {
        val comment = binding.etReviewComment.text.toString().trim()
        if (comment.isEmpty()) {
            Toast.makeText(this, "Please write a review comment", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: "anonymous"
        val userName = auth.currentUser?.displayName ?: "Customer"
        val prodId = product?.productId ?: return

        val review = ProductReview(
            userId = userId,
            userName = userName,
            rating = 5.0f,
            comment = comment,
            timestamp = System.currentTimeMillis()
        )

        db.collection("products").document(prodId).collection("reviews")
            .add(review)
            .addOnSuccessListener {
                binding.etReviewComment.text?.clear()
                Toast.makeText(this, "Review submitted! Thank you.", Toast.LENGTH_SHORT).show()
            }
    }

    class ReviewAdapter(private val items: List<ProductReview>) :
        RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemProductReviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.tvReviewerName.text = item.userName.ifEmpty { "Verified Customer" }
            holder.binding.tvReviewRating.text = "${item.rating} ★"
            holder.binding.tvReviewComment.text = item.comment
        }

        override fun getItemCount() = items.size

        class ViewHolder(val binding: ItemProductReviewBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
