package com.example.loginandsignup

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.loginandsignup.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class SignUpActivity : AppCompatActivity() {
    private val binding: ActivitySignUpBinding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize FirebaseAuth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set click listener for the sign-in button
        binding.signinbutton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.registerButton.setOnClickListener {
            // Get values from EditText fields
            val email = binding.Email.text.toString().trim()
            val phone = binding.phoneno.text.toString().trim()
            val username = binding.username.text.toString().trim()
            val password = binding.password.text.toString()
            val repassword = binding.repeatpassword.text.toString()

            // Validate fields
            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || repassword.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            } else if (password != repassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            } else if (!isValidPhoneNumber(phone)) {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            } else {
                // If all validations pass, proceed with registration
                registerUser(email, password, username, phone)
            }
        }
    }

    // Function to validate phone number
    private fun isValidPhoneNumber(phone: String): Boolean {
        return phone.length == 10 && phone.all { it.isDigit() }
    }

    private fun registerUser(email: String, password: String, username: String, phone: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Get the user ID
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Create user data
                        val user = hashMapOf(
                            "userId" to userId,
                            "username" to username,
                            "email" to email,
                            "phone" to phone,
                            "createdAt" to Date(),
                            "accountType" to "Standard",
                            "isEmailVerified" to false
                        )

                        // Store user data in Firestore
                        db.collection("users")
                            .document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                // Send email verification
                                auth.currentUser?.sendEmailVerification()
                                    ?.addOnCompleteListener { verificationTask ->
                                        if (verificationTask.isSuccessful) {
                                            Toast.makeText(this, "Registration successful! Please check your email for verification.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                
                                // Navigate to login screen
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to store user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Registration failed
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

// Data class to represent a user
data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val createdAt: Date = Date(),
    val accountType: String = "Standard",
    val isEmailVerified: Boolean = false
)