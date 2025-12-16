package com.example.womenssafetyapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences
    private val encryptedPreferences: EncryptedSharedPreferences

    init {
        // Regular preferences for non-sensitive data
        sharedPreferences = context.getSharedPreferences("women_safety_prefs", Context.MODE_PRIVATE)

        // Encrypted preferences for sensitive data
        // Cast to EncryptedSharedPreferences
        encryptedPreferences = EncryptionUtils.getEncryptedSharedPreferences(context) as EncryptedSharedPreferences
    }

    // Emergency Code
    fun saveEmergencyCode(code: String) {
        encryptedPreferences.edit()
            .putString("emergency_code", code)
            .apply()
    }

    fun getEmergencyCode(): String? {
        return encryptedPreferences.getString("emergency_code", null)
    }

    // User Email
    fun saveUserEmail(email: String) {
        encryptedPreferences.edit()
            .putString("user_email", email)
            .apply()
    }

    fun getUserEmail(): String? {
        return encryptedPreferences.getString("user_email", null)
    }

    // Emergency Status
    fun setEmergencyActive(isActive: Boolean) {
        sharedPreferences.edit().putBoolean("emergency_active", isActive).apply()
    }

    fun isEmergencyActive(): Boolean {
        return sharedPreferences.getBoolean("emergency_active", false)
    }

    // First Launch
    fun setFirstLaunch(isFirst: Boolean) {
        sharedPreferences.edit()
            .putBoolean("first_launch", isFirst)
            .apply()
    }

    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean("first_launch", true)
    }

    // Location Tracking Enabled
    fun setLocationTrackingEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean("location_tracking", enabled)
            .apply()
    }

    fun isLocationTrackingEnabled(): Boolean {
        return sharedPreferences.getBoolean("location_tracking", true)
    }

    // Clear all data (logout)
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
        encryptedPreferences.edit().clear().apply()
    }
}