package com.example.womenssafetyapp.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.womenssafetyapp.R
import com.example.womenssafetyapp.adapters.EmergencyContactsAdapter
import com.example.womenssafetyapp.databinding.FragmentEmergencyContactsBinding
import com.example.womenssafetyapp.models.EmergencyContact
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EmergencyContactsFragment : Fragment() {

    private lateinit var binding: FragmentEmergencyContactsBinding
    private lateinit var adapter: EmergencyContactsAdapter
    private lateinit var firestore: FirebaseFirestore
    private val auth = FirebaseAuth.getInstance()
    private val contactsList = mutableListOf<EmergencyContact>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEmergencyContactsBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadEmergencyContacts()

        binding.fabAddContact.setOnClickListener {
            showAddContactDialog()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = EmergencyContactsAdapter(contactsList) { contact, action ->
            when (action) {
                "delete" -> showDeleteDialog(contact)
                "call" -> callContact(contact)
                "message" -> messageContact(contact)
            }
        }

        binding.rvContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContacts.adapter = adapter
    }

    private fun loadEmergencyContacts() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("emergency_contacts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Failed to load contacts", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                contactsList.clear()
                snapshot?.documents?.forEach { document ->
                    val contact = document.toObject(EmergencyContact::class.java)
                    contact?.let { contactsList.add(it) }
                }

                adapter.notifyDataSetChanged()

                if (contactsList.isEmpty()) {
                    binding.tvNoContacts.visibility = View.VISIBLE
                } else {
                    binding.tvNoContacts.visibility = View.GONE
                }
            }
    }

    private fun showAddContactDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        val etName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName)
        val etPhone = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPhone)
        val etRelationship = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRelationship)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Emergency Contact")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val relationship = etRelationship.text.toString().trim()

                if (name.isNotEmpty() && phone.isNotEmpty() && phone.length == 10) {
                    addEmergencyContact(name, phone, relationship)
                } else {
                    Toast.makeText(requireContext(), "Please enter valid details", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addEmergencyContact(name: String, phone: String, relationship: String) {
        val userId = auth.currentUser?.uid ?: return

        val contactId = firestore.collection("users").document(userId)
            .collection("emergency_contacts").document().id

        val contact = EmergencyContact(
            id = contactId,
            name = name,
            phone = phone,
            relationship = relationship,
            addedAt = System.currentTimeMillis()
        )

        firestore.collection("users").document(userId)
            .collection("emergency_contacts")
            .document(contactId)
            .set(contact)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Contact added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to add contact", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteDialog(contact: EmergencyContact) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete ${contact.name}?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteContact(contact)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteContact(contact: EmergencyContact) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("emergency_contacts")
            .document(contact.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Contact deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete contact", Toast.LENGTH_SHORT).show()
            }
    }

    private fun callContact(contact: EmergencyContact) {
        val intent = android.content.Intent(android.content.Intent.ACTION_CALL)
        intent.data = android.net.Uri.parse("tel:${contact.phone}")
        startActivity(intent)
    }

    private fun messageContact(contact: EmergencyContact) {
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO)
        intent.data = android.net.Uri.parse("smsto:${contact.phone}")
        startActivity(intent)
    }
}