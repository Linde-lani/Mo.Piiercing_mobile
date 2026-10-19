package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.jvm.java

class LoginScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        progressBar = findViewById(R.id.loginProgressBar)
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // If already logged in, we still verify the role to ensure correct redirection
            fetchRoleAndRedirect(currentUser.uid)
        }
    }

    fun loginRedirect(view: View) {
        val intent = Intent(this, SignUpScreen::class.java)
        startActivity(intent)
        finish()
    }

//    fun getSavedUser(view: View) {
//        val emailEditText = findViewById<EditText>(R.id.edtEmailLogin)
//        val passwordEditText = findViewById<EditText>(R.id.edtPasswordLogin)
//
//        val email = emailEditText.text.toString().trim()
//        val password = passwordEditText.text.toString().trim()
//
//        if (email.isEmpty()) {
//            emailEditText.error = "Enter your email"
//            return
//        }
//
//        if (password.isEmpty()) {
//            passwordEditText.error = "Enter your password"
//            return
//        }
//
//        // Show progress bar
//        progressBar.visibility = View.VISIBLE
//
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    val user = auth.currentUser
//                    if (user != null) {
//                        fetchRoleAndRedirect(user.uid)
//                    }
//                } else {
//                    progressBar.visibility = View.GONE
//                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }

    private fun fetchRoleAndRedirect(userId: String) {
        progressBar.visibility = View.VISIBLE
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                progressBar.visibility = View.GONE
                if (document != null && document.exists()) {
                    val role = document.getString("role")?.lowercase() ?: "user"
                    
                    if (role == "admin") {
                        Toast.makeText(this, "Welcome Admin", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, AdminScreen::class.java))
                    } else {
                        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    finish()
                } else {
                    // Default fallback if document doesn't exist
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error fetching profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun getSavedUser(view: View) {
        val email = findViewById<EditText>(R.id.edtEmailLogin).text.toString().trim()
        val password = findViewById<EditText>(R.id.edtPasswordLogin).text.toString().trim()
        val progressBar = findViewById<ProgressBar>(R.id.loginProgressBar)

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: return@addOnSuccessListener

                // Check Firestore for user role
                FirebaseFirestore.getInstance().collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        progressBar.visibility = View.GONE
                        if (document.exists()) {
                            val role = document.getString("role") ?: "user"

                            if (role == "admin") {
                                startActivity(Intent(this, AdminScreen::class.java))
                            } else {
                                startActivity(Intent(this, MainActivity::class.java))
                            }
                            finish()
                        }
                    }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
