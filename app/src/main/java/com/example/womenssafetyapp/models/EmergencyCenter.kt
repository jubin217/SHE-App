package com.example.womenssafetyapp.models

data class EmergencyCenter(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val phone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    var distance: Double = 0.0
)