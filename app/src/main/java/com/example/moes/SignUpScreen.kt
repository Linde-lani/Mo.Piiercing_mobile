package com.example.moes

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpScreen : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var signupButton: Button
    private lateinit var signupRedirectText: TextView
    private lateinit var rgRole: RadioGroup
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up_screen)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        username = findViewById(R.id.edtUsernameSignup)
        email = findViewById(R.id.edtEmailSignup)
        password = findViewById(R.id.edtPasswordSignup)
        confirmPassword = findViewById(R.id.edtConfirmPasswordSignup)
        signupButton = findViewById(R.id.btnRegister)
        signupRedirectText = findViewById(R.id.txtSignUpRedirect)
        rgRole = findViewById(R.id.rgRole)

        signupButton.setOnClickListener {
            registerUser()
        }

        signupRedirectText.setOnClickListener {
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun registerUser() {
        val userName = username.text.toString().trim()
        val userEmail = email.text.toString().trim()
        val userPassword = password.text.toString().trim()
        val confirm = confirmPassword.text.toString().trim()
        
        val selectedRoleId = rgRole.checkedRadioButtonId
        val role = if (selectedRoleId == R.id.rbAdmin) "Admin" else "User"

        if (userName.isEmpty()) {
            username.error = "Enter your username"
            return
        }
        if (userEmail.isEmpty()) {
            email.error = "Enter your email"
            return
        }
        if (userPassword.length < 6) {
            password.error = "Password must be at least 6 characters"
            return
        }
        if (userPassword != confirm) {
            confirmPassword.error = "Passwords do not match"
            return
        }

        auth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        saveUserToFirestore(userId, userName, userEmail, role)
                    }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun saveUserToFirestore(userId: String, name: String, email: String, role: String) {
        val userMap = hashMapOf(
            "username" to name,
            "email" to email,
            "userId" to userId,
            "role" to role,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(userId)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Account created as $role", Toast.LENGTH_SHORT).show()
                auth.currentUser?.sendEmailVerification()
                startActivity(Intent(this, VerifyEmailScreen::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}