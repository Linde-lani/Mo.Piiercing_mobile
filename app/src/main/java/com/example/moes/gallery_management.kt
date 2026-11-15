package com.example.moes

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.moes.databinding.ActivityGalleryManagementBinding
import com.example.moes.databinding.DialogAddJewelleryBinding
import com.example.moes.databinding.DialogAddTattooBinding
import com.example.moes.databinding.DialogAdminProductDetailBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Admin Gallery Management Activity handling real-time Firestore product creation,
 * updates, feature toggles, stock adjustments, and dashboard statistics.
 */
class gallery_management : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryManagementBinding
    private lateinit var db: FirebaseFirestore

    private lateinit var featuredTattooAdapter: TattooAdapter
    private lateinit var featuredJewelleryAdapter: JewelleryAdapter
    private lateinit var allProductsAdapter: GalleryAdapter

    private val allProducts = mutableListOf<Product>()
    private var currentSearchQuery = ""
    private var currentFilterOption = "All Products"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityGalleryManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        setupRecyclerViews()
        setupFilterDropdown()
        setupSearchBar()
        setupSwipeToRefresh()

        listenToFirestoreProducts()
    }

    private fun setupRecyclerViews() {
        // Featured Tattoos Horizontal List (Admin mode: 1st card is Add Tattoo)
        featuredTattooAdapter = TattooAdapter(
            isAdmin = true,
            onAddTattooClick = { showAddTattooDialog() },
            onProductClick = { product -> showAdminProductDetailDialog(product) },
            onOverflowClick = { product, view -> showProductOverflowMenu(product, view) }
        )
        binding.rvAdminFeaturedTattoos.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvAdminFeaturedTattoos.adapter = featuredTattooAdapter

        // Featured Jewellery Horizontal List (Admin mode: 1st card is Add Jewellery)
        featuredJewelleryAdapter = JewelleryAdapter(
            isAdmin = true,
            onAddJewelleryClick = { showAddJewelleryDialog() },
            onProductClick = { product -> showAdminProductDetailDialog(product) },
            onOverflowClick = { product, view -> showProductOverflowMenu(product, view) }
        )
        binding.rvAdminFeaturedJewellery.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvAdminFeaturedJewellery.adapter = featuredJewelleryAdapter

        // All Products Grid List
        allProductsAdapter = GalleryAdapter(
            isAdmin = true,
            onProductClick = { product -> showAdminProductDetailDialog(product) },
            onOverflowClick = { product, view -> showProductOverflowMenu(product, view) }
        )
        binding.rvAdminAllProducts.layoutManager = GridLayoutManager(this, 2)
        binding.rvAdminAllProducts.adapter = allProductsAdapter
    }

    private fun setupFilterDropdown() {
        val filters = arrayOf(
            "All Products",
            "Tattoos",
            "Jewellery",
            "Featured",
            "Hidden",
            "Low Stock",
            "Recently Added"
        )
        val dropdownAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, filters)
        binding.actvAdminFilter.setAdapter(dropdownAdapter)

        binding.actvAdminFilter.setOnItemClickListener { parent, _, position, _ ->
            currentFilterOption = parent.getItemAtPosition(position) as String
            applyFilterAndSearch()
        }
    }

    private fun setupSearchBar() {
        binding.etSearchAdmin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                applyFilterAndSearch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
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
     * Real-time Firestore snapshot listener for `products` collection.
     */
    private fun listenToFirestoreProducts() {
        db.collection("products")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading products", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots == null || snapshots.isEmpty) {
                    seedSampleFirestoreProducts()
                    return@addSnapshotListener
                }

                val products = snapshots.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.apply { productId = doc.id }
                }

                allProducts.clear()
                allProducts.addAll(products)

                updateDashboardStatistics()
                applyFilterAndSearch()
            }
    }

    /**
     * Seeds initial Firestore sample data if collection is empty.
     */
    private fun seedSampleFirestoreProducts() {
        val sampleList = listOf(
            Product("", "Flash Art A1 - Dragon", "Japanese traditional blackwork dragon tattoo design", 850.0, "https://images.unsplash.com/photo-1598371839696-5c5bb00bdc28?auto=format&fit=crop&w=600&q=80", "Tattoos", 1, featured = true, active = true, isNew = true, bestSeller = true),
            Product("", "Flash Art A2 - Rose", "Fine-line gothic black and grey rose flash art", 650.0, "https://images.unsplash.com/photo-1562962230-16e4623d36e6?auto=format&fit=crop&w=600&q=80", "Tattoos", 1, featured = true, active = true, isNew = false, bestSeller = true),
            Product("", "Flash Art A3 - Snake", "Minimalist line-art coiled viper tattoo design", 750.0, "https://images.unsplash.com/photo-1611501275019-9b5cda994e8d?auto=format&fit=crop&w=600&q=80", "Tattoos", 1, featured = false, active = true, isNew = true, bestSeller = false),
            Product("", "Titanium Septum Clicker", "16g 8mm ASTM F-136 implant-grade titanium septum ring", 350.0, "https://images.unsplash.com/photo-1606760227091-3dd850d97f1d?auto=format&fit=crop&w=600&q=80", "Jewellery", 15, featured = true, active = true, isNew = true, bestSeller = true),
            Product("", "Gold Opal Helix Stud", "14k solid gold threadless labret with Australian white opal", 550.0, "https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?auto=format&fit=crop&w=600&q=80", "Jewellery", 4, featured = true, active = true, isNew = true, bestSeller = false),
            Product("", "Double Nostril Hoop", "18g seamless gold anodized titanium nose hoop", 280.0, "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?auto=format&fit=crop&w=600&q=80", "Nose Piercing", 20, featured = false, active = true, isNew = false, bestSeller = true),
            Product("", "Crystal Navel Barbell", "14g curved titanium navel barbell with teardrop cubic zirconia", 420.0, "https://images.unsplash.com/photo-1600003014608-c2fb2162a87a?auto=format&fit=crop&w=600&q=80", "Belly Piercing", 3, featured = false, active = true, isNew = false, bestSeller = false)
        )

        for (item in sampleList) {
            db.collection("products").add(item)
        }
    }

    private fun updateDashboardStatistics() {
        val total = allProducts.size
        val tattoos = allProducts.count { it.category.contains("Tattoo", ignoreCase = true) }
        val jewellery = allProducts.count { !it.category.contains("Tattoo", ignoreCase = true) }
        val featured = allProducts.count { it.featured }
        val active = allProducts.count { it.active }
        val lowStock = allProducts.count { it.stock <= 5 }

        binding.tvStatTotalProducts.text = total.toString()
        binding.tvStatTotalTattoos.text = tattoos.toString()
        binding.tvStatTotalJewellery.text = jewellery.toString()
        binding.tvStatFeatured.text = featured.toString()
        binding.tvStatActive.text = active.toString()
        binding.tvStatLowStock.text = lowStock.toString()
    }

    private fun applyFilterAndSearch() {
        val filtered = allProducts.filter { prod ->
            val matchesFilter = when (currentFilterOption) {
                "Tattoos" -> prod.category.contains("Tattoo", ignoreCase = true)
                "Jewellery" -> !prod.category.contains("Tattoo", ignoreCase = true)
                "Featured" -> prod.featured
                "Hidden" -> !prod.active
                "Low Stock" -> prod.stock <= 5
                "Recently Added" -> (System.currentTimeMillis() - prod.createdAt) < (7 * 24 * 60 * 60 * 1000L)
                else -> true
            }

            val matchesSearch = if (currentSearchQuery.isEmpty()) true else {
                prod.name.contains(currentSearchQuery, ignoreCase = true) ||
                        prod.description.contains(currentSearchQuery, ignoreCase = true) ||
                        prod.category.contains(currentSearchQuery, ignoreCase = true)
            }

            matchesFilter && matchesSearch
        }

        val featuredTattoos = filtered.filter { it.category.contains("Tattoo", ignoreCase = true) && it.featured }
        val featuredJewellery = filtered.filter { !it.category.contains("Tattoo", ignoreCase = true) && it.featured }

        featuredTattooAdapter.submitList(featuredTattoos)
        featuredJewelleryAdapter.submitList(featuredJewellery)
        allProductsAdapter.submitList(filtered)
    }

    /**
     * Opens Add Tattoo Dialog.
     */
    private fun showAddTattooDialog() {
        val dialog = Dialog(this)
        val dialogBinding = DialogAddTattooBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnCancelTattoo.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSaveTattoo.setOnClickListener {
            val name = dialogBinding.etTattooName.text.toString().trim()
            val desc = dialogBinding.etTattooDesc.text.toString().trim()
            val priceStr = dialogBinding.etTattooPrice.text.toString().trim()
            val imageUrl = dialogBinding.etTattooImageUrl.text.toString().trim()
            val category = dialogBinding.actvTattooCategory.text.toString().ifEmpty { "Tattoos" }
            val isFeatured = dialogBinding.switchFeatured.isChecked
            val isActive = dialogBinding.switchActive.isChecked

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Please enter tattoo name and price", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull() ?: 0.0

            val product = Product(
                name = name,
                description = desc,
                price = price,
                imageUrl = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1598371839696-5c5bb00bdc28?auto=format&fit=crop&w=600&q=80" },
                category = category,
                stock = 1,
                featured = isFeatured,
                active = isActive,
                isNew = true,
                createdAt = System.currentTimeMillis()
            )

            db.collection("products").add(product)
                .addOnSuccessListener {
                    Toast.makeText(this, "Tattoo added successfully!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    /**
     * Opens Add Jewellery Dialog.
     */
    private fun showAddJewelleryDialog() {
        val dialog = Dialog(this)
        val dialogBinding = DialogAddJewelleryBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnCancelJewellery.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSaveJewellery.setOnClickListener {
            val name = dialogBinding.etJewelleryName.text.toString().trim()
            val desc = dialogBinding.etJewelleryDesc.text.toString().trim()
            val priceStr = dialogBinding.etJewelleryPrice.text.toString().trim()
            val stockStr = dialogBinding.etJewelleryStock.text.toString().trim()
            val imageUrl = dialogBinding.etJewelleryImageUrl.text.toString().trim()
            val category = dialogBinding.actvJewelleryCategory.text.toString().ifEmpty { "Jewellery" }
            val isFeatured = dialogBinding.switchFeaturedJewellery.isChecked
            val isActive = dialogBinding.switchActiveJewellery.isChecked

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Please enter product name and price", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull() ?: 0.0
            val stock = stockStr.toIntOrNull() ?: 10

            val product = Product(
                name = name,
                description = desc,
                price = price,
                imageUrl = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1606760227091-3dd850d97f1d?auto=format&fit=crop&w=600&q=80" },
                category = category,
                stock = stock,
                featured = isFeatured,
                active = isActive,
                isNew = true,
                createdAt = System.currentTimeMillis()
            )

            db.collection("products").add(product)
                .addOnSuccessListener {
                    Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    /**
     * Shows Three-dot Overflow Menu for Admin actions on product cards.
     */
    private fun showProductOverflowMenu(product: Product, view: View) {
        val popup = PopupMenu(this, view)
        popup.menu.add("Inspect Details")
        popup.menu.add(if (product.active) "Hide Product" else "Show Product")
        popup.menu.add(if (product.featured) "Remove Featured" else "Mark Featured")
        popup.menu.add("Delete Product")

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Inspect Details" -> showAdminProductDetailDialog(product)
                "Hide Product", "Show Product" -> toggleActiveState(product)
                "Mark Featured", "Remove Featured" -> toggleFeaturedState(product)
                "Delete Product" -> deleteProduct(product)
            }
            true
        }
        popup.show()
    }

    private fun toggleActiveState(product: Product) {
        val newState = !product.active
        db.collection("products").document(product.productId)
            .update("active", newState)
            .addOnSuccessListener {
                val label = if (newState) "Visible" else "Hidden"
                Toast.makeText(this, "${product.name} is now $label", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleFeaturedState(product: Product) {
        val newState = !product.featured
        db.collection("products").document(product.productId)
            .update("featured", newState)
            .addOnSuccessListener {
                val label = if (newState) "Featured" else "Unfeatured"
                Toast.makeText(this, "${product.name} marked as $label", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteProduct(product: Product) {
        db.collection("products").document(product.productId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "${product.name} deleted", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Shows Admin Product Detail Dialog.
     */
    private fun showAdminProductDetailDialog(product: Product) {
        val dialog = Dialog(this)
        val dialogBinding = DialogAdminProductDetailBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.dialogTvProductName.text = product.name
        dialogBinding.dialogTvCategory.text = "Category: ${product.category}"
        dialogBinding.dialogTvPrice.text = "Price: R${String.format("%.2f", product.price)}"
        dialogBinding.dialogTvDescription.text = product.description.ifEmpty { "No description available." }
        dialogBinding.dialogTvStock.text = "Available Stock: ${product.stock} items"
        dialogBinding.dialogTvStatus.text = "Featured: ${if (product.featured) "Yes" else "No"} | Active: ${if (product.active) "Yes" else "No"}"

        Glide.with(this)
            .load(product.imageUrl)
            .placeholder(R.drawable.sparkle_motion)
            .into(dialogBinding.dialogIvProduct)

        dialogBinding.btnDialogDelete.setOnClickListener {
            deleteProduct(product)
            dialog.dismiss()
        }

        dialogBinding.btnDialogEdit.setOnClickListener {
            dialog.dismiss()
            toggleFeaturedState(product)
        }

        dialogBinding.btnDialogClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
