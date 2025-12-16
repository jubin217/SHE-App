package com.example.womenssafetyapp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.womenssafetyapp.R
import com.example.womenssafetyapp.databinding.ActivityLoginBinding
import com.example.womenssafetyapp.dashboard.MainDashboardActivity
import com.example.womenssafetyapp.utils.SharedPreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPrefs: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        sharedPrefs = SharedPreferencesManager(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = "Email required"
            return false
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Password required"
            return false
        }
        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            return false
        }
        return true
    }

    private fun loginUser(email: String, password: String) {
        binding.progressBar.visibility = android.view.View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = android.view.View.GONE

                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    // Get emergency code from Firestore
                    firestore.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val emergencyCode = document.getString("emergencyCode")
                                if (emergencyCode != null) {
                                    sharedPrefs.saveEmergencyCode(emergencyCode)
                                    startActivity(Intent(this, MainDashboardActivity::class.java))
                                    finish()
                                } else {
                                    // Generate new emergency code
                                    generateEmergencyCode(userId)
                                }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun generateEmergencyCode(userId: String) {
        val emergencyCode = (100..999).random().toString()

        val userData = hashMapOf(
            "emergencyCode" to emergencyCode,
            "isActive" to true,
            "lastLogin" to System.currentTimeMillis()
        )

        firestore.collection("users").document(userId)
            .update(userData as Map<String, Any>)
            .addOnSuccessListener {
                sharedPrefs.saveEmergencyCode(emergencyCode)
                startActivity(Intent(this, MainDashboardActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to generate emergency code", Toast.LENGTH_SHORT).show()
            }
    }
}