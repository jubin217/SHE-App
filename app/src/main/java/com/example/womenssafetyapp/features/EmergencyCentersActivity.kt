package com.example.womenssafetyapp.features

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.womenssafetyapp.R
import com.example.womenssafetyapp.adapters.EmergencyCenterAdapter
import com.example.womenssafetyapp.databinding.ActivityEmergencyCentersBinding
import com.example.womenssafetyapp.models.EmergencyCenter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class EmergencyCentersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyCentersBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var adapter: EmergencyCenterAdapter
    private val emergencyCenters = mutableListOf<EmergencyCenter>()
    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyCentersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupRecyclerView()
        getCurrentLocation()
        loadEmergencyCenters()

        binding.btnRefresh.setOnClickListener {
            getCurrentLocation()
        }
    }

    private fun setupRecyclerView() {
        adapter = EmergencyCenterAdapter(emergencyCenters) { center, action ->
            when (action) {
                "call" -> callEmergencyCenter(center.phone)
                "navigate" -> navigateToCenter(center.latitude, center.longitude)
            }
        }

        binding.rvCenters.layoutManager = LinearLayoutManager(this)
        binding.rvCenters.adapter = adapter
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                currentLocation = location
                if (location != null) {
                    binding.tvCurrentLocation.text =
                        "Your location: ${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}"
                    sortCentersByDistance()
                } else {
                    binding.tvCurrentLocation.text = "Location not available"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadEmergencyCenters() {
        // In production, this would come from a database or API
        // For now, using sample data

        emergencyCenters.clear()
        emergencyCenters.addAll(getSampleEmergencyCenters())
        adapter.notifyDataSetChanged()
    }

    private fun getSampleEmergencyCenters(): List<EmergencyCenter> {
        return listOf(
            EmergencyCenter(
                id = "1",
                name = "City Police Station",
                type = "Police",
                phone = "100",
                latitude = 12.9716,
                longitude = 77.5946,
                address = "MG Road, Bangalore",
                distance = 0.0
            ),
            EmergencyCenter(
                id = "2",
                name = "Women Help Desk",
                type = "Support",
                phone = "1091",
                latitude = 12.9758,
                longitude = 77.5991,
                address = "Commercial Street, Bangalore",
                distance = 0.0
            ),
            EmergencyCenter(
                id = "3",
                name = "Emergency Medical Services",
                type = "Medical",
                phone = "108",
                latitude = 12.9675,
                longitude = 77.5875,
                address = "Residency Road, Bangalore",
                distance = 0.0
            ),
            EmergencyCenter(
                id = "4",
                name = "Women Safety Cell",
                type = "Support",
                phone = "181",
                latitude = 12.9791,
                longitude = 77.5918,
                address = "Cunningham Road, Bangalore",
                distance = 0.0
            ),
            EmergencyCenter(
                id = "5",
                name = "Nearest Hospital",
                type = "Medical",
                phone = "102",
                latitude = 12.9639,
                longitude = 77.5937,
                address = "St. Marks Road, Bangalore",
                distance = 0.0
            )
        )
    }

    private fun sortCentersByDistance() {
        currentLocation?.let { userLocation ->
            emergencyCenters.forEach { center ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    userLocation.latitude,
                    userLocation.longitude,
                    center.latitude,
                    center.longitude,
                    results
                )
                center.distance = results[0] / 1000.0 // Convert to kilometers
            }

            emergencyCenters.sortBy { it.distance }
            adapter.notifyDataSetChanged()
        }
    }

    private fun callEmergencyCenter(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$phoneNumber")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(intent)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                101
            )
        }
    }

    private fun navigateToCenter(latitude: Double, longitude: Double) {
        val uri = "google.navigation:q=$latitude,$longitude"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show()
        }
    }
}