package com.mobdeve.s13.wang.jeremy.mobdevemco.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.HomeItemBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.helper.Base64Converter.Companion.decodeBase64ToBitmap
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item

class HomeAdapter(private val items: List<Item>) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    class ViewHolder(private val binding: HomeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: Item) {
            binding.tvItemProduct.text = item.name
            binding.tvItemPrice.text = item.price.toString()
            binding.tvItemStock.text = item.stock.toString()
            binding.ivItemImage.setImageBitmap(decodeBase64ToBitmap(item.imageUri))
            binding.ivItemAdd.setOnClickListener {
                binding.etItemQty.setText(
                    (binding.etItemQty.text.toString().toInt() + 1).toString()
                )
            }
            binding.ivItemMinus.setOnClickListener {
                if (binding.etItemQty.text.toString().toInt() > 0)
                    binding.etItemQty.setText(
                        (binding.etItemQty.text.toString().toInt() - 1).toString()
                    )
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
