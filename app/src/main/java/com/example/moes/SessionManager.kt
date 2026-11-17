package com.example.moes

import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Global Session Manager & Security Guard for Mo Piercings & Co.
 * Manages user login states, role verification, and access protection.
 */
object SessionManager {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    /**
     * Checks user role in Firestore and redirects to appropriate Activity:
     * ADMIN -> AdminMain
     * CUSTOMER -> MainActivity
     */
    fun verifyRoleAndRedirect(context: Context, onComplete: ((String) -> Unit)? = null) {
        val uid = getCurrentUserId() ?: run {
            context.startActivity(Intent(context, LoginScreen::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")?.trim()?.uppercase() ?: "CUSTOMER"
                onComplete?.invoke(role)

                if (role == "ADMIN") {
                    context.startActivity(Intent(context, AdminMain::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                } else {
                    context.startActivity(Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
            }
            .addOnFailureListener {
                context.startActivity(Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
    }

    /**
     * Enforces Admin security. If a CUSTOMER tries to access an Admin Activity,
     * redirects them back to MainActivity.
     */
    fun enforceAdminAccess(context: Context, onVerified: () -> Unit) {
        val uid = getCurrentUserId() ?: run {
            context.startActivity(Intent(context, LoginScreen::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")?.trim()?.uppercase() ?: "CUSTOMER"
                if (role == "ADMIN") {
                    onVerified()
                } else {
                    context.startActivity(Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
            }
            .addOnFailureListener {
                context.startActivity(Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
    }

    /**
     * Logs out current user and returns to LoginScreen.
     */
    fun logout(context: Context) {
        auth.signOut()
        context.startActivity(Intent(context, LoginScreen::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}
