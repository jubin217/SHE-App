package com.example.womenssafetyapp.features


import android.telephony.SmsManager
import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.womenssafetyapp.R
import com.example.womenssafetyapp.dashboard.MainDashboardActivity
import com.example.womenssafetyapp.models.LocationData
import com.example.womenssafetyapp.utils.EncryptionUtils
import com.example.womenssafetyapp.utils.SharedPreferencesManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.womenssafetyapp.models.EmergencyContact
class LocationTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var sharedPrefs: SharedPreferencesManager
    private lateinit var firestore: FirebaseFirestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate() {
        super.onCreate()
        sharedPrefs = SharedPreferencesManager(this)
        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createLocationRequest()
        createLocationCallback()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 30000 // 30 seconds
            fastestInterval = 15000 // 15 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    handleNewLocation(location)
                }
            }
        }
    }

    private fun handleNewLocation(location: Location) {
        val userId = auth.currentUser?.uid ?: return

        // Encrypt location data
        val encryptedLat = EncryptionUtils.encryptData(location.latitude.toString())
        val encryptedLng = EncryptionUtils.encryptData(location.longitude.toString())

        val locationData = LocationData(
            latitude = encryptedLat,
            longitude = encryptedLng,
            timestamp = System.currentTimeMillis(),
            accuracy = location.accuracy
        )

        // Save to Firestore
        firestore.collection("users").document(userId)
            .collection("location_history")
            .add(locationData)
            .addOnSuccessListener {
                Log.d("LocationService", "Location saved: ${location.latitude}, ${location.longitude}")
            }
            .addOnFailureListener { e ->
                Log.e("LocationService", "Failed to save location: ${e.message}")
            }

        // Share with emergency contacts if emergency is active
        if (sharedPrefs.isEmergencyActive()) {
            shareLocationWithContacts(location)
        }
    }

    private fun shareLocationWithContacts(location: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            val userId = auth.currentUser?.uid ?: return@launch

            firestore.collection("users").document(userId)
                .collection("emergency_contacts")
                .get()
                .addOnSuccessListener { snapshot ->
                    snapshot.documents.forEach { document ->
                        val contact = document.toObject(EmergencyContact::class.java)
                        if (contact != null) {
                            sendLocationSms(contact.phone, location, contact.name)
                        }
                    }
                }
        }
    }

    private fun sendLocationSms(phoneNumber: String, location: Location, contactName: String = "") {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("LocationService", "No SMS permission")
            return
        }

        val message = "EMERGENCY! User location: https://maps.google.com/?q=${location.latitude},${location.longitude}"

        try {
            // Make sure you're using SmsManager.getDefault()
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d("LocationService", "Location SMS sent to $contactName: $phoneNumber")
        } catch (e: Exception) {
            Log.e("LocationService", "Failed to send SMS: ${e.message}")
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Tracks your location for safety"

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainDashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SHE - Safety App")
            .setContentText("Tracking your location for safety")
            .setSmallIcon(R.drawable.ic_safety)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val NOTIFICATION_ID = 101
    }
}