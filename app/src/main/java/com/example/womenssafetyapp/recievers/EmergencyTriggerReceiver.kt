package com.example.womenssafetyapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class EmergencyTriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "STOP_EMERGENCY" -> {
                // Stop emergency service
                Toast.makeText(context, "Emergency stopped", Toast.LENGTH_SHORT).show()
            }
            "TEST_EMERGENCY" -> {
                // Test emergency
                Toast.makeText(context, "Test emergency triggered", Toast.LENGTH_SHORT).show()
            }
        }
    }
}