package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

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
            fetchRoleAndRedirect(currentUser.uid)
        }
    }

    fun loginRedirect(view: View) {
        val intent = Intent(this, SignUpScreen::class.java)
        startActivity(intent)
        finish()
    }

    fun signInWithGoogle(view: View) {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(this)
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginScreen
                )
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                } else {
                    progressBar.visibility = View.GONE
                    Log.w("LoginScreen", "Credential is not of type Google ID!")
                    Toast.makeText(this@LoginScreen, "Google Sign-In failed: Invalid credential type", Toast.LENGTH_SHORT).show()
                }
            } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                progressBar.visibility = View.GONE
                Log.i("LoginScreen", "Google Sign-In cancelled by user.")
            } catch (e: GetCredentialException) {
                progressBar.visibility = View.GONE
                Log.e("LoginScreen", "GetCredentialException: ${e.type} - ${e.message}", e)
                Toast.makeText(this@LoginScreen, "Google Sign-In failed. Verify SHA-1 fingerprint in Firebase Console.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e("LoginScreen", "Exception: ${e.message}", e)
                Toast.makeText(this@LoginScreen, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        checkAndSaveUserToFirestore(user.uid, user.displayName ?: "User", user.email ?: "")
                    }
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Firebase Auth Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkAndSaveUserToFirestore(userId: String, name: String, email: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    fetchRoleAndRedirect(userId)
                } else {
                    val userMap = hashMapOf(
                        "username" to name,
                        "email" to email,
                        "userId" to userId,
                        "role" to "user",
                        "createdAt" to System.currentTimeMillis()
                    )
                    db.collection("users").document(userId).set(userMap)
                        .addOnSuccessListener {
                            fetchRoleAndRedirect(userId)
                        }
                        .addOnFailureListener { e ->
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

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

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: return@addOnSuccessListener
                fetchRoleAndRedirect(userId)
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
