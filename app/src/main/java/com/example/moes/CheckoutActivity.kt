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

class CheckoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_checkout)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val edtBillingAddress = findViewById<EditText>(R.id.edtBillingAddress)
        val edtCardNumber = findViewById<EditText>(R.id.edtCardNumber)
        val edtExpiry = findViewById<EditText>(R.id.edtExpiry)
        val edtCvv = findViewById<EditText>(R.id.edtCvv)
        val btnCompletePayment = findViewById<Button>(R.id.btnCompletePayment)

        btnCompletePayment.setOnClickListener {
            val address = edtBillingAddress.text.toString().trim()
            val cardNumber = edtCardNumber.text.toString().trim()
            val expiry = edtExpiry.text.toString().trim()
            val cvv = edtCvv.text.toString().trim()

            if (address.isEmpty() || cardNumber.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
                Toast.makeText(this, "Please fill in all billing and card details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cardNumber.length < 16) {
                Toast.makeText(this, "Please enter a valid 16-digit card number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Payment successful! Booking confirmed.", Toast.LENGTH_LONG).show()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
