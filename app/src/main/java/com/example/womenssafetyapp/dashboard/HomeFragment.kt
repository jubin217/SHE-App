package com.example.womenssafetyapp.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.womenssafetyapp.R
import com.example.womenssafetyapp.databinding.FragmentHomeBinding
import com.example.womenssafetyapp.features.EmergencyAlertService
import com.example.womenssafetyapp.utils.SharedPreferencesManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefs: SharedPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefs = SharedPreferencesManager(requireContext())

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        val emergencyCode = sharedPrefs.getEmergencyCode()
        binding.tvEmergencyCode.text = emergencyCode ?: "Not set"
    }

    private fun setupClickListeners() {
        binding.btnTestEmergency.setOnClickListener {
            showTestEmergencyDialog()
        }

        binding.btnShareLocation.setOnClickListener {
            shareLiveLocation()
        }

        binding.btnSafeRoute.setOnClickListener {
            showSafeRoutes()
        }
    }

    private fun showTestEmergencyDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Test Emergency Alert")
            .setMessage("This will send a test alert to your emergency contacts. Are you sure?")
            .setPositiveButton("Test") { dialog, _ ->
                dialog.dismiss()
                triggerTestEmergency()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun triggerTestEmergency() {
        // Simplified version - start service directly
        val intent = Intent(requireContext(), EmergencyAlertService::class.java)
        intent.putExtra("is_test", true)

        // For Android O and above, use startForegroundService
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
        }
    }

    private fun shareLiveLocation() {
        // Start sharing live location with emergency contacts
        android.widget.Toast.makeText(requireContext(), "Live location sharing coming soon", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun showSafeRoutes() {
        // Show safe routes based on AI analysis
        android.widget.Toast.makeText(requireContext(), "Safe routes feature coming soon", android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}