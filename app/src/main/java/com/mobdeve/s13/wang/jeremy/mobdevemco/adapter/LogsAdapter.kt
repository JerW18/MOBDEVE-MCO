package com.mobdeve.s13.wang.jeremy.mobdevemco.adapter

import android.app.Dialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.CartDetailsBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.LogsItemBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Logs

class LogsAdapter(private val logs: List<Logs>) : RecyclerView.Adapter<LogsAdapter.ViewHolder>() {

    class ViewHolder(private val binding: LogsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(log: Logs) {
            binding.tvLogItemDateTime.text = log.date
            binding.tvLogItemInOut.text = log.type
            binding.tvLogItemQty.text = "₱ ${log.total}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LogsItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(logs[position])
        holder.itemView.setOnClickListener {
            val dialogBinding =
                CartDetailsBinding.inflate(LayoutInflater.from(holder.itemView.context))

            // Create the dialog
            val dialog = Dialog(holder.itemView.context)
            dialog.setContentView(dialogBinding.root)
            dialogBinding.rvTransactionDetails.layoutManager =
                LinearLayoutManager(holder.itemView.context)

            dialogBinding.rvTransactionDetails.adapter = CartAdapter(logs[position].items)

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
        return logs.size
    }
}
