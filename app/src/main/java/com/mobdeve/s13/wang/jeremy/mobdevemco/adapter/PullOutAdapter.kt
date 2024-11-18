package com.mobdeve.s13.wang.jeremy.mobdevemco.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.PullOutItemBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item

class PullOutAdapter(private val items: List<Item>) : RecyclerView.Adapter<PullOutAdapter.ViewHolder>() {

    class ViewHolder(private val binding: PullOutItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: Item) {
            binding.tvPullOutPrice.text = item.price.toString()
            binding.evPullOutQty.setText(item.stock.toString())
//            binding.ivPullOutImage.setImageURI(Uri.parse(item.imageUri))
            binding.tvPullOutProductName.text = item.name

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PullOutItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
