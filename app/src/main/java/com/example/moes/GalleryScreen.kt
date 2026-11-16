package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

/**
 * Customer Gallery Activity displaying dynamic Firebase Firestore products organized in horizontal sections,
 * Category Chips, Search, Wishlist integration, and real-time Cart state.
 * Includes all uploaded studio drawable images categorized by image name.
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
     * Fallbacks to local studio images if Firestore returns empty.
     */
    private fun listenToFirestoreProducts() {
        db.collection("products")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshots, _ ->
                val items = snapshots?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.apply { productId = doc.id }
                } ?: emptyList()

                allProductsList.clear()
                if (items.isNotEmpty()) {
                    allProductsList.addAll(items)
                } else {
                    allProductsList.addAll(getFallbackProducts())
                }

                filterAndDistributeProducts()
            }
    }

    private fun getFallbackProducts(): List<Product> {
        return listOf(
            // --- TATTOOS ---
            Product("tat_1", "Fine-Line Rose Tattoo", "Delicate fine-line gothic rose tattoo piece", 650.0, "tatto_1", "Tattoos", 8, featured = true, active = true, isNew = false, bestSeller = true),
            Product("tat_2", "Floral Symbol Tattoo", "Minimalist floral symbol custom design", 550.0, "tatto_2", "Tattoos", 10, featured = false, active = true, isNew = true, bestSeller = false),
            Product("tat_3", "Minimalist Script Tattoo", "Subtle fine-line lettering tattoo", 450.0, "tatto_3", "Tattoos", 12, featured = false, active = true, isNew = true, bestSeller = false),
            Product("tat_4", "Gothic Blackwork Tattoo", "Dark gothic blackwork shading art", 750.0, "tatto_4", "Tattoos", 6, featured = true, active = true, isNew = true, bestSeller = false),
            Product("tat_5", "Japanese Dragon Forearm Sleeve", "Traditional Japanese blackwork dragon tattoo design", 1200.0, "tatto_5", "Tattoos", 5, featured = true, active = true, isNew = true, bestSeller = true),
            Product("tat_6", "Custom Skull & Snake Sleeve", "Detailed dark skull & viper arm piece", 1500.0, "tattoo_6", "Tattoos", 4, featured = true, active = true, isNew = false, bestSeller = true),

            // --- JEWELLERY ---
            Product("jwl_1", "Titanium Body Jewellery Studs", "ASTM F-136 implant-grade titanium studs", 320.0, "jewelry_1", "Jewellery", 15, featured = true, active = true, isNew = true, bestSeller = true),
            Product("jwl_2", "Gold Anodized Piercing Ring Set", "Gold anodized seamless clickers & rings", 450.0, "jewelry_2", "Jewellery", 10, featured = true, active = true, isNew = false, bestSeller = true),
            Product("jwl_3", "Opal & Crystal Labret Stack", "Threadless opal & cubic zirconia labret studs", 520.0, "jewelry_3", "Jewellery", 8, featured = true, active = true, isNew = true, bestSeller = false),

            // --- NOSE PIERCINGS ---
            Product("nos_1", "Seamless Gold Nostril Hoop", "18g seamless gold anodized titanium nose hoop", 280.0, "nose_1", "Nose Piercings", 20, featured = true, active = true, isNew = false, bestSeller = true),
            Product("nos_2", "Gold Nostril Stud & Hoop Stack", "Dual nostril piercing gold stud and clicker combination", 350.0, "nose_2", "Nose Piercings", 12, featured = true, active = true, isNew = true, bestSeller = true),
            Product("nos_3", "Nose Hoop & Ear Cartilage Setup", "Matching nose hoop & ear cartilage styling", 420.0, "nose_ear_1", "Nose Piercings", 7, featured = false, active = true, isNew = true, bestSeller = false),
            Product("nos_4", "Nostril & Lip Piercing Combo", "Stylized nostril hoop & lip labret combination", 480.0, "nose_lips_1", "Nose Piercings", 6, featured = true, active = true, isNew = true, bestSeller = false),
            Product("nos_5", "Double Nostril & Snake Bites Setup", "Curated facial stack with dual nostril & lip rings", 550.0, "nose_lips_2", "Nose Piercings", 5, featured = false, active = true, isNew = false, bestSeller = true),
            Product("nos_6", "Septum Clicker & Lip Stud Combo", "Titanium septum clicker with lip labret stud", 520.0, "nose_lips_3", "Nose Piercings", 8, featured = true, active = true, isNew = true, bestSeller = true),

            // --- EAR PIERCINGS ---
            Product("ear_1", "Gold Opal Helix Stud", "14k solid gold labret with genuine Australian opal stud", 380.0, "ear_1", "Ear Piercings", 9, featured = true, active = true, isNew = true, bestSeller = false),
            Product("ear_2", "Ear Cartilage Scaffolding Piercing", "Titanium ear cartilage double piercing setup", 450.0, "ear_2", "Ear Piercings", 11, featured = true, active = true, isNew = false, bestSeller = true),
            Product("ear_3", "Curated Lobe Triple Stack", "Three-tier lobe piercing stack with titanium studs", 350.0, "ear_3", "Ear Piercings", 14, featured = false, active = true, isNew = true, bestSeller = false),
            Product("ear_4", "Sparkle Motion Ear Cluster", "Cartilage cluster piercing with cubic zirconia gems", 420.0, "sparkle_motion_ear_4", "Ear Piercings", 10, featured = true, active = true, isNew = true, bestSeller = true),
            Product("ear_5", "Dermal Anchor Neck Piercing", "Single point dermal anchor neck placement", 490.0, "neck_1", "Ear Piercings", 5, featured = false, active = true, isNew = true, bestSeller = false),

            // --- BELLY PIERCINGS ---
            Product("bly_1", "Teardrop Crystal Navel Barbell", "14g curved titanium navel barbell with teardrop gem", 420.0, "belly_1", "Belly Piercings", 12, featured = true, active = true, isNew = false, bestSeller = true),
            Product("bly_2", "Gold Dangle Butterfly Belly Ring", "Gold anodized navel ring with dangling butterfly crystal", 480.0, "belly_2", "Belly Piercings", 9, featured = true, active = true, isNew = true, bestSeller = false),

            // --- TONGUE PIERCINGS ---
            Product("tng_1", "Titanium Tongue Barbell", "Standard 14g straight titanium tongue barbell", 350.0, "tongue_1", "Tongue Piercings", 15, featured = false, active = true, isNew = true, bestSeller = false),

            // --- TOOTH GEMS ---
            Product("tgm_1", "Single Swarovski Tooth Gem", "Professional lead-free Swarovski tooth gem bonding", 250.0, "tooth_gem_1", "Tooth Gems", 25, featured = true, active = true, isNew = true, bestSeller = true),
            Product("tgm_2", "Butterfly Crystal Tooth Gem", "Custom butterfly crystal tooth gem design", 350.0, "tooth_gem_2", "Tooth Gems", 18, featured = true, active = true, isNew = true, bestSeller = false),
            Product("tgm_3", "Starfall Diamond Tooth Gem Cluster", "Multi-crystal tooth gem starfall cluster", 450.0, "tooth_gem_3", "Tooth Gems", 10, featured = false, active = true, isNew = true, bestSeller = true),
            Product("tgm_4", "Gold Heart Tooth Gem Accent", "18k gold heart tooth gem accent piece", 300.0, "tooth_gem_4", "Tooth Gems", 14, featured = false, active = true, isNew = false, bestSeller = false),
            Product("tgm_5", "Double Crystal Tooth Gem Stack", "Dual tooth gem bonding on upper lateral incisors", 380.0, "tooth_gem_5", "Tooth Gems", 12, featured = true, active = true, isNew = true, bestSeller = false),
            Product("tgm_6", "Full Smile Swarovski Tooth Gem Array", "Full smile crystal bonding array", 600.0, "tooth_gem_6", "Tooth Gems", 7, featured = true, active = true, isNew = true, bestSeller = true)
        )
    }

    /**
     * Filters products by category & search term, then distributes to the 5 horizontal sections.
     */
    private fun filterAndDistributeProducts() {
        val filtered = allProductsList.filter { prod ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Tattoos" -> prod.category.contains("Tattoo", ignoreCase = true) || prod.imageUrl.contains("tatto", ignoreCase = true)
                "Jewellery" -> prod.category.equals("Jewellery", ignoreCase = true) || prod.imageUrl.contains("jewelry", ignoreCase = true)
                "Ear Piercings", "Ear" -> prod.category.contains("Ear", ignoreCase = true) || prod.imageUrl.contains("ear", ignoreCase = true) || prod.imageUrl.contains("neck", ignoreCase = true)
                "Nose Piercings", "Nose" -> prod.category.contains("Nose", ignoreCase = true) || prod.imageUrl.contains("nose", ignoreCase = true)
                "Belly Piercings", "Belly" -> prod.category.contains("Belly", ignoreCase = true) || prod.imageUrl.contains("belly", ignoreCase = true)
                "Tooth Gems" -> prod.category.contains("Tooth", ignoreCase = true) || prod.imageUrl.contains("tooth_gem", ignoreCase = true)
                "Industrial Piercings", "Industrial" -> false // Explicitly empty per user instructions
                "Featured" -> prod.featured
                else -> prod.category.contains(selectedCategory, ignoreCase = true) || prod.imageUrl.contains(selectedCategory, ignoreCase = true)
            }

            val matchesSearch = if (currentSearchQuery.isEmpty()) true else {
                prod.name.contains(currentSearchQuery, ignoreCase = true) ||
                        prod.description.contains(currentSearchQuery, ignoreCase = true) ||
                        prod.category.contains(currentSearchQuery, ignoreCase = true) ||
                        prod.imageUrl.contains(currentSearchQuery, ignoreCase = true)
            }

            matchesCategory && matchesSearch
        }

        val featuredTattoos = filtered.filter { (it.category.contains("Tattoo", ignoreCase = true) || it.imageUrl.contains("tatto", ignoreCase = true)) && it.featured }
        val featuredJewellery = filtered.filter { (it.category.equals("Jewellery", ignoreCase = true) || it.imageUrl.contains("jewelry", ignoreCase = true)) && it.featured }
        val newArrivals = filtered.filter { it.isNew || (System.currentTimeMillis() - it.createdAt) < (7 * 24 * 60 * 60 * 1000L) }
        val bestSellers = filtered.filter { it.bestSeller }
        val recommended = filtered.take(12)

        featuredTattoosAdapter.submitList(if (featuredTattoos.isNotEmpty()) featuredTattoos else filtered.filter { it.category.contains("Tattoo", ignoreCase = true) || it.imageUrl.contains("tatto", ignoreCase = true) })
        featuredJewelleryAdapter.submitList(if (featuredJewellery.isNotEmpty()) featuredJewellery else filtered.filter { it.category.equals("Jewellery", ignoreCase = true) || it.imageUrl.contains("jewelry", ignoreCase = true) })
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
        intent.putExtra("SERVICE_NAME", product.name)
        startActivity(intent)
    }

    private fun addToCart(product: Product) {
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
    }

    private fun toggleWishlist(product: Product) {
        val userId = auth.currentUser?.uid ?: return
        val docId = if (product.productId.isNotEmpty()) product.productId else product.name.replace(" ", "_")
        val ref = db.collection("users").document(userId).collection("wishlist").document(docId)

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
                    (doc.getDouble("price") ?: 0.0) * (doc.getLong("quantity") ?: 1L).toDouble()
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
