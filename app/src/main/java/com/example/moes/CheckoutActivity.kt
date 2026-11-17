package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * CheckoutActivity handles customer order checkout, billing address validation,
 * payment card processing, Firestore order creation, and cart clearance.
 */
class CheckoutActivity : AppCompatActivity() {

    // Firebase Authentication & Firestore instances
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_checkout)

        // Initialize Firebase services
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Configure edge-to-edge window insets padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve total purchase price passed from PurchaseScreen/cart
        val totalPrice = intent.getDoubleExtra("TOTAL_PRICE", 0.0)

        // UI Element references
        val edtBillingAddress = findViewById<EditText>(R.id.edtBillingAddress)
        val edtCardNumber = findViewById<EditText>(R.id.edtCardNumber)
        val edtExpiry = findViewById<EditText>(R.id.edtExpiry)
        val edtCvv = findViewById<EditText>(R.id.edtCvv)
        val btnCompletePayment = findViewById<Button>(R.id.btnCompletePayment)

        // Handle Complete Payment button click
        btnCompletePayment.setOnClickListener {
            val address = edtBillingAddress.text.toString().trim()
            val cardNumber = edtCardNumber.text.toString().trim()
            val expiry = edtExpiry.text.toString().trim()
            val cvv = edtCvv.text.toString().trim()

            // Input Validation: Check for empty required billing fields
            if (address.isEmpty() || cardNumber.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
                Toast.makeText(this, "Please fill in all billing and card details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Input Validation: Ensure card number meets 16-digit length requirement
            if (cardNumber.length < 16) {
                Toast.makeText(this, "Please enter a valid 16-digit card number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Retrieve current user details
            val userEmail = auth.currentUser?.email ?: "guest@mopiercings.com"
            val userId = auth.currentUser?.uid ?: ""
            // Generate a unique 6-digit invoice reference ID
            val invoiceNumber = "INV-${(100000..999999).random()}"

            // Construct order object map for Firestore persistence
            val orderData = hashMapOf(
                "invoiceNumber" to invoiceNumber,
                "userId" to userId,
                "userEmail" to userEmail,
                "billingAddress" to address,
                "totalAmount" to totalPrice,
                "status" to "COMPLETED",
                "createdAt" to System.currentTimeMillis()
            )

            // Save order document to Firestore 'orders' collection
            db.collection("orders").add(orderData)
                .addOnSuccessListener {
                    // On successful order creation, clear items from the user's cart
                    if (userId.isNotEmpty()) {
                        db.collection("users").document(userId).collection("cart").get()
                            .addOnSuccessListener { docs ->
                                for (doc in docs) {
                                    doc.reference.delete()
                                }
                            }
                    }

                    Toast.makeText(this, "Payment successful! Order placed.", Toast.LENGTH_SHORT).show()

                    // Redirect to Payment Confirmation Screen with Invoice Details
                    val intent = Intent(this, PaymentConfirmationActivity::class.java).apply {
                        putExtra("INVOICE_NUMBER", invoiceNumber)
                        putExtra("TOTAL_PAID", totalPrice)
                    }
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Order Creation Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
