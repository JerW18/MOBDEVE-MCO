package com.mobdeve.s13.wang.jeremy.mobdevemco.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.NotifItemBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item

class NotifAdapter(private val items: List<Item>) : RecyclerView.Adapter<NotifAdapter.ViewHolder>() {

    class ViewHolder(private val binding: NotifItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: Item) {
            binding.notifText.text = "${item.name} currently has ${item.stock} items left, and the restock point is ${item.restock} items."
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = NotifItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
