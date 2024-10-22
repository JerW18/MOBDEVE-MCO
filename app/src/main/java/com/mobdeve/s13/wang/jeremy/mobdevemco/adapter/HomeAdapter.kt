package com.mobdeve.s13.wang.jeremy.mobdevemco.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.HomeItemBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item

class HomeAdapter(private val items: List<Item>) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    class ViewHolder(private val binding: HomeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: Item) {
            binding.tvProductName.text = item.name
            binding.tvPrice.text = item.price.toString()
            binding.tvStock.text = item.stock.toString()
            binding.ivItemImage.setImageResource(item.imageId)
            binding.ivAdd.setOnClickListener{
                binding.etQty.setText((binding.etQty.text.toString().toInt() + 1).toString())
            }
            binding.ivMinus.setOnClickListener{
                if (binding.etQty.text.toString().toInt() > 0)
                    binding.etQty.setText((binding.etQty.text.toString().toInt() - 1).toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = HomeItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
