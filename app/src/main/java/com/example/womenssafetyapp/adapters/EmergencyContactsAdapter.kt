package com.example.womenssafetyapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.womenssafetyapp.R
import com.example.womenssafetyapp.models.EmergencyContact

class EmergencyContactsAdapter(
    private val contacts: List<EmergencyContact>,
    private val onItemClick: (EmergencyContact, String) -> Unit
) : RecyclerView.Adapter<EmergencyContactsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_contact_name)
        val tvPhone: TextView = itemView.findViewById(R.id.tv_contact_phone)
        val tvRelationship: TextView = itemView.findViewById(R.id.tv_relationship)
        val ivDelete: View = itemView.findViewById(R.id.iv_delete)
        val ivCall: View = itemView.findViewById(R.id.iv_call)
        val ivMessage: View = itemView.findViewById(R.id.iv_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]

        holder.tvName.text = contact.name
        holder.tvPhone.text = contact.phone
        holder.tvRelationship.text = contact.relationship

        holder.ivDelete.setOnClickListener {
            onItemClick(contact, "delete")
        }

        holder.ivCall.setOnClickListener {
            onItemClick(contact, "call")
        }

        holder.ivMessage.setOnClickListener {
            onItemClick(contact, "message")
        }
    }

    override fun getItemCount(): Int = contacts.size
}