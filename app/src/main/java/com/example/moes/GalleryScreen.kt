package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moes.databinding.ActivityGalleryScreenBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Customer Gallery Activity displaying dynamic Firebase Firestore products organized in horizontal sections,
 * Category Chips, Search, Wishlist integration, and real-time Cart state.
 */
class GalleryScreen : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryScreenBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var featuredTattoosAdapter: TattooAdapter
    private lateinit var featuredJewelleryAdapter: JewelleryAdapter
    private lateinit var newArrivalsAdapter: GalleryAdapter
    private lateinit var bestSellersAdapter: GalleryAdapter
    private lateinit var recommendedAdapter: GalleryAdapter

    private val allProductsList = mutableListOf<Product>()
    private var currentSearchQuery: String = ""
    private var selectedCategory: String = "All"

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
        binding = ActivityGalleryScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerViews()
        setupSearchAndChips()
        setupNavigation(R.id.nav_gallery)

        binding.btnWishlistHeader.setOnClickListener {
            startActivity(Intent(this, WishlistActivity::class.java))
        }

        binding.btnViewCart.setOnClickListener {
            startActivity(Intent(this, cart::class.java))
        }

        setupSwipeToRefresh()
        listenToCartUpdates()
        listenToFirestoreProducts()
    }

    private fun setupRecyclerViews() {
        // 1. Featured Tattoos
        featuredTattoosAdapter = TattooAdapter(
            isAdmin = false,
            onProductClick = { openProductDetail(it) },
            onBookClick = { bookProduct(it) },
            onAddToCartClick = { addToCart(it) },
            onWishlistClick = { toggleWishlist(it) }
        )
        binding.rvFeaturedTattoos.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFeaturedTattoos.adapter = featuredTattoosAdapter

        // 2. Featured Jewellery
        featuredJewelleryAdapter = JewelleryAdapter(
            isAdmin = false,
            onProductClick = { openProductDetail(it) },
            onBookClick = { bookProduct(it) },
            onAddToCartClick = { addToCart(it) },
            onWishlistClick = { toggleWishlist(it) }
        )
        binding.rvFeaturedJewellery.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFeaturedJewellery.adapter = featuredJewelleryAdapter

        // 3. New Arrivals
        newArrivalsAdapter = GalleryAdapter(
            isAdmin = false,
            onProductClick = { openProductDetail(it) },
            onBookClick = { bookProduct(it) },
            onAddToCartClick = { addToCart(it) },
            onWishlistClick = { toggleWishlist(it) }
        )
        binding.rvNewArrivals.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvNewArrivals.adapter = newArrivalsAdapter

        // 4. Best Sellers
        bestSellersAdapter = GalleryAdapter(
            isAdmin = false,
            onProductClick = { openProductDetail(it) },
            onBookClick = { bookProduct(it) },
            onAddToCartClick = { addToCart(it) },
            onWishlistClick = { toggleWishlist(it) }
        )
        binding.rvBestSellers.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBestSellers.adapter = bestSellersAdapter

        // 5. Recommended For You
        recommendedAdapter = GalleryAdapter(
            isAdmin = false,
            onProductClick = { openProductDetail(it) },
            onBookClick = { bookProduct(it) },
            onAddToCartClick = { addToCart(it) },
            onWishlistClick = { toggleWishlist(it) }
        )
        binding.rvRecommended.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecommended.adapter = recommendedAdapter
    }

    private fun setupSearchAndChips() {
        binding.etSearchCustomer.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                filterAndDistributeProducts()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                selectedCategory = chip?.text?.toString() ?: "All"
                filterAndDistributeProducts()
            }
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(this, R.color.moes_gold),
            ContextCompat.getColor(this, R.color.moes_forest_green)
        )
        binding.swipeRefreshLayout.setOnRefreshListener {
            listenToFirestoreProducts()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    /**
     * Listens to Firestore `products` collection for real-time updates.
     */
    private fun listenToFirestoreProducts() {
        db.collection("products")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading gallery products", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val items = snapshots?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.apply { productId = doc.id }
                } ?: emptyList()

                allProductsList.clear()
                allProductsList.addAll(items)

                filterAndDistributeProducts()
            }
    }

    /**
     * Filters products by category & search term, then distributes to the 5 horizontal sections.
     */
    private fun filterAndDistributeProducts() {
        val filtered = allProductsList.filter { prod ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Tattoos" -> prod.category.contains("Tattoo", ignoreCase = true)
                "Jewellery" -> !prod.category.contains("Tattoo", ignoreCase = true)
                "Featured" -> prod.featured
                else -> prod.category.equals(selectedCategory, ignoreCase = true)
            }

            val matchesSearch = if (currentSearchQuery.isEmpty()) true else {
                prod.name.contains(currentSearchQuery, ignoreCase = true) ||
                        prod.description.contains(currentSearchQuery, ignoreCase = true) ||
                        prod.category.contains(currentSearchQuery, ignoreCase = true)
            }

            matchesCategory && matchesSearch
        }

        val featuredTattoos = filtered.filter { it.category.contains("Tattoo", ignoreCase = true) && it.featured }
        val featuredJewellery = filtered.filter { !it.category.contains("Tattoo", ignoreCase = true) && it.featured }
        val newArrivals = filtered.filter { it.isNew || (System.currentTimeMillis() - it.createdAt) < (7 * 24 * 60 * 60 * 1000L) }
        val bestSellers = filtered.filter { it.bestSeller }
        val recommended = filtered.take(10)

        featuredTattoosAdapter.submitList(if (featuredTattoos.isNotEmpty()) featuredTattoos else filtered.filter { it.category.contains("Tattoo", ignoreCase = true) })
        featuredJewelleryAdapter.submitList(if (featuredJewellery.isNotEmpty()) featuredJewellery else filtered.filter { !it.category.contains("Tattoo", ignoreCase = true) })
        newArrivalsAdapter.submitList(if (newArrivals.isNotEmpty()) newArrivals else filtered)
        bestSellersAdapter.submitList(if (bestSellers.isNotEmpty()) bestSellers else filtered.reversed())
        recommendedAdapter.submitList(recommended)
    }

    private fun openProductDetail(product: Product) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("EXTRA_PRODUCT", product)
        startActivity(intent)
    }

    private fun bookProduct(product: Product) {
        val intent = Intent(this, BookingScreen::class.java)
        intent.putExtra("SELECTED_SERVICE", product.name)
        startActivity(intent)
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

    private fun toggleWishlist(product: Product) {
        val userId = auth.currentUser?.uid ?: return
        val ref = db.collection("users").document(userId).collection("wishlist").document(product.productId)

        ref.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                ref.delete().addOnSuccessListener {
                    Toast.makeText(this, "Removed from Wishlist", Toast.LENGTH_SHORT).show()
                }
            } else {
                ref.set(product).addOnSuccessListener {
                    Toast.makeText(this, "Added to Wishlist!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun listenToCartUpdates() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("cart")
            .addSnapshotListener { snapshots, _ ->
                val count = snapshots?.size() ?: 0
                val totalPrice = snapshots?.documents?.sumOf { doc ->
                    doc.getDouble("price") ?: 0.0
                } ?: 0.0

                binding.tvCartSummary.text = "Cart: $count items | Total: R${String.format("%.2f", totalPrice)}"
            }
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
                R.id.nav_gallery -> true
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
