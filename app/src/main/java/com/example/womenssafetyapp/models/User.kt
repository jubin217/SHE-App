package com.example.womenssafetyapp.models

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "", // Encrypted
    val phone: String = "", // Encrypted
    val emergencyCode: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val emergencyContacts: List<String> = emptyList()
)