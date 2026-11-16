package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moes.databinding.ActivityCustomerProfilesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Admin Activity for searching and managing customer profiles.
 */
class customer_profiles : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerProfilesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var adapter: CustomerAdapter
    private val allCustomers = mutableListOf<Customer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Admin Security Check
        SessionManager.enforceAdminAccess(this) {
            enableEdgeToEdge()
            binding = ActivityCustomerProfilesBinding.inflate(layoutInflater)
            setContentView(binding.root)

            ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            setupRecyclerView()
            setupSearchBar()
            loadCustomerProfiles()
        }
    }

    private fun setupRecyclerView() {
        adapter = CustomerAdapter { customer ->
            Toast.makeText(this, "Customer: ${customer.customerName}\nEmail: ${customer.email}", Toast.LENGTH_LONG).show()
        }
        binding.rvCustomers.layoutManager = LinearLayoutManager(this)
        binding.rvCustomers.adapter = adapter
    }

    private fun setupSearchBar() {
        binding.etSearchCustomer.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCustomers(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadCustomerProfiles() {
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                allCustomers.clear()
                for (doc in documents) {
                    val name = doc.getString("username") ?: doc.getString("fullName") ?: "Client"
                    val email = doc.getString("email") ?: ""
                    val phone = doc.getString("phoneNumber") ?: doc.getString("phone") ?: ""

                    allCustomers.add(
                        Customer(
                            customerId = doc.id,
                            customerName = name,
                            email = email,
                            phone = phone
                        )
                    )
                }

                filterCustomers(binding.etSearchCustomer.text.toString().trim())
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load customers: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterCustomers(query: String) {
        val filtered = if (query.isEmpty()) {
            allCustomers
        } else {
            allCustomers.filter {
                it.customerName.contains(query, ignoreCase = true) ||
                        it.email.contains(query, ignoreCase = true)
            }
        }

        adapter.submitList(filtered)
        if (filtered.isEmpty()) {
            binding.layoutEmptyCustomers.visibility = View.VISIBLE
            binding.rvCustomers.visibility = View.GONE
        } else {
            binding.layoutEmptyCustomers.visibility = View.GONE
            binding.rvCustomers.visibility = View.VISIBLE
        }
    }
}
