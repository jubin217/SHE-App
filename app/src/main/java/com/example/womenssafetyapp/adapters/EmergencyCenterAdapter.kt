package com.example.womenssafetyapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.womenssafetyapp.R
import com.example.womenssafetyapp.models.EmergencyCenter

class EmergencyCenterAdapter(
    private val centers: List<EmergencyCenter>,
    private val onItemClick: (EmergencyCenter, String) -> Unit
) : RecyclerView.Adapter<EmergencyCenterAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_center_name)
        val tvDistance: TextView = itemView.findViewById(R.id.tv_distance)
        val tvType: TextView = itemView.findViewById(R.id.tv_center_type)
        val tvAddress: TextView = itemView.findViewById(R.id.tv_center_address)
        val btnCall: Button = itemView.findViewById(R.id.btn_call)
        val btnNavigate: Button = itemView.findViewById(R.id.btn_navigate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emergency_center, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val center = centers[position]

        holder.tvName.text = center.name
        holder.tvDistance.text = if (center.distance > 0) {
            String.format("%.1f km", center.distance)
        } else {
            ""
        }
        holder.tvType.text = center.type
        holder.tvAddress.text = center.address

        holder.btnCall.setOnClickListener {
            onItemClick(center, "call")
        }

        holder.btnNavigate.setOnClickListener {
            onItemClick(center, "navigate")
        }
    }

    override fun getItemCount(): Int = centers.size
}