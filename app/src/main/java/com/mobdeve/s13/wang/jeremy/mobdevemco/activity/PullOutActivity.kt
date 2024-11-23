package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s13.wang.jeremy.mobdevemco.adapter.PullOutAdapter
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.PullOutBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemList.Companion.itemList
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemWithQuantityList.Companion.itemWithQuantityList
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.logsList.Companion.logsList
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.ItemDetails
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.ItemLog
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Logs
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
            // Ensure that the entire logic is inside a coroutine scope
            lifecycleScope.launch {
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
                        val dateFormat =
                            SimpleDateFormat("MM.dd.yyyy HH:mm:ss", Locale.getDefault())
                        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Manila")
                        val dateString = dateFormat.format(date)


                        val totalCost = items.sumOf { it.item.price * it.quantity }

                        // create logItem
                        val logitems = items.map { item ->
                            ItemLog(
                                item = ItemDetails(
                                    itemID = item.item.itemID,
                                    name = item.item.name,
                                    price = item.item.price,
                                    stock = item.item.stock
                                ),
                                quantity = item.quantity
                            )
                        }
                        val log = Logs(dateString, logitems, totalCost, "Out")

                        try {
                            // Add log to Firestore (awaiting this operation)
                            logsRef.add(log).await()

                            // Iterate over each item and process them asynchronously
                            val tasks = items.map { item ->
                                async {
                                    val itemRef = FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(userId)
                                        .collection("items")

                                    val querySnapshot = itemRef
                                        .whereEqualTo("itemID", item.item.itemID)
                                        .get()
                                        .await()

                                    if (!querySnapshot.isEmpty) {
                                        val document = querySnapshot.documents[0]
                                        val documentId = document.id
                                        val newStock = item.item.stock - item.quantity

                                        // Update stock in Firestore (awaiting this operation)
                                        itemRef.document(documentId)
                                            .update("stock", newStock)
                                            .await()

                                        // Update local lists and shared preferences
                                        val itemInList =
                                            itemList.find { it.itemID == item.item.itemID }
                                        itemInList?.stock = newStock

                                        val itemWithQuantity =
                                            itemWithQuantityList.find { it.item.itemID == item.item.itemID }
                                        itemWithQuantity?.apply {
                                            item.item.stock = newStock
                                            quantity = 0
                                        }

                                        val key = "qty_${item.item.itemID}"
                                        sharedPreferences.edit().putInt(key, 0).apply()

                                        Log.d(
                                            "FirestoreUpdate",
                                            "Item ${item.item.itemID} updated successfully."
                                        )
                                    } else {
                                        Log.e(
                                            "FirestoreUpdate",
                                            "No document found for itemId: ${item.item.itemID}"
                                        )
                                    }
                                }
                            }

                            // Wait for all Firestore updates to complete
                            tasks.awaitAll()

                            // Show success message
                            Toast.makeText(
                                this@PullOutActivity,
                                "Items successfully pulled out.",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Add log to local logs list
                            logsList.add(0, log)

                            // Return result to previous activity
                            val resultIntent = Intent()
                            // return a list of item id changed

                            resultIntent.putExtra(
                                "itemID",
                                items.map { it.item.itemID }.toTypedArray()
                            )
                            setResult(RESULT_OK, resultIntent)
                            finish()

                        } catch (e: Exception) {
                            // Handle errors
                            Log.e("FirestoreUpdate", "Error processing items: ${e.message}")
                            Toast.makeText(
                                this@PullOutActivity,
                                "Error processing items: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@PullOutActivity,
                            "No items with quantity greater than 0.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@PullOutActivity, "User not logged in.", Toast.LENGTH_SHORT)
                        .show()
                }
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