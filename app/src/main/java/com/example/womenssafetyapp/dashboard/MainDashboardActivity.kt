package com.example.womenssafetyapp.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.womenssafetyapp.R
import com.example.womenssafetyapp.databinding.ActivityMainDashboardBinding
import com.example.womenssafetyapp.features.ChatBotActivity
import com.example.womenssafetyapp.features.EmergencyCentersActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainDashboardBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        setupClickListeners()
        checkAndRequestPermissions()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                    true
                }
                R.id.nav_contacts -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, EmergencyContactsFragment())
                        .commit()
                    true
                }
                R.id.nav_safety -> {
                    // Create a placeholder fragment for now
                    val fragment = object : androidx.fragment.app.Fragment() {
                        override fun onCreateView(
                            inflater: android.view.LayoutInflater,
                            container: android.view.ViewGroup?,
                            savedInstanceState: android.os.Bundle?
                        ): android.view.View? {
                            val view = android.view.View(context)
                            view.setBackgroundColor(android.graphics.Color.WHITE)
                            return view
                        }
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                    true
                }
                R.id.nav_profile -> {
                    // Create a placeholder fragment for now
                    val fragment = object : androidx.fragment.app.Fragment() {
                        override fun onCreateView(
                            inflater: android.view.LayoutInflater,
                            container: android.view.ViewGroup?,
                            savedInstanceState: android.os.Bundle?
                        ): android.view.View? {
                            val view = android.view.View(context)
                            view.setBackgroundColor(android.graphics.Color.WHITE)
                            return view
                        }
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                    true
                }
                else -> false
            }
        }

        // Load default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }

    private fun setupClickListeners() {
        binding.fabEmergency.setOnClickListener {
            triggerEmergencyAlert()
        }

        // Remove these if they don't exist in your layout
        // binding.btnChatBot.setOnClickListener { ... }
        // binding.btnEmergencyCenters.setOnClickListener { ... }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val newPermissions = permissions.plus(Manifest.permission.POST_NOTIFICATIONS)
            requestPermissions(newPermissions, 100)
        } else {
            requestPermissions(permissions, 100)
        }
    }

    private fun triggerEmergencyAlert() {
        android.widget.Toast.makeText(this, "Emergency alert triggered!", android.widget.Toast.LENGTH_SHORT).show()
        // We'll implement the actual emergency service later
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            // Handle permission results
        }
    }
}