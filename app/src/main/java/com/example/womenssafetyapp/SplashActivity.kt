package com.example.womenssafetyapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.womenssafetyapp.auth.LoginActivity
import com.example.womenssafetyapp.dashboard.MainDashboardActivity
import com.example.womenssafetyapp.utils.SharedPreferencesManager
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthStatus()
        }, 2000)
    }

    private fun checkAuthStatus() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val sharedPrefs = SharedPreferencesManager(this)

        if (currentUser != null && sharedPrefs.getEmergencyCode() != null) {
            startActivity(
                Intent(this, MainDashboardActivity::class.java)
            )
        } else {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
        }
        finish()
    }
}
