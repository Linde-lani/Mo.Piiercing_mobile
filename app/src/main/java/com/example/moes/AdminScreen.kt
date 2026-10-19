package com.example.moes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminScreen : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var etProductName: TextInputEditText
    private lateinit var etProductDesc: TextInputEditText
    private lateinit var etProductPrice: TextInputEditText
    private lateinit var btnAddProduct: Button
    private lateinit var rvPendingBookings: RecyclerView
    private lateinit var adapter: BookingAdapter
    private lateinit var tvNoBookings: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_screen)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        
        initViews()
        setupBookingList()
        
        btnAddProduct.setOnClickListener {
            addNewProduct()
        }
    }

    private fun initViews() {
        etProductName = findViewById(R.id.etAdminProductName)
        etProductDesc = findViewById(R.id.etAdminProductDesc)
        etProductPrice = findViewById(R.id.etAdminProductPrice)
        btnAddProduct = findViewById(R.id.btnAddProduct)
        rvPendingBookings = findViewById(R.id.rvPendingBookings)
        tvNoBookings = findViewById(R.id.tvNoBookings)
    }

    private fun addNewProduct() {
        val name = etProductName.text.toString()
        val desc = etProductDesc.text.toString()
        val priceStr = etProductPrice.text.toString()

        if (name.isBlank() || priceStr.isBlank()) {
            Toast.makeText(this, "Please enter name and price", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show()
            return
        }

        val product = hashMapOf(
            "name" to name,
            "description" to desc,
            "price" to price,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("gallery_products").add(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Service added to gallery!", Toast.LENGTH_SHORT).show()
                etProductName.text?.clear()
                etProductDesc.text?.clear()
                etProductPrice.text?.clear()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBookingList() {
        rvPendingBookings.layoutManager = LinearLayoutManager(this)
        adapter = BookingAdapter { bookingId -> approveBooking(bookingId) }
        rvPendingBookings.adapter = adapter
        
        db.collection("bookings")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error loading bookings", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                
                val bookings = snapshots?.documents?.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) {
                        BookingItem(
                            id = doc.id,
                            name = data["name"] as? String ?: "Unknown",
                            service = data["service"] as? String ?: "No Service",
                            date = data["date"] as? String ?: "",
                            time = data["time"] as? String ?: "",
                            status = data["status"] as? String ?: "Pending"
                        )
                    } else null
                } ?: emptyList()
                
                tvNoBookings.visibility = if (bookings.isEmpty()) View.VISIBLE else View.GONE
                adapter.submitList(bookings)
            }
    }

    private fun approveBooking(bookingId: String) {
        db.collection("bookings").document(bookingId)
            .update("status", "Approved")
            .addOnSuccessListener {
                Toast.makeText(this, "Booking confirmed and approved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Approval failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    data class BookingItem(
        val id: String,
        val name: String,
        val service: String,
        val date: String,
        val time: String,
        val status: String
    )

    class BookingAdapter(private val onApproveClick: (String) -> Unit) : RecyclerView.Adapter<BookingAdapter.ViewHolder>() {
        private var items = listOf<BookingItem>()

        fun submitList(newList: List<BookingItem>) {
            items = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_booking, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvName.text = item.name
            holder.tvService.text = "Service: ${item.service}"
            holder.tvDateTime.text = "Date: ${item.date} | Time: ${item.time}"
            holder.tvStatus.text = "Status: ${item.status}"
            
            if (item.status == "Approved") {
                holder.btnConfirm.visibility = View.GONE
            } else {
                holder.btnConfirm.visibility = View.VISIBLE
                holder.btnConfirm.setOnClickListener { onApproveClick(item.id) }
            }
        }

        override fun getItemCount() = items.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvAdminBookingName)
            val tvService: TextView = view.findViewById(R.id.tvAdminBookingService)
            val tvDateTime: TextView = view.findViewById(R.id.tvAdminBookingDateTime)
            val tvStatus: TextView = view.findViewById(R.id.tvAdminBookingStatus)
            val btnConfirm: Button = view.findViewById(R.id.btnConfirmBookingAdmin)
        }
    }
}
