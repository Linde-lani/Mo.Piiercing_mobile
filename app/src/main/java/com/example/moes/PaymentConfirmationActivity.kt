package com.example.moes

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moes.databinding.ActivityPaymentConfirmationBinding

/**
 * Payment Confirmation activity displayed after successful checkout completion.
 */
class PaymentConfirmationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPaymentConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val invoiceNumber = intent.getStringExtra("INVOICE_NUMBER") ?: "INV-${(100000..999999).random()}"
        val totalPaid = intent.getDoubleExtra("TOTAL_PAID", 0.0)

        binding.tvInvoiceNumber.text = invoiceNumber
        binding.tvTotalPaid.text = "R ${"%.2f".format(totalPaid)}"

        binding.btnReturnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
