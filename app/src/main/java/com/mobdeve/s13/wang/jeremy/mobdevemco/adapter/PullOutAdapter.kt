package com.mobdeve.s13.wang.jeremy.mobdevemco.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.PullOutItemBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.helper.Base64Converter.Companion.decodeBase64ToBitmap
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.ItemWithQuantity

class PullOutAdapter(
    private val items: MutableList<ItemWithQuantity>,
    private val context: Context
) : RecyclerView.Adapter<PullOutAdapter.ViewHolder>() {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("item_preferences", Context.MODE_PRIVATE)

    class ViewHolder(private val binding: PullOutItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(
            item: ItemWithQuantity, position: Int, adapter: PullOutAdapter,
            sharedPreferences: SharedPreferences,
            onQuantityChanged: (ItemWithQuantity) -> Unit
        ) {
            val key = "qty_${item.item.itemSKU}"
                binding.tvPullOutPrice.text = item.item.price.toString()
            binding.tvPullOutStock.text = item.item.stock.toString()
            binding.ivPullOutImage.setImageBitmap(decodeBase64ToBitmap(item.item.imageUri))
            binding.tvPullOutProductName.text = item.item.name
            binding.evPullOutQty.setText(item.quantity.toString())

            binding.ivPullOutAdd.setOnClickListener {
                if (item.quantity < item.item.stock) { // Prevent quantity from exceeding stock
                    item.quantity += 1
                    binding.evPullOutQty.setText(item.quantity.toString())
                    sharedPreferences.edit().putInt(key, item.quantity)
                        .apply() // Save new quantity
                    onQuantityChanged(item)  // Notify the adapter of the quantity change
                }

            }

            binding.ivPullOutMinus.setOnClickListener {
                if (item.quantity > 1) { // Prevent quantity from going below 1
                    item.quantity -= 1
                    binding.evPullOutQty.setText(item.quantity.toString())
                        sharedPreferences.edit().putInt(key, item.quantity)
                            .apply() // Save new quantity
                        onQuantityChanged(item)  // Notify the adapter of the quantity change
                    } else if (item.quantity == 1) {
                        item.quantity -= 1
                        binding.evPullOutQty.setText(item.quantity.toString())
                        sharedPreferences.edit().putInt(key, item.quantity)
                            .apply() // Save new quantity

                        adapter.items.removeAt(position)
                        adapter.notifyDataSetChanged()
                    }
                }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PullOutItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(items[position], position, this, sharedPreferences) { updatedItem ->
            items[position] = updatedItem
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
