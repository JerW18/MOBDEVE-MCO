package com.mobdeve.s13.wang.jeremy.mobdevemco.adapter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s13.wang.jeremy.mobdevemco.activity.EditProductActivity
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.ProductDeletionConfirmationBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.EditItemBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.helper.Base64Converter.Companion.decodeBase64ToBitmap
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemList.Companion.itemList
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemWithQuantityList.Companion.itemWithQuantityList

class EditAdapter(private val items: MutableList<Item>) : RecyclerView.Adapter<EditAdapter.ViewHolder>() {

    class ViewHolder(private val binding: EditItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: Item, adapter: EditAdapter, items: MutableList<Item>) {
            binding.ivPSEditImage.setImageBitmap(decodeBase64ToBitmap(item.imageUri))
            val formattedPrice = String.format("%.2f", item.price)
            binding.tvPSEditPrice.text = formattedPrice
            binding.tvPSEditStock.text = item.stock.toString()
            binding.tvPSEditProductName.text = item.name
            binding.tvPSEdit.isAllCaps = false
            binding.tvPSDelete.isAllCaps = false
            binding.tvPSEdit.setOnClickListener {
                val intent = Intent(binding.root.context, EditProductActivity::class.java)
                intent.putExtra("itemSKU", item.itemSKU)
                intent.putExtra("name", item.name)
                intent.putExtra("price", formattedPrice)
                intent.putExtra("stock", item.stock.toString())
                intent.putExtra("imageUri", item.imageUri)
                intent.putExtra("restock", item.restock.toString())
                startActivity(binding.root.context, intent, null)
            }
            binding.tvPSDelete.setOnClickListener {
                showDeleteConfirmationDialog(item, items, adapter, binding)
            }

        }

        private fun showDeleteConfirmationDialog(item: Item, items: MutableList<Item>, adapter: EditAdapter, binding: EditItemBinding) {
            val dialogBinding = ProductDeletionConfirmationBinding.inflate(LayoutInflater.from(binding.root.context))

            val dialog = Dialog(binding.root.context)
            dialog.setContentView(dialogBinding.root)

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            dialogBinding.btnProductDeletionCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.btnProductDeletionConfirm.setOnClickListener {
                dialog.dismiss()
                itemList.remove(item)
                items.remove(item)
                itemWithQuantityList.remove(itemWithQuantityList.find { it.item.itemSKU == item.itemSKU })
                binding.root.context.getSharedPreferences("item_preferences", Context.MODE_PRIVATE).edit().remove("qty_${item.itemSKU}").apply()
                adapter.notifyDataSetChanged()
                deleteItem(item)
            }

            dialog.show()
        }

        private fun deleteItem(item: Item) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val db = FirebaseFirestore.getInstance()
            val userId = currentUser!!.uid
            val itemsRef = db.collection("users").document(userId).collection("items")
            itemsRef.whereEqualTo("itemSKU", item.itemSKU).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // Get the first document that matches the itemSKU
                        val document = querySnapshot.documents.first()

                        // Delete the document
                        document.reference.delete()
                            .addOnSuccessListener {
                                Log.d("Firestore", "Item successfully deleted")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error deleting item", e)
                            }
                    } else {
                        Log.d("Firestore", "No item found with SKU: $item.itemSKU")
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error getting documents", e)
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = EditItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(items[position], this, items)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
