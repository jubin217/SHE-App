package com.example.womenssafetyapp.models

data class EmergencyContact(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val relationship: String = "",
    val addedAt: Long = System.currentTimeMillis()
)