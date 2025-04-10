package com.example.loginandsignup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.loginandsignup.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private val binding: ActivityProfileBinding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Get current user
        val user = auth.currentUser
        if (user != null) {
            loadUserDataFromFirestore(user)
        } else {
            // If no user is logged in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Set click listener for logout button
        binding.logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadUserDataFromFirestore(user: FirebaseUser) {
        val userRef = db.collection("users").document(user.uid)
        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Get user data
                    val username = document.getString("username")
                    val email = document.getString("email")
                    val phone = document.getString("phone")
                    val createdAt = document.getDate("createdAt")
                    val accountType = document.getString("accountType")
                    val isEmailVerified = document.getBoolean("isEmailVerified") ?: false

                    // Update UI with user data
                    binding.userName.text = username
                    binding.userEmail.text = email
                    binding.phoneNumber.text = "Phone: ${phone ?: "Not provided"}"

                    // Set join date
                    val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                    val joinDate = dateFormat.format(createdAt ?: Date())
                    binding.joinDate.text = "Member since: $joinDate"

                    // Set account type
                    val displayAccountType = if (isEmailVerified) "Verified" else accountType ?: "Standard"
                    binding.accountType.text = "Account Type: $displayAccountType"
                } else {
                    Toast.makeText(this, "No user data found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logoutUser() {
        auth.signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
} 