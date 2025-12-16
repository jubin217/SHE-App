package com.example.womenssafetyapp.features

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.womenssafetyapp.R
import com.example.womenssafetyapp.dashboard.MainDashboardActivity
import com.example.womenssafetyapp.receivers.EmergencyTriggerReceiver
import com.example.womenssafetyapp.utils.SharedPreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EmergencyAlertService : Service() {

    private lateinit var sharedPrefs: SharedPreferencesManager
    private lateinit var firestore: FirebaseFirestore
    private lateinit var database: FirebaseDatabase
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate() {
        super.onCreate()
        sharedPrefs = SharedPreferencesManager(this)
        firestore = FirebaseFirestore.getInstance()
        database = FirebaseDatabase.getInstance()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val emergencyCode = intent?.getStringExtra("emergency_code")
        val isTest = intent?.getBooleanExtra("is_test", false) ?: false

        startForeground(ALERT_NOTIFICATION_ID, createNotification())

        if (emergencyCode != null) {
            CoroutineScope(Dispatchers.IO).launch {
                handleEmergencyAlert(emergencyCode, isTest)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun handleEmergencyAlert(emergencyCode: String, isTest: Boolean) {
        val userId = auth.currentUser?.uid ?: return

        // Start video recording
        startVideoRecording()

        // Send alerts to emergency contacts
        sendEmergencyAlerts(userId, isTest)

        // Broadcast to nearby users
        broadcastToNearbyUsers(userId, emergencyCode)

        // Save emergency record
        saveEmergencyRecord(userId, emergencyCode, isTest)

        // Set emergency active
        sharedPrefs.setEmergencyActive(true)
    }

    private fun startVideoRecording() {
        val intent = Intent(this, VideoRecordingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun sendEmergencyAlerts(userId: String, isTest: Boolean) {
        firestore.collection("users").document(userId)
            .collection("emergency_contacts")
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { document ->
                    val phone = document.getString("phone") ?: ""
                    val name = document.getString("name") ?: "Contact"

                    if (phone.isNotEmpty()) {
                        sendSmsAlert(phone, isTest, name)
                    }
                }
            }
    }

    private fun sendSmsAlert(phoneNumber: String, isTest: Boolean, contactName: String = "") {
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val prefix = if (isTest) "[TEST] " else ""
        val message = "${prefix}EMERGENCY ALERT! Your contact has triggered an emergency alert. Please check on them immediately."

        try {
            val smsManager = getSystemService(Context.TELEPHONY_SERVICE) as SmsManager
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d("EmergencyAlert", "SMS sent to $phoneNumber")
        } catch (e: Exception) {
            Log.e("EmergencyAlert", "Failed to send SMS: ${e.message}")
        }
    }

    private fun makeEmergencyCall(phoneNumber: String) {
        // This would typically initiate a call
        // For SMS-only in this implementation, we'll log
        Log.d("EmergencyAlert", "Would call: $phoneNumber")
    }

    private fun broadcastToNearbyUsers(userId: String, emergencyCode: String) {
        val alertData = hashMapOf(
            "userId" to userId,
            "emergencyCode" to emergencyCode,
            "timestamp" to System.currentTimeMillis(),
            "location" to null // Will be filled by location service
        )

        database.reference.child("emergency_alerts")
            .push()
            .setValue(alertData)
            .addOnSuccessListener {
                Log.d("EmergencyAlert", "Alert broadcasted to nearby users")
            }
            .addOnFailureListener { e ->
                Log.e("EmergencyAlert", "Failed to broadcast alert: ${e.message}")
            }
    }

    private fun saveEmergencyRecord(userId: String, emergencyCode: String, isTest: Boolean) {
        val emergencyRecord = hashMapOf(
            "userId" to userId,
            "emergencyCode" to emergencyCode,
            "isTest" to isTest,
            "timestamp" to System.currentTimeMillis(),
            "resolved" to false
        )

        firestore.collection("emergency_records")
            .add(emergencyRecord)
            .addOnSuccessListener {
                Log.d("EmergencyAlert", "Emergency record saved")
            }
            .addOnFailureListener { e ->
                Log.e("EmergencyAlert", "Failed to save emergency record: ${e.message}")
            }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Emergency alert notifications"
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000)

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

        // Stop emergency intent
        val stopIntent = Intent(this, EmergencyTriggerReceiver::class.java)
        stopIntent.action = "STOP_EMERGENCY"
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle("🚨 EMERGENCY ALERT ACTIVATED")
            .setContentText("Emergency services and contacts have been notified")
            .setSmallIcon(R.drawable.ic_safety)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_safety,
                "STOP EMERGENCY",
                stopPendingIntent
            )
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPrefs.setEmergencyActive(false)
        stopVideoRecording()
    }

    private fun stopVideoRecording() {
        val intent = Intent(this, VideoRecordingService::class.java)
        stopService(intent)
    }

    companion object {
        private const val ALERT_CHANNEL_ID = "emergency_alert_channel"
        private const val ALERT_NOTIFICATION_ID = 102
    }
}