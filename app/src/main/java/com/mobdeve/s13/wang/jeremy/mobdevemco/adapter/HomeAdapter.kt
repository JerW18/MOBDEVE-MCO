package com.mobdeve.s13.wang.jeremy.mobdevemco.adapter

import android.content.Context
import android.content.SharedPreferences
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.HomeItemBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.helper.Base64Converter.Companion.decodeBase64ToBitmap
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemWithQuantityList.Companion.itemWithQuantityList
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item

class HomeAdapter(private val items: MutableList<Item>, context: Context, private val itemSelectionListener: ItemSelectionListener) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("item_preferences", Context.MODE_PRIVATE)

    class ViewHolder(private val binding: HomeItemBinding, private val itemSelectionListener: ItemSelectionListener) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item, sharedPreferences: SharedPreferences) {
            val key = "qty_${item.itemID}"

            val savedQty = sharedPreferences.getInt(key, 0)
            binding.etItemQty.setText(savedQty.toString())

            binding.tvItemProduct.text = item.name
            binding.tvItemPrice.text = "%.2f".format(item.price)
            binding.tvItemStock.text = item.stock.toString()
            binding.ivItemImage.setImageBitmap(decodeBase64ToBitmap(item.imageUri))

            // Add item logic
            binding.ivItemAdd.setOnClickListener {
                val currentQty = binding.etItemQty.text.toString().toInt()
                if (currentQty < item.stock) {
                    val newQty = currentQty + 1
                    binding.etItemQty.setText(newQty.toString())
                }
            }

            // Subtract item logic
            binding.ivItemMinus.setOnClickListener {
                val currentQty = binding.etItemQty.text.toString().toInt()
                if (currentQty > 0) {
                    val newQty = currentQty - 1
                    binding.etItemQty.setText(newQty.toString())
                }
            }

            binding.etItemQty.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun afterTextChanged(s: android.text.Editable?) {
                    if (binding.etItemQty.text.toString().isEmpty()) {
                        return
                    }
                    val newQty = binding.etItemQty.text.toString().toInt()
                    if (newQty > item.stock) {
                        binding.etItemQty.setText(item.stock.toString())
                    }
                    sharedPreferences.edit().putInt(key, newQty).apply() // Save new quantity
                    itemWithQuantityList.find { it.item.itemSKU == item.itemSKU }?.quantity = newQty
                    updateItemSelection()
                }
            })
        }

        private fun updateItemSelection() {
            val selectedCount = itemWithQuantityList.filter { it.quantity > 0 }.size
            val totalPrice = itemWithQuantityList.sumOf { it.item.price * it.quantity.toDouble() }

            itemSelectionListener.onItemSelectionChanged(selectedCount, totalPrice)
        }
    }

    interface ItemSelectionListener {
        fun onItemSelectionChanged(selectedItemCount: Int, totalPrice: Double)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = HomeItemBinding.inflate(inflater, parent, false)

        return ViewHolder(binding, itemSelectionListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], sharedPreferences)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
