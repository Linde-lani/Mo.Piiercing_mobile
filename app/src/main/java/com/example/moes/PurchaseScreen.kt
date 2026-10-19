package com.example.moes

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity



import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Data class for cart items
data class CartItem(
    var id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val imageUrl: String = ""
)

class PurchaseScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var cartItemsContainer: LinearLayout
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvItemCount: TextView

    private var subtotalPrice: Double = 0.0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_screen)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // UI Initialization - Ensure these IDs exist in activity_purchase_screen.xml
        cartItemsContainer = findViewById(R.id.cartItemsList)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvTotal = findViewById(R.id.tvTotal)
        tvItemCount = findViewById(R.id.tvItemCount)

        loadCartItems()

//        findViewById<Button>(R.id.btnPayNow).setOnClickListener {
//            // Navigate to PaymentActivity (ensure this activity exists)
//            val intent = Intent(this, PaymentActivity::class.java)
//            intent.putExtra("TOTAL_PRICE", subtotalPrice)
//            startActivity(intent)
//        }

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            finish() // Returns to the previous screen (Gallery)
        }
    }

    private fun loadCartItems() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("carts").document(userId).collection("items")
            .get()
            .addOnSuccessListener { documents ->
                cartItemsContainer.removeAllViews()
                subtotalPrice = 0.0
                var totalItems = 0

                for (document in documents) {
                    val item = document.toObject(CartItem::class.java).apply { id = document.id }
                    addCartItemToView(item)
                    subtotalPrice += (item.price * item.quantity)
                    totalItems += item.quantity
                }
                updateSummary(totalItems)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("MissingInflatedId")
    private fun addCartItemToView(item: CartItem) {
        val inflater = LayoutInflater.from(this)

        // FIX: Change R.layout.activity_purchase_screen to R.layout.item_cart_card
        val itemView = inflater.inflate(R.layout.activity_item_cart_card, cartItemsContainer, false)

        // Bind Views
        val tvName = itemView.findViewById<TextView>(R.id.tvItemName)
        val tvPriceEach = itemView.findViewById<TextView>(R.id.tvPriceEach)
        val tvLinePrice = itemView.findViewById<TextView>(R.id.tvLinePrice)
        val tvQty = itemView.findViewById<TextView>(R.id.tvQty)
        val btnRemove = itemView.findViewById<TextView>(R.id.btnRemove)
        val btnAdd = itemView.findViewById<ImageView>(R.id.ivAdd)
        val btnMinus = itemView.findViewById<ImageView>(R.id.ivMinus)
        val ivItem = itemView.findViewById<ImageView>(R.id.itemImage)

        // Populate Data
        tvName.text = item.name
        tvPriceEach.text = "R ${item.price.toInt()} each"
        tvLinePrice.text = "R ${(item.price * item.quantity).toInt()}"
        tvQty.text = item.quantity.toString()

        // Button Logic
        btnRemove.setOnClickListener { removeItem(item.id) }

        btnAdd.setOnClickListener {
            updateQuantity(item.id, item.quantity + 1)
        }

        btnMinus.setOnClickListener {
            if (item.quantity > 1) {
                updateQuantity(item.id, item.quantity - 1)
            } else {
                removeItem(item.id)
            }
        }

        // Add the dynamic row to the container
        cartItemsContainer.addView(itemView)
    }

    private fun updateQuantity(itemId: String, newQty: Int) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("carts").document(userId).collection("items").document(itemId)
            .update("quantity", newQty)
            .addOnSuccessListener { loadCartItems() }
    }

    private fun removeItem(itemId: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("carts").document(userId).collection("items").document(itemId)
            .delete()
            .addOnSuccessListener { loadCartItems() }
    }

    private fun updateSummary(count: Int) {
        tvItemCount.text = "$count items"
        tvSubtotal.text = "R ${String.format("%.0f", subtotalPrice)}"
        tvTotal.text = "R ${String.format("%.0f", subtotalPrice)}"
    }
}