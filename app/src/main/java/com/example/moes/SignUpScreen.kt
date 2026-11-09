package com.example.moes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
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
                        saveUserToFirestore(userId, userName, userEmail, role, false)
                    }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun signUpWithGoogle(view: View) {
        val selectedRoleId = rgRole.checkedRadioButtonId
        val role = if (selectedRoleId == R.id.rbAdmin) "Admin" else "User"

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(this)

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@SignUpScreen
                )
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    firebaseAuthWithGoogleForSignup(googleIdTokenCredential.idToken, role)
                } else {
                    Log.w("SignUpScreen", "Credential is not of type Google ID!")
                    Toast.makeText(this@SignUpScreen, "Google Sign-Up failed: Invalid credential type", Toast.LENGTH_SHORT).show()
                }
            } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                Log.i("SignUpScreen", "Google Sign-Up cancelled by user.")
            } catch (e: GetCredentialException) {
                Log.e("SignUpScreen", "GetCredentialException: ${e.type} - ${e.message}", e)
                Toast.makeText(this@SignUpScreen, "Google Sign-Up failed. Verify SHA-1 fingerprint in Firebase Console.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("SignUpScreen", "Exception: ${e.message}", e)
                Toast.makeText(this@SignUpScreen, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogleForSignup(idToken: String, role: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserToFirestore(user.uid, user.displayName ?: "User", user.email ?: "", role, true)
                    }
                } else {
                    Toast.makeText(this, "Firebase Auth Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun saveUserToFirestore(userId: String, name: String, email: String, role: String, isGoogleSignIn: Boolean) {
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
                if (isGoogleSignIn) {
                    if (role.equals("admin", ignoreCase = true)) {
                        startActivity(Intent(this, AdminScreen::class.java))
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                } else {
                    auth.currentUser?.sendEmailVerification()
                    startActivity(Intent(this, VerifyEmailScreen::class.java))
                }
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
