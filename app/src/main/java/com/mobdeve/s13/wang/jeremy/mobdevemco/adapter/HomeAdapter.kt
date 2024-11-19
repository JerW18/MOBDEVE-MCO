package com.mobdeve.s13.wang.jeremy.mobdevemco.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.HomeItemBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.helper.Base64Converter.Companion.decodeBase64ToBitmap
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item

class HomeAdapter(private val items: List<Item>, private val context: Context) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("item_preferences", Context.MODE_PRIVATE)

    class ViewHolder(private val binding: HomeItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item, sharedPreferences: SharedPreferences) {
            val key = "qty_${item.itemSKU}"

            val savedQty = sharedPreferences.getInt(key, 0)
            binding.etItemQty.setText(savedQty.toString())

            binding.tvItemProduct.text = item.name
            binding.tvItemPrice.text = item.price.toString()
            binding.tvItemStock.text = item.stock.toString()
            binding.ivItemImage.setImageBitmap(decodeBase64ToBitmap(item.imageUri))

            // Add item logic
            binding.ivItemAdd.setOnClickListener {
                val currentQty = binding.etItemQty.text.toString().toInt()
                val newQty = currentQty + 1
                binding.etItemQty.setText(newQty.toString())
                sharedPreferences.edit().putInt(key, newQty).apply() // Save new quantity
            }

            // Subtract item logic
            binding.ivItemMinus.setOnClickListener {
                val currentQty = binding.etItemQty.text.toString().toInt()
                if (currentQty > 0) {
                    val newQty = currentQty - 1
                    binding.etItemQty.setText(newQty.toString())
                    sharedPreferences.edit().putInt(key, newQty).apply() // Save new quantity
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = HomeItemBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], sharedPreferences)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
