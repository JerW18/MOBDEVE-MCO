package com.mobdeve.s13.wang.jeremy.mobdevemco.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.PullOutItemBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.helper.Base64Converter.Companion.decodeBase64ToBitmap
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.ItemWithQuantity

class PullOutAdapter(private val items: MutableList<ItemWithQuantity>) :
    RecyclerView.Adapter<PullOutAdapter.ViewHolder>() {

    class ViewHolder(private val binding: PullOutItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

            fun bindData(item: ItemWithQuantity, onQuantityChanged: (ItemWithQuantity) -> Unit) {
                if (item.quantity != 0) {
                    binding.root.visibility = View.VISIBLE

                    binding.tvPullOutPrice.text = item.item.price.toString()
                    binding.evPullOutQty.setText(item.item.stock.toString())
                    binding.ivPullOutImage.setImageBitmap(decodeBase64ToBitmap(item.item.imageUri))
                    binding.tvPullOutProductName.text = item.item.name
                    binding.evPullOutQty.setText(item.quantity.toString())

                    binding.ivPullOutAdd.setOnClickListener {
                        item.quantity += 1
                        binding.evPullOutQty.setText(item.quantity.toString())
                        onQuantityChanged(item)  // Notify the adapter of the quantity change
                    }

                    binding.ivPullOutMinus.setOnClickListener {
                        if (item.quantity > 1) { // Prevent quantity from going below 1
                            item.quantity -= 1
                            binding.evPullOutQty.setText(item.quantity.toString())
                            onQuantityChanged(item)  // Notify the adapter of the quantity change
                        }
                    }
                } else {
                    // Hide the view if quantity == 0
                    binding.root.visibility = View.GONE
                }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PullOutItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(items[position]) { updatedItem ->
            items[position] = updatedItem
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
