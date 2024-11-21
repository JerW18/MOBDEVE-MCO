package com.mobdeve.s13.wang.jeremy.mobdevemco.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.CartDetailsBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.LogsFilterBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.LogsItemBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.TransactionItemBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.ItemWithQuantity
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Logs

class CartAdapter(private val products: List<ItemWithQuantity>) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(private val binding: TransactionItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: ItemWithQuantity) {
            binding.tvTransItem.text = item.item.name
            binding.tvTransQty.text = "${item.quantity}"
            binding.tvTransPrice.text = "₱ ${item.item.price}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TransactionItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(products[position])
    }

    override fun getItemCount(): Int {
        return products.size
    }
}