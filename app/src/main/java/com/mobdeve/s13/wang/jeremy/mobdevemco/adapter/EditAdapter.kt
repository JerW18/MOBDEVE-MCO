package com.mobdeve.s13.wang.jeremy.mobdevemco.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.EditItemBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.helper.Base64Converter.Companion.decodeBase64ToBitmap
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item

class EditAdapter(private val items: List<Item>) : RecyclerView.Adapter<EditAdapter.ViewHolder>() {

    class ViewHolder(private val binding: EditItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: Item) {
            binding.ivPSEditImage.setImageBitmap(decodeBase64ToBitmap(item.imageUri))
            binding.tvPSEditPrice.text = item.price.toString()
            binding.tvPSEditStock.text = item.stock.toString()
            binding.tvPSEditProductName.text = item.name
            binding.tvPSEdit.isAllCaps = false
            binding.tvPSDelete.isAllCaps = false

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = EditItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
