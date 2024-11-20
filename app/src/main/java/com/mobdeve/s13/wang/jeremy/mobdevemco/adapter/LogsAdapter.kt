package com.mobdeve.s13.wang.jeremy.mobdevemco.adapter

import android.app.Dialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.CartDetailsBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.LogsFilterModalBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.LogsItemBinding

class LogsAdapter(private val items: List<Int>) : RecyclerView.Adapter<LogsAdapter.ViewHolder>() {

    class ViewHolder(private val binding: LogsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: Int) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LogsItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(items[position])
        holder.itemView.setOnClickListener {
                val dialogBinding = CartDetailsBinding.inflate(LayoutInflater.from(holder.itemView.context))

            // Create the dialog
            val dialog = Dialog(holder.itemView.context)
            dialog.setContentView(dialogBinding.root)
            dialogBinding.rvTransactionDetails.layoutManager = LinearLayoutManager(holder.itemView.context)
            val numList = mutableListOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            dialogBinding.rvTransactionDetails.adapter = CartAdapter(numList)

            // Set transparent background for the dialog
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            // Cancel button logic
            dialogBinding.btnCloseTrans.setOnClickListener {
                dialog.dismiss()
            }


            // Show the dialog
            dialog.show()
        }

        }


    override fun getItemCount(): Int {
        return items.size
    }
}
