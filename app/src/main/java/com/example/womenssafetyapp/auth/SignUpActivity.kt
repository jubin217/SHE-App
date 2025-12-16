package com.example.womenssafetyapp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.womenssafetyapp.databinding.ActivitySignupBinding
import com.example.womenssafetyapp.dashboard.MainDashboardActivity
import com.example.womenssafetyapp.models.User
import com.example.womenssafetyapp.utils.EncryptionUtils
import com.example.womenssafetyapp.utils.SharedPreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPrefs: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        sharedPrefs = SharedPreferencesManager(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInput(name, email, phone, password, confirmPassword)) {
                signUpUser(name, email, phone, password)
            }
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (name.isEmpty()) {
            binding.etName.error = "Name required"
            return false
        }
        if (email.isEmpty()) {
            binding.etEmail.error = "Email required"
            return false
        }
        if (phone.isEmpty() || phone.length != 10) {
            binding.etPhone.error = "Valid phone number required"
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
        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords don't match"
            return false
        }
        return true
    }

    private fun signUpUser(name: String, email: String, phone: String, password: String) {
        binding.progressBar.visibility = android.view.View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid ?: ""

                    // Update profile with name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                // Generate emergency code
                                val emergencyCode = (100..999).random().toString()

                                // Create user data with encryption
                                val encryptedPhone = EncryptionUtils.encryptData(phone)
                                val encryptedEmail = EncryptionUtils.encryptData(email)

                                val userData = User(
                                    userId = userId,
                                    name = name,
                                    email = encryptedEmail,
                                    phone = encryptedPhone,
                                    emergencyCode = emergencyCode,
                                    isActive = true,
                                    createdAt = System.currentTimeMillis(),
                                    emergencyContacts = mutableListOf()
                                )

                                // Save to Firestore
                                firestore.collection("users").document(userId)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        // Save emergency code locally
                                        sharedPrefs.saveEmergencyCode(emergencyCode)
                                        sharedPrefs.saveUserEmail(email)

                                        // Show emergency code to user
                                        showEmergencyCodeDialog(emergencyCode)

                                        binding.progressBar.visibility = android.view.View.GONE
                                    }
                                    .addOnFailureListener { e ->
                                        binding.progressBar.visibility = android.view.View.GONE
                                        Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                } else {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showEmergencyCodeDialog(emergencyCode: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Your Emergency Code")
            .setMessage("Your emergency code is: $emergencyCode\n\nThis code will be used to trigger emergency alerts. Keep it safe and don't share it with anyone.")
            .setPositiveButton("I've Saved It") { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(this, MainDashboardActivity::class.java))
                finish()
            }
            .setCancelable(false)
            .show()
    }
}