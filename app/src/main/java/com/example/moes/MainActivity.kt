package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moes.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Customer Home Dashboard Activity for Mo Piercings.
 * Displays luxury hero banner, business stats, upcoming bookings, featured tattoos,
 * featured jewellery, new arrivals, best sellers, studio services, cart summary, client reviews,
 * and quick shortcut actions dynamically from Firebase Firestore.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var featuredTattooAdapter: FeaturedTattooAdapter
    private lateinit var featuredJewelleryAdapter: FeaturedJewelleryAdapter
    private lateinit var newArrivalsAdapter: FeaturedTattooAdapter
    private lateinit var bestSellersAdapter: FeaturedJewelleryAdapter
    private lateinit var quickActionsAdapter: HomeQuickActionsAdapter
    private lateinit var servicesAdapter: ServiceHighlightAdapter
    private lateinit var reviewsAdapter: ReviewsAdapter

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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check Role Verification & Fetch Profile
        verifyUserRoleAndFetchProfile(currentUser.uid, currentUser.email ?: "")

        // Initialize UI components and Adapters
        setupRecyclerViews()
        setupClickListeners()
        setupNavigation(R.id.nav_home)

        // Load Data from Firebase Firestore
        loadDashboardData(currentUser.email ?: "")
    }

    private fun verifyUserRoleAndFetchProfile(userId: String, email: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")?.uppercase() ?: "CUSTOMER"
                    if (role == "ADMIN") {
                        startActivity(Intent(this, AdminMain::class.java))
                        finish()
                        return@addOnSuccessListener
                    }

                    val name = document.getString("username")
                        ?: document.getString("name")
                        ?: email.substringBefore("@")
                    binding.tvWelcomeGreeting.text = "Welcome Back, $name 👋"
                }
            }
            .addOnFailureListener {
                binding.tvWelcomeGreeting.text = "Welcome Back 👋"
            }
    }

    private fun setupRecyclerViews() {
        // Quick Shortcuts Adapter
        quickActionsAdapter = HomeQuickActionsAdapter { action ->
            handleQuickAction(action)
        }
        binding.rvQuickActions.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvQuickActions.adapter = quickActionsAdapter
        quickActionsAdapter.submitList(getQuickActionsList())

        // Featured Tattoos Adapter
        featuredTattooAdapter = FeaturedTattooAdapter(
            onBookClick = { product -> openBookingForProduct(product) },
            onAddToCartClick = { product -> addProductToCart(product) }
        )
        binding.rvFeaturedTattoos.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFeaturedTattoos.adapter = featuredTattooAdapter

        // Featured Jewellery Adapter
        featuredJewelleryAdapter = FeaturedJewelleryAdapter(
            onBookClick = { product -> openBookingForProduct(product) },
            onAddToCartClick = { product -> addProductToCart(product) }
        )
        binding.rvFeaturedJewellery.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFeaturedJewellery.adapter = featuredJewelleryAdapter

        // New Arrivals Adapter
        newArrivalsAdapter = FeaturedTattooAdapter(
            onBookClick = { product -> openBookingForProduct(product) },
            onAddToCartClick = { product -> addProductToCart(product) }
        )
        binding.rvNewArrivals.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvNewArrivals.adapter = newArrivalsAdapter

        // Best Sellers Adapter
        bestSellersAdapter = FeaturedJewelleryAdapter(
            onBookClick = { product -> openBookingForProduct(product) },
            onAddToCartClick = { product -> addProductToCart(product) }
        )
        binding.rvBestSellers.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBestSellers.adapter = bestSellersAdapter

        // Studio Services Adapter
        servicesAdapter = ServiceHighlightAdapter { service ->
            val intent = Intent(this, BookingScreen::class.java)
            intent.putExtra("SERVICE_NAME", service.title)
            startActivity(intent)
        }
        binding.rvServices.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvServices.adapter = servicesAdapter
        servicesAdapter.submitList(getStudioServicesList())

        // Customer Reviews Adapter
        reviewsAdapter = ReviewsAdapter()
        binding.rvCustomerReviews.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCustomerReviews.adapter = reviewsAdapter
    }

    private fun setupClickListeners() {
        binding.btnHeroBook.setOnClickListener {
            startActivity(Intent(this, BookingScreen::class.java))
        }

        binding.btnHeroGallery.setOnClickListener {
            startActivity(Intent(this, GalleryScreen::class.java))
        }

        binding.btnCart.setOnClickListener {
            startActivity(Intent(this, cart::class.java))
        }

        binding.btnViewCartSummary.setOnClickListener {
            startActivity(Intent(this, cart::class.java))
        }

        binding.btnNotifications.setOnClickListener {
            Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show()
        }

        binding.btnNoBookingAction.setOnClickListener {
            startActivity(Intent(this, BookingScreen::class.java))
        }

        binding.btnViewBookings.setOnClickListener {
            startActivity(Intent(this, my_bookings::class.java))
        }

        binding.tvSeeAllTattoos.setOnClickListener {
            startActivity(Intent(this, GalleryScreen::class.java))
        }

        binding.tvSeeAllJewellery.setOnClickListener {
            startActivity(Intent(this, GalleryScreen::class.java))
        }
    }

    private fun loadDashboardData(userEmail: String) {
        // Fetch Products from Firestore
        db.collection("products")
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { documents ->
                val allProducts = documents.mapNotNull { doc ->
                    val product = doc.toObject(Product::class.java)
                    product.productId = doc.id
                    product
                }

                // Filter Featured Tattoos
                val featuredTattoos = allProducts.filter {
                    it.featured && it.category.equals("Tattoo", ignoreCase = true)
                }
                featuredTattooAdapter.submitList(if (featuredTattoos.isNotEmpty()) featuredTattoos else allProducts.take(4))

                // Filter Featured Jewellery
                val featuredJewellery = allProducts.filter {
                    it.featured && it.category.equals("Jewellery", ignoreCase = true)
                }
                featuredJewelleryAdapter.submitList(if (featuredJewellery.isNotEmpty()) featuredJewellery else allProducts.take(4))

                // Filter New Arrivals
                val newArrivals = allProducts.filter { it.isNew }
                newArrivalsAdapter.submitList(if (newArrivals.isNotEmpty()) newArrivals else allProducts.take(4))

                // Filter Best Sellers
                val bestSellers = allProducts.filter { it.bestSeller }
                bestSellersAdapter.submitList(if (bestSellers.isNotEmpty()) bestSellers else allProducts.take(4))
            }
            .addOnFailureListener {
                // Keep default empty states
            }

        // Fetch Upcoming Bookings for User
        db.collection("bookings")
            .whereEqualTo("customerEmail", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                val userBookings = documents.mapNotNull { doc ->
                    val booking = doc.toObject(Booking::class.java)
                    booking.bookingId = doc.id
                    booking
                }.sortedByDescending { it.bookingDate }

                val upcoming = userBookings.firstOrNull {
                    it.status.equals("Pending", ignoreCase = true) ||
                            it.status.equals("Confirmed", ignoreCase = true)
                }

                if (upcoming != null) {
                    binding.cardUpcomingBooking.visibility = View.VISIBLE
                    binding.layoutNoBookings.visibility = View.GONE
                    binding.tvUpcomingService.text = upcoming.serviceName
                    binding.tvUpcomingDateTime.text = "📅 ${upcoming.bookingDate} | ⏰ ${upcoming.bookingTime}"
                    binding.tvUpcomingStatus.text = upcoming.status
                } else {
                    binding.cardUpcomingBooking.visibility = View.GONE
                    binding.layoutNoBookings.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                binding.cardUpcomingBooking.visibility = View.GONE
                binding.layoutNoBookings.visibility = View.VISIBLE
            }

        // Fetch Cart Summary & Count
        db.collection("cart")
            .whereEqualTo("userEmail", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                val count = documents.size()
                var totalPrice = 0.0
                for (doc in documents) {
                    totalPrice += (doc.getDouble("price") ?: 0.0) * (doc.getLong("quantity") ?: 1L).toDouble()
                }

                if (count > 0) {
                    binding.tvCartBadge.text = count.toString()
                    binding.tvCartBadge.visibility = View.VISIBLE
                    binding.tvCartSummaryDetails.text = "$count item(s) | Total: R ${"%.2f".format(totalPrice)}"
                } else {
                    binding.tvCartBadge.visibility = View.GONE
                    binding.tvCartSummaryDetails.text = "0 items | Total: R 0.00"
                }
            }

        // Fetch Customer Reviews
        db.collection("reviews")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(6)
            .get()
            .addOnSuccessListener { documents ->
                val reviewsList = documents.mapNotNull { doc ->
                    val review = doc.toObject(Review::class.java)
                    review.reviewId = doc.id
                    review
                }
                if (reviewsList.isNotEmpty()) {
                    reviewsAdapter.submitList(reviewsList)
                } else {
                    reviewsAdapter.submitList(getSampleReviewsList())
                }
            }
            .addOnFailureListener {
                reviewsAdapter.submitList(getSampleReviewsList())
            }
    }

    private fun handleQuickAction(action: QuickAction) {
        when (action.actionType) {
            "BOOK" -> startActivity(Intent(this, BookingScreen::class.java))
            "GALLERY" -> startActivity(Intent(this, GalleryScreen::class.java))
            "CART" -> startActivity(Intent(this, cart::class.java))
            "MY_BOOKINGS" -> startActivity(Intent(this, my_bookings::class.java))
            "PROFILE" -> startActivity(Intent(this, ProfileScreen::class.java))
        }
    }

    private fun openBookingForProduct(product: Product) {
        val intent = Intent(this, BookingScreen::class.java)
        intent.putExtra("SERVICE_NAME", product.name)
        startActivity(intent)
    }

    private fun addProductToCart(product: Product) {
        val userEmail = auth.currentUser?.email ?: return
        val cartItem = hashMapOf(
            "userEmail" to userEmail,
            "productId" to product.productId,
            "productName" to product.name,
            "price" to product.price,
            "imageUrl" to product.imageUrl,
            "quantity" to 1,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("cart").add(cartItem)
            .addOnSuccessListener {
                Toast.makeText(this, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                loadDashboardData(userEmail)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getQuickActionsList(): List<QuickAction> {
        return listOf(
            QuickAction("Book Appointment", R.drawable.ic_calendar_24, "BOOK"),
            QuickAction("Browse Gallery", R.drawable.ic_gallery_24, "GALLERY"),
            QuickAction("View Cart", R.drawable.ic_jewelry_24, "CART"),
            QuickAction("My Bookings", R.drawable.ic_calendar_24, "MY_BOOKINGS"),
            QuickAction("My Profile", R.drawable.ic_profile, "PROFILE")
        )
    }

    private fun getStudioServicesList(): List<ServiceHighlight> {
        return listOf(
            ServiceHighlight("1", "Small Custom Tattoo", "Fine line, minimalist, and small symbol tattoos.", "From R450", R.drawable.tatto_1),
            ServiceHighlight("2", "Large Custom Tattoo", "Full sleeve, back piece, & detailed custom art.", "From R1,200", R.drawable.tatto_5),
            ServiceHighlight("3", "Ear Piercings", "Lobe, helix, tragus, & industrial styling.", "From R250", R.drawable.ear_1),
            ServiceHighlight("4", "Nose Piercings", "Nostril and septum piercing with titanium jewelry.", "From R300", R.drawable.nose_1),
            ServiceHighlight("5", "Industrial Piercings", "Double ear cartilage piercing with barbell.", "From R350", R.drawable.ear_2),
            ServiceHighlight("6", "Belly Piercings", "Navel piercing with premium gold & titanium studs.", "From R350", R.drawable.belly_1)
        )
    }

    private fun getSampleReviewsList(): List<Review> {
        return listOf(
            Review("1", "Sarah Jenkins", "Got my first helix piercing here! Super clean studio, incredibly professional staff, and painless execution.", 5.0f, "2 days ago"),
            Review("2", "Marcus Wright", "Incredible custom tattoo work! Mo turned my rough sketch into a masterpiece. 10/10 recommendation.", 5.0f, "1 week ago"),
            Review("3", "Elena Rostova", "Best body piercing studio in town. High quality titanium jewellery and strict hygiene standards.", 5.0f, "2 weeks ago")
        )
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
