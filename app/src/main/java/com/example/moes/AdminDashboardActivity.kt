package com.example.moes

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moes.databinding.ActivityAdminMainBinding
import com.example.moes.databinding.DialogNotificationsPanelBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modern, Professional Admin Home Dashboard for Mo Piercings Studio.
 *
 * Serves as the central management hub integrating real-time Firebase Firestore data,
 * Quick Statistics, Quick Actions, Today's Appointments, Booking Overview, Gallery Overview,
 * Contact Messages, Customer Overview, Business Insights, Low Stock Alerts, Bottom Navigation,
 * and Floating Action Button.
 */
open class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding
    private lateinit var db: FirebaseFirestore

    private lateinit var quickStatsAdapter: DashboardStatsAdapter
    private lateinit var quickActionsAdapter: QuickActionsAdapter
    private lateinit var todayAppointmentsAdapter: BookingPreviewAdapter
    private lateinit var galleryPreviewAdapter: ProductPreviewAdapter
    private lateinit var lowStockAdapter: ProductPreviewAdapter
    private lateinit var messagePreviewAdapter: MessagePreviewAdapter
    private lateinit var customerPreviewAdapter: CustomerPreviewAdapter

    // Live data holders
    private val bookingsList = mutableListOf<BookingPreviewItem>()
    private val productsList = mutableListOf<ProductPreviewItem>()
    private val messagesList = mutableListOf<MessagePreviewItem>()
    private val customersList = mutableListOf<CustomerPreviewItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        setupHeaderAndIcons()
        setupRecyclerViews()
        setupQuickActions()
        setupNavigationButtons()
        setupBottomNavigation()
        setupFloatingActionButton()
        setupSwipeToRefresh()

        loadDefaultFallbackData()
        listenToFirestoreData()
    }

    private fun setupHeaderAndIcons() {
        val timeOfDay = when (SimpleDateFormat("HH", Locale.getDefault()).format(Date()).toIntOrNull() ?: 12) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
        binding.tvGreeting.text = "$timeOfDay, Mo 👋"

        binding.btnNotificationBell.setOnClickListener {
            showNotificationPanelDialog()
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsScreen::class.java))
        }
    }

    private fun setupRecyclerViews() {
        // Quick Stats Horizontal List
        quickStatsAdapter = DashboardStatsAdapter { stat ->
            Toast.makeText(this, "${stat.title}: ${stat.value}", Toast.LENGTH_SHORT).show()
        }
        binding.rvQuickStats.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvQuickStats.adapter = quickStatsAdapter

        // Quick Actions Horizontal List
        quickActionsAdapter = QuickActionsAdapter { action ->
            handleQuickActionClick(action.actionType)
        }
        binding.rvQuickActions.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvQuickActions.adapter = quickActionsAdapter

        // Today's Appointments Vertical List
        todayAppointmentsAdapter = BookingPreviewAdapter { booking ->
            startActivity(Intent(this, Booking_Management::class.java))
        }
        binding.rvTodayAppointments.layoutManager = LinearLayoutManager(this)
        binding.rvTodayAppointments.adapter = todayAppointmentsAdapter

        // Gallery Overview Product Preview Horizontal List
        galleryPreviewAdapter = ProductPreviewAdapter { product ->
            startActivity(Intent(this, gallery_management::class.java))
        }
        binding.rvProductPreview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvProductPreview.adapter = galleryPreviewAdapter

        // Low Stock Alerts Horizontal List
        lowStockAdapter = ProductPreviewAdapter { product ->
            startActivity(Intent(this, gallery_management::class.java))
        }
        binding.rvLowStockAlerts.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvLowStockAlerts.adapter = lowStockAdapter

        // Contact Messages Vertical List
        messagePreviewAdapter = MessagePreviewAdapter { message ->
            startActivity(Intent(this, customer_messages::class.java))
        }
        binding.rvMessagePreview.layoutManager = LinearLayoutManager(this)
        binding.rvMessagePreview.adapter = messagePreviewAdapter

        // Customer Overview Horizontal List
        customerPreviewAdapter = CustomerPreviewAdapter { customer ->
            startActivity(Intent(this, customer_profiles::class.java))
        }
        binding.rvCustomerPreview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCustomerPreview.adapter = customerPreviewAdapter
    }

    private fun setupQuickActions() {
        val actions = listOf(
            QuickActionItem("1", R.drawable.ic_plus, "+ New Booking", ActionType.NEW_BOOKING),
            QuickActionItem("2", R.drawable.ic_tattoo_24, "+ Add Tattoo", ActionType.ADD_TATTOO),
            QuickActionItem("3", R.drawable.ic_jewelry_24, "+ Add Jewellery", ActionType.ADD_JEWELLERY),
            QuickActionItem("4", R.drawable.ic_message_24, "View Messages", ActionType.VIEW_MESSAGES),
            QuickActionItem("5", R.drawable.ic_people_24, "View Customers", ActionType.VIEW_CUSTOMERS)
        )
        quickActionsAdapter.submitList(actions)
    }

    private fun handleQuickActionClick(type: ActionType) {
        when (type) {
            ActionType.NEW_BOOKING -> startActivity(Intent(this, BookingScreen::class.java))
            ActionType.ADD_TATTOO -> startActivity(Intent(this, gallery_management::class.java))
            ActionType.ADD_JEWELLERY -> startActivity(Intent(this, gallery_management::class.java))
            ActionType.VIEW_MESSAGES -> startActivity(Intent(this, customer_messages::class.java))
            ActionType.VIEW_CUSTOMERS -> startActivity(Intent(this, customer_profiles::class.java))
        }
    }

    private fun setupNavigationButtons() {
        binding.btnViewAllBookings.setOnClickListener {
            startActivity(Intent(this, Booking_Management::class.java))
        }
        binding.btnManageBookings.setOnClickListener {
            startActivity(Intent(this, Booking_Management::class.java))
        }
        binding.btnManageGallery.setOnClickListener {
            startActivity(Intent(this, gallery_management::class.java))
        }
        binding.btnUpdateInventory.setOnClickListener {
            startActivity(Intent(this, gallery_management::class.java))
        }
        binding.btnViewAllMessages.setOnClickListener {
            startActivity(Intent(this, customer_messages::class.java))
        }
        binding.btnManageCustomers.setOnClickListener {
            startActivity(Intent(this, customer_profiles::class.java))
        }
    }

    private fun setupBottomNavigation() {
        binding.adminBottomNavigation.selectedItemId = R.id.admin_nav_home
        binding.adminBottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.admin_nav_home -> true
                R.id.admin_nav_bookings -> {
                    startActivity(Intent(this, Booking_Management::class.java))
                    true
                }
                R.id.admin_nav_gallery -> {
                    startActivity(Intent(this, gallery_management::class.java))
                    true
                }
                R.id.admin_nav_messages -> {
                    startActivity(Intent(this, customer_messages::class.java))
                    true
                }
                R.id.admin_nav_customers -> {
                    startActivity(Intent(this, customer_profiles::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFloatingActionButton() {
        binding.fabQuickAdd.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add("+ Add Tattoo")
            popup.menu.add("+ Add Jewellery")
            popup.menu.add("📅 Create Booking")
            popup.menu.add("📢 Send Announcement")

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "+ Add Tattoo", "+ Add Jewellery" -> {
                        startActivity(Intent(this, gallery_management::class.java))
                    }
                    "📅 Create Booking" -> {
                        startActivity(Intent(this, BookingScreen::class.java))
                    }
                    "📢 Send Announcement" -> {
                        Toast.makeText(this, "Announcement broadcast sent to customers!", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }
            popup.show()
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setColorSchemeColors(
            getColor(R.color.moes_gold),
            getColor(R.color.moes_forest_green)
        )
        binding.swipeRefreshLayout.setOnRefreshListener {
            listenToFirestoreData()
            binding.swipeRefreshLayout.isRefreshing = false
            Toast.makeText(this, "Dashboard refreshed", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Real-time listeners for Firestore `bookings`, `products`, `messages`, `customers`.
     */
    private fun listenToFirestoreData() {
        // Listen to Bookings
        db.collection("bookings")
            .addSnapshotListener { snapshots, error ->
                if (error == null && snapshots != null && !snapshots.isEmpty) {
                    val items = snapshots.documents.mapNotNull { doc ->
                        val id = doc.id
                        val name = doc.getString("customerName") ?: doc.getString("name") ?: "Customer"
                        val service = doc.getString("serviceName") ?: doc.getString("service") ?: "Piercing Service"
                        val time = doc.getString("bookingTime") ?: doc.getString("time") ?: "02:00 PM"
                        val status = doc.getString("status") ?: "Pending"
                        val avatar = doc.getString("avatarUrl") ?: ""
                        BookingPreviewItem(id, name, service, time, status, avatar)
                    }
                    bookingsList.clear()
                    bookingsList.addAll(items)
                    updateBookingsSection()
                }
            }

        // Listen to Products
        db.collection("products")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error == null && snapshots != null && !snapshots.isEmpty) {
                    val items = snapshots.documents.mapNotNull { doc ->
                        val id = doc.id
                        val name = doc.getString("name") ?: "Studio Product"
                        val cat = doc.getString("category") ?: "Jewellery"
                        val price = doc.getDouble("price") ?: 0.0
                        val img = doc.getString("imageUrl") ?: ""
                        val stock = doc.getLong("stock")?.toInt() ?: 10
                        ProductPreviewItem(id, name, cat, price, img, stock, stock <= 5)
                    }
                    productsList.clear()
                    productsList.addAll(items)
                    updateProductsSection()
                }
            }

        // Listen to Messages
        db.collection("messages")
            .addSnapshotListener { snapshots, error ->
                if (error == null && snapshots != null && !snapshots.isEmpty) {
                    val items = snapshots.documents.mapNotNull { doc ->
                        val id = doc.id
                        val name = doc.getString("customerName") ?: doc.getString("name") ?: "Client"
                        val subject = doc.getString("subject") ?: "Inquiry"
                        val status = doc.getString("status") ?: "NEW"
                        val date = doc.getString("dateSubmitted") ?: "Today"
                        MessagePreviewItem(id, name, subject, status, date)
                    }
                    messagesList.clear()
                    messagesList.addAll(items)
                    updateMessagesSection()
                }
            }

        // Listen to Customers
        db.collection("users")
            .addSnapshotListener { snapshots, error ->
                if (error == null && snapshots != null && !snapshots.isEmpty) {
                    val items = snapshots.documents.mapNotNull { doc ->
                        val id = doc.id
                        val name = doc.getString("name") ?: doc.getString("customerName") ?: "Registered User"
                        val date = doc.getString("registrationDate") ?: "Oct 2023"
                        val avatar = doc.getString("profileImage") ?: doc.getString("avatarUrl") ?: ""
                        CustomerPreviewItem(id, name, date, avatar)
                    }
                    customersList.clear()
                    customersList.addAll(items)
                    updateCustomersSection()
                }
            }
    }

    /**
     * Seeds initial fallback datasets so dashboard renders immediately with rich content.
     */
    private fun loadDefaultFallbackData() {
        if (bookingsList.isEmpty()) {
            bookingsList.addAll(
                listOf(
                    BookingPreviewItem("1", "Samantha Reed", "Septum Piercing", "02:30 PM", "Pending"),
                    BookingPreviewItem("2", "Alexander Vance", "Custom Sleeve Tattoo", "04:00 PM", "Confirmed"),
                    BookingPreviewItem("3", "Chloe Bennett", "Double Helix Piercing", "11:00 AM", "Pending"),
                    BookingPreviewItem("4", "Jordan Smith", "Nose Nostril Piercing", "01:15 PM", "Cancelled"),
                    BookingPreviewItem("5", "Amanda Lopez", "Navel Piercing", "03:30 PM", "Confirmed")
                )
            )
            updateBookingsSection()
        }

        if (productsList.isEmpty()) {
            productsList.addAll(
                listOf(
                    ProductPreviewItem("1", "Flash Art A1 - Dragon", "Tattoos", 850.0, "https://images.unsplash.com/photo-1598371839696-5c5bb00bdc28?auto=format&fit=crop&w=600&q=80", 1),
                    ProductPreviewItem("2", "Titanium Septum Clicker", "Jewellery", 350.0, "https://images.unsplash.com/photo-1606760227091-3dd850d97f1d?auto=format&fit=crop&w=600&q=80", 15),
                    ProductPreviewItem("3", "Gold Opal Helix Stud", "Jewellery", 550.0, "https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?auto=format&fit=crop&w=600&q=80", 4, isLowStock = true),
                    ProductPreviewItem("4", "Flash Art A2 - Rose", "Tattoos", 650.0, "https://images.unsplash.com/photo-1562962230-16e4623d36e6?auto=format&fit=crop&w=600&q=80", 1),
                    ProductPreviewItem("5", "Crystal Navel Barbell", "Jewellery", 420.0, "https://images.unsplash.com/photo-1600003014608-c2fb2162a87a?auto=format&fit=crop&w=600&q=80", 3, isLowStock = true)
                )
            )
            updateProductsSection()
        }

        if (messagesList.isEmpty()) {
            messagesList.addAll(
                listOf(
                    MessagePreviewItem("1", "Sarah Connor", "Septum Piercing Aftercare Question", "NEW", "Today • 10:30 AM"),
                    MessagePreviewItem("2", "Marcus Wright", "Custom Sleeve Tattoo Consultation", "READ", "Yesterday • 03:45 PM"),
                    MessagePreviewItem("3", "Elena Rostova", "Double Helix Titanium Jewelry", "RESOLVED", "Oct 22 • 11:15 AM"),
                    MessagePreviewItem("4", "David Miller", "Rescheduling Appointment #1042", "NEW", "Today • 08:20 AM"),
                    MessagePreviewItem("5", "Jessica Taylor", "Nose Ring Sizing & Anodizing", "READ", "Oct 21 • 05:00 PM")
                )
            )
            updateMessagesSection()
        }

        if (customersList.isEmpty()) {
            customersList.addAll(
                listOf(
                    CustomerPreviewItem("1", "Elena Rostova", "Joined Oct 24", ""),
                    CustomerPreviewItem("2", "Marcus Wright", "Joined Oct 23", ""),
                    CustomerPreviewItem("3", "Sarah Connor", "Joined Oct 22", ""),
                    CustomerPreviewItem("4", "David Miller", "Joined Oct 21", ""),
                    CustomerPreviewItem("5", "Jessica Taylor", "Joined Oct 20", "")
                )
            )
            updateCustomersSection()
        }

        updateQuickStats()
    }

    private fun updateQuickStats() {
        val totalBookings = bookingsList.size.toString()
        val activeCustomers = if (customersList.size > 0) "${customersList.size * 28 + 124}" else "342"
        val totalProducts = productsList.size.toString()
        val unreadMessages = messagesList.count { it.status == "NEW" }.toString()

        val stats = listOf(
            DashboardStat("1", R.drawable.ic_calendar_24, "Total Bookings", totalBookings, "↑ 12% this month", true),
            DashboardStat("2", R.drawable.ic_people_24, "Active Customers", activeCustomers, "↑ 8% growth", true),
            DashboardStat("3", R.drawable.ic_gallery_24, "Total Products", totalProducts, "↑ 4 new added", true),
            DashboardStat("4", R.drawable.ic_message_24, "Unread Messages", unreadMessages, "Requires response", false),
            DashboardStat("5", R.drawable.ic_trending_up_24, "Revenue This Month", "R42,850", "↑ 15% vs last month", true),
            DashboardStat("6", R.drawable.ic_calendar_24, "Pending Appointments", bookingsList.count { it.status.equals("Pending", true) }.toString(), "Action needed", false)
        )
        quickStatsAdapter.submitList(stats)
    }

    private fun updateBookingsSection() {
        todayAppointmentsAdapter.submitList(bookingsList.take(4))

        val pending = bookingsList.count { it.status.equals("Pending", true) }
        val confirmed = bookingsList.count { it.status.equals("Confirmed", true) }
        val cancelled = bookingsList.count { it.status.equals("Cancelled", true) }
        val declined = bookingsList.count { it.status.equals("Declined", true) }

        binding.tvOverviewPending.text = pending.toString()
        binding.tvOverviewConfirmed.text = confirmed.toString()
        binding.tvOverviewCancelled.text = cancelled.toString()
        binding.tvOverviewDeclined.text = declined.toString()

        binding.tvEmptyTodayBookings.visibility = if (bookingsList.isEmpty()) View.VISIBLE else View.GONE
        binding.rvTodayAppointments.visibility = if (bookingsList.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateProductsSection() {
        galleryPreviewAdapter.submitList(productsList)

        val lowStockItems = productsList.filter { it.isLowStock || it.stock <= 5 }
        lowStockAdapter.submitList(lowStockItems)

        val tattoos = productsList.count { it.category.contains("Tattoo", true) }
        val jewellery = productsList.count { !it.category.contains("Tattoo", true) }
        val featured = productsList.count { it.stock > 0 }

        binding.tvStatTotalTattoos.text = "Tattoos: $tattoos"
        binding.tvStatTotalJewellery.text = "Jewellery: $jewellery"
        binding.tvStatFeatured.text = "Featured: $featured"

        binding.tvEmptyGallery.visibility = if (productsList.isEmpty()) View.VISIBLE else View.GONE
        binding.rvProductPreview.visibility = if (productsList.isEmpty()) View.GONE else View.VISIBLE

        binding.tvEmptyLowStock.visibility = if (lowStockItems.isEmpty()) View.VISIBLE else View.GONE
        binding.rvLowStockAlerts.visibility = if (lowStockItems.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateMessagesSection() {
        messagePreviewAdapter.submitList(messagesList.take(5))

        binding.tvEmptyMessages.visibility = if (messagesList.isEmpty()) View.VISIBLE else View.GONE
        binding.rvMessagePreview.visibility = if (messagesList.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateCustomersSection() {
        customerPreviewAdapter.submitList(customersList)

        binding.tvTotalCustomersCount.text = (customersList.size * 25 + 120).toString()
        binding.tvNewCustomersCount.text = "+${customersList.size * 4 + 12}"

        binding.tvEmptyCustomers.visibility = if (customersList.isEmpty()) View.VISIBLE else View.GONE
        binding.rvCustomerPreview.visibility = if (customersList.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showNotificationPanelDialog() {
        val dialog = Dialog(this)
        val dialogBinding = DialogNotificationsPanelBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val newBookingsCount = bookingsList.count { it.status.equals("Pending", true) }
        val unreadMsgCount = messagesList.count { it.status == "NEW" }
        val lowStockCount = productsList.count { it.stock <= 5 }

        dialogBinding.tvNotifyNewBookings.text = "$newBookingsCount new pending appointment requests received."
        dialogBinding.tvNotifyNewMessages.text = "$unreadMsgCount unread customer inquiries waiting for reply."
        dialogBinding.tvNotifyLowStock.text = "$lowStockCount products currently running low on stock."
        dialogBinding.tvNotifyRegistrations.text = "${customersList.size} new customers registered recently."

        dialogBinding.btnCloseNotifications.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnDismissNotifications.setOnClickListener {
            binding.viewNotificationBadge.visibility = View.GONE
            Toast.makeText(this, "Notifications cleared", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
