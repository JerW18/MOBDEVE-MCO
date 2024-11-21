package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s13.wang.jeremy.mobdevemco.adapter.PullOutAdapter
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.PullOutBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemList.Companion.itemList
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemWithQuantityList.Companion.itemWithQuantityList
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.logsList.Companion.logsList
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Logs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PullOutActivity : ComponentActivity(), PullOutAdapter.ItemSelectionListener {
    private lateinit var binding: PullOutBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("item_preferences", MODE_PRIVATE)

        binding = PullOutBinding.inflate(layoutInflater)
        initUI()
        setContentView(binding.root)
    }


    private fun initUI() {
        binding.btnPullOut.isAllCaps = false
        binding.ivPullOutBack.setOnClickListener {
            finish()
        }

        binding.btnPullOut.setOnClickListener {
            //take all the items with quantity > 0 and add them to the logs in firestore
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                val logsRef = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("logs")

                // Filter items with quantity > 0
                val items = itemWithQuantityList.filter { it.quantity > 0 }



                if (items.isNotEmpty()) {
                    // Format the current date and time
                    val date = Date()
                    val dateFormat = SimpleDateFormat("MM.dd.yyyy HH:mm:ss", Locale.getDefault())
                    val dateString = dateFormat.format(date)

                    val totalCost = items.sumOf { it.item.price * it.quantity }

                    val log = Logs(dateString, items, totalCost, "Out")

                    logsRef.add(log)
                        .addOnSuccessListener {
                            for (item in items) {
                                val itemRef = FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(userId)
                                    .collection("items")

                                itemRef
                                    .whereEqualTo("itemID", item.item.itemID)
                                    .get()
                                    .addOnSuccessListener { querySnapshot ->
                                        if (!querySnapshot.isEmpty) {
                                            val document = querySnapshot.documents[0]
                                            val documentId = document.id

                                            val newStock = item.item.stock - item.quantity

                                            itemRef.document(documentId)
                                                .update("stock", newStock)
                                                .addOnSuccessListener {
                                                    val itemInList = itemList.find { it.itemID == item.item.itemID }
                                                    if (itemInList != null) {
                                                        itemInList.stock = newStock
                                                    }

                                                    val itemWithQuantity = itemWithQuantityList.find { it.item.itemID == item.item.itemID }
                                                    if (itemWithQuantity != null) {
                                                        itemWithQuantity.item.stock = newStock
                                                        itemWithQuantity.quantity = 0
                                                    }

                                                    val key = "qty_${item.item.itemID}"
                                                    sharedPreferences.edit().putInt(key, 0).apply()


                                                    Log.d("FirestoreUpdate", "Item ${item.item.itemID} updated successfully.")

                                                    val resultIntent = Intent()
                                                    setResult(RESULT_OK, resultIntent)
                                                    finish()
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("FirestoreUpdate", "Error updating item ${item.item.itemID}: ${e.message}")
                                                }
                                        } else {
                                            Log.e("FirestoreUpdate", "No document found for itemId: ${item.item.itemID}")
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("FirestoreQuery", "Error querying Firestore for itemId ${item.item.itemID}: ${e.message}")
                                    }
                            }

                            Toast.makeText(this, "Items successfully checked out.", Toast.LENGTH_SHORT).show()

                            logsList.add(0, log)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error adding log: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "No items with quantity greater than 0.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            }

        }
        binding.recyclerPullOut.layoutManager = LinearLayoutManager(this)
        val filteredList = itemWithQuantityList.filter { it.quantity > 0 }.toMutableList()
        binding.recyclerPullOut.adapter = PullOutAdapter(filteredList, this, this)
        binding.tvPullOutTotal.text = "${filteredList.sumOf { it.item.price * it.quantity }}"
    }

    override fun onItemSelectionChanged(totalPrice: Double) {
        binding.tvPullOutTotal.text = "$totalPrice"
    }
}