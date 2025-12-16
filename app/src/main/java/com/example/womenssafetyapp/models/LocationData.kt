package com.example.womenssafetyapp.models

data class LocationData(
    val latitude: String = "",  // Encrypted
    val longitude: String = "", // Encrypted
    val timestamp: Long = System.currentTimeMillis(),
    val accuracy: Float = 0f
)