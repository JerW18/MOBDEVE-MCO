package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s13.wang.jeremy.mobdevemco.R
import com.mobdeve.s13.wang.jeremy.mobdevemco.adapter.HomeAdapter
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.HomeBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.helper.NetworkChecker
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemList.Companion.itemList
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemWithQuantityList.Companion.itemWithQuantityList
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.logsList.Companion.logsList
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.ItemWithQuantity
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Logs
import com.mobdeve.s13.wang.jeremy.mobdevemco.helper.OnSwipeTouchListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class HomeActivity : ComponentActivity(), HomeAdapter.ItemSelectionListener {
    private lateinit var binding: HomeBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var filteredList = mutableListOf<Item>()

    private val pullOutActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            if (it.resultCode == RESULT_OK) {
                val notifList = mutableListOf<String>()
                val check = it.data?.getStringArrayExtra("itemID")
                if (check != null) {
                    for (item in itemList) {
                        if (check.contains(item.itemID)) {
                            if (item.stock <= item.restock) {
                                notifList.add(item.name)
                            }
                        }
                    }
                    if (notifList.isNotEmpty()) {
                        shownotif(notifList)
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    searchProduct()
                    binding.recyclerHome.adapter?.notifyDataSetChanged()
                    binding.tvNumItemSelected.text =
                        "${itemWithQuantityList.filter { it.quantity > 0 }.size} items selected"
                    binding.tvTotalSum.text =
                        "₱ %.2f".format(itemWithQuantityList.sumOf { it.item.price * it.quantity })

                }
            }
        }

    private fun shownotif(notifList: MutableList<String>) {
        var notifString = "Stocks for "
        var first = true
        for (item in notifList) {
            if (first) {
                notifString += item
                first = false
            } else if (notifList.indexOf(item) == notifList.size - 1)
                notifString += " and $item"
            else
                notifString += ", $item"
        }
        notifString += " are running low!"
        binding.notifBar.translationY = -200f
        binding.notifText.text = notifString
        binding.notifBar.visibility = View.VISIBLE

        // delay 2 seconds
        binding.notifBar.postDelayed({
            animateNotifBar()
        }, 2000)
    }

    private fun animateNotifBar() {
        binding.notifBar.animate().translationY(0f).setDuration(1000).withEndAction {
            // Delay 3 seconds before starting the next animation
            binding.notifBar.postDelayed({
                // Animate it going up
                binding.notifBar.animate().translationY(-200f).setDuration(1000).withEndAction {
                    // Hide the notification bar after the animation ends
                    binding.notifBar.visibility = View.GONE
                }
            }, 2000) // 3000 milliseconds = 3 seconds delay
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("item_preferences", Context.MODE_PRIVATE)
        binding = HomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // create thread that constantly checks if there is wifi
        lifecycleScope.launch(Dispatchers.IO) {
            var isRunning = true
            while (isRunning) {
                val wifiChecker = NetworkChecker().isConnectedToInternet(this@HomeActivity)
                Log.d(ContentValues.TAG, "Wifi Checker: $wifiChecker")
                if (!wifiChecker) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@HomeActivity,
                            "Can't connect to the database",
                            Toast.LENGTH_SHORT
                        ).show()
                        // sign out user
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    Log.d(ContentValues.TAG, "Setting isRunning to false")
                    isRunning = false
                }
                delay(5000)
            }
        }


    }

    override fun onItemSelectionChanged(selectedItemCount: Int, totalPrice: Double) {
        binding.tvNumItemSelected.text = "$selectedItemCount items selected"
        binding.tvTotalSum.text = "₱ %.2f".format(totalPrice)
}
    override fun onResume() {
        super.onResume()
        searchProduct()
        sharedPreferences = getSharedPreferences("item_preferences", Context.MODE_PRIVATE)

        CoroutineScope(Dispatchers.Main).launch {
            getLogs()
            getItem()
            filteredList = itemList.toMutableList()
            initUI()
            binding.tvNumItemSelected.text =
                "${itemWithQuantityList.filter { it.quantity > 0 }.size} items selected"
            binding.tvTotalSum.text =
                "₱ %.2f".format(itemWithQuantityList.sumOf { it.item.price * it.quantity })
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private suspend fun getItem() {
        itemList.clear()
        itemWithQuantityList.clear()
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
                        val key = "qty_${item.itemID}"
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

    private suspend fun getLogs() {
        logsList.clear()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val logsRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("logs")

            try {
                val querySnapshot = logsRef.get().await()

                if (!querySnapshot.isEmpty) {
                    val logs = querySnapshot.toObjects(Logs::class.java)
                    for (log in logs) {
                        logsList.add(log)
                        Log.d("GetLogs", "Log retrieved: ${log.date}")
                    }
                    logsList.sortByDescending { it.date }
                    Log.d("GetLogs", "Logs retrieved successfully: ${logsList.size} logs")
                } else {
                    Log.d("GetLogs", "No logs found.")
                }
            } catch (e: Exception) {
                Log.e("GetLogs", "Error getting logs: ", e)
            }
        } else {
            Log.e("GetLogs", "User not authenticated.")
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

        binding.tvAppName.text = spannableString
        binding.btnReview.isAllCaps = false
        binding.recyclerHome.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerHome.adapter = HomeAdapter(filteredList, this, this)

        @Suppress("ClickableViewAccessibility")
        binding.notifBar.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeUp() {
                binding.notifBar.clearAnimation()
                binding.notifBar.animate().translationY(-200f).setDuration(500).withEndAction {
                    binding.notifBar.visibility = View.GONE
                }
            }

        })

        binding.notifBar.setOnClickListener {
            // Navigate to NotifActivity on click
            val intent = Intent(this, NotifActivity::class.java)
            startActivity(intent)
        }

        binding.btnReview.setOnClickListener {
            val intent = Intent(this, PullOutActivity::class.java)
            pullOutActivityLauncher.launch(intent)
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

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: android.text.Editable?) {
                searchProduct()
            }
        })

    }

    private fun searchProduct() {
        val searchQuery = binding.etSearch.text.toString().lowercase()
        filteredList.clear()
        filteredList.addAll(
            itemList.filter {
                it.name.lowercase().contains(searchQuery)
            }
        )
        binding.recyclerHome.adapter?.notifyDataSetChanged()
    }
}