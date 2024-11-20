package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s13.wang.jeremy.mobdevemco.R
import com.mobdeve.s13.wang.jeremy.mobdevemco.adapter.HomeAdapter
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.HomeBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemList.Companion.itemList
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemWithQuantityList.Companion.itemWithQuantityList
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.ItemWithQuantity

class HomeActivity : ComponentActivity() {
    private lateinit var binding: HomeBinding
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("item_preferences", Context.MODE_PRIVATE)
        binding = HomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CoroutineScope(Dispatchers.Main).launch {
            getItem()
            initUI()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.recyclerHome.adapter?.notifyDataSetChanged()
        // check if user is still authenticated if not redirect to loginactivity and finish the current activity
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private suspend fun getItem() {
        itemList.clear()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val itemsRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("items")

            try {
                val querySnapshot = itemsRef.get().await()

                if (!querySnapshot.isEmpty) {
                    val items = querySnapshot.toObjects(Item::class.java)
                    for (item in items) {
                        itemList.add(item)
                        val key = "qty_${item.itemSKU}"
                        itemWithQuantityList.add(
                            ItemWithQuantity(
                                item,
                                sharedPreferences.getInt(key, 0)
                            )
                        )
                        Log.d(
                            ContentValues.TAG,
                            "Item: ${item.name}, SKU: ${item.itemSKU}, Image URL: ${item.imageUri}"
                        )
                    }
                } else {
                    Log.d(ContentValues.TAG, "No items found.")
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error getting documents: ", e)
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initUI() {
        val text = binding.tvAppName.text.toString()
        val spannableString = SpannableString(text)
        val startIndex = text.indexOf("IT")
        val endIndex = startIndex + 2
        val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.dark_green))
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val sharedPreferences = getSharedPreferences("item_preferences", MODE_PRIVATE)

        binding.tvAppName.text = spannableString
        binding.btnReview.isAllCaps = false
        binding.recyclerHome.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerHome.adapter = HomeAdapter(itemList, this)

        binding.btnReview.setOnClickListener {
            val intent = Intent(this, PullOutActivity::class.java)
            startActivity(intent)
        }

        binding.ivNotif.setOnClickListener {
            val intent = Intent(this, NotifActivity::class.java)
            startActivity(intent)
        }

        binding.ivAcc.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }

        binding.ivSetting.setOnClickListener {
            val intent = Intent(this, ProductSettingsActivity::class.java)
            startActivity(intent)
        }

        binding.ivScan.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }

    }
}