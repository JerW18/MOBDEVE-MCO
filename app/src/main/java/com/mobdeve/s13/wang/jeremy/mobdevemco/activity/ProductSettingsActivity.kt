package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s13.wang.jeremy.mobdevemco.R
import com.mobdeve.s13.wang.jeremy.mobdevemco.adapter.EditAdapter
import com.mobdeve.s13.wang.jeremy.mobdevemco.adapter.LogsAdapter
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.ProductSettingBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item
import com.mobdeve.s13.wang.jeremy.mobdevemco.helper.Base64Converter.Companion.convertBitmapToBase64
import com.mobdeve.s13.wang.jeremy.mobdevemco.helper.Base64Converter.Companion.uriToBase64
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemList.Companion.itemList
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemWithQuantityList.Companion.itemWithQuantityList
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.logsList.Companion.logsList
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.ItemWithQuantity

class ProductSettingsActivity : ComponentActivity() {
    private lateinit var binding: ProductSettingBinding
    private var state = "ADD"
    private var base64: String = ""
    private var filteredList = itemList.toMutableList()
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private val galleryResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                selectedImageUri?.let {
                    base64 = uriToBase64(this, it)

                    binding.ivPSImage.setImageURI(it)
                }
            }
        }

    private val cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageBitmap: Bitmap? = result.data?.extras?.get("data") as Bitmap?
                imageBitmap?.let {
                    base64 = convertBitmapToBase64(it)

                    binding.ivPSImage.setImageBitmap(it)
                }
            }
        }

    private val barcodeResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val barcode = result.data?.getStringExtra("barcode")
                barcode?.let {
                    binding.etPSProductSKU.setText(it)
                }
            }
        }

    override fun onResume() {
        super.onResume()
        binding.recyclerEdit.adapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        cameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    openCamera() // If permission is granted, open the camera
                } else {
                    // If permission is denied, show a Toast message
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()

                    // Optionally, check if the user has permanently denied the permission
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        // If the user denied the permission, we can prompt them again or explain
                        Toast.makeText(
                            this,
                            "Please grant the camera permission to take photos.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // If permission is permanently denied, guide the user to settings
                        Toast.makeText(
                            this,
                            "Camera permission is permanently denied. Please enable it in app settings.",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                }
            }

        super.onCreate(savedInstanceState)
        binding = ProductSettingBinding.inflate(layoutInflater)

        val itemSKU = intent.getStringExtra("itemSKU")
        if (itemSKU != null) {
            binding.etPSProductSKU.setText(itemSKU)
        }

        initUI()
        setContentView(binding.root)
    }

    private fun setButtonState(button: Button, isActive: Boolean) {
        button.setBackgroundResource(if (isActive) R.drawable.active_button else R.drawable.inactive_button)
        button.setTextColor(
            ContextCompat.getColor(
                this,
                if (isActive) R.color.white else R.color.green
            )
        )
    }

    private fun toggleVisibility(visibleViews: List<View>, goneViews: List<View>) {
        visibleViews.forEach { it.visibility = View.VISIBLE }
        goneViews.forEach { it.visibility = View.GONE }
    }

    private fun initUI() {
        binding.btnPSAdd.isAllCaps = false
        binding.btnPSEdit.isAllCaps = false
        binding.btnPSLogs.isAllCaps = false
        binding.btnSaveProduct.isAllCaps = false
        binding.recyclerLogs.layoutManager = LinearLayoutManager(this)
        binding.recyclerLogs.adapter = LogsAdapter(logsList)

        val dividerItemDecoration =
            DividerItemDecoration(binding.recyclerLogs.context, LinearLayoutManager.VERTICAL)
        binding.recyclerLogs.addItemDecoration(dividerItemDecoration)

        binding.recyclerEdit.layoutManager = LinearLayoutManager(this)
        binding.recyclerEdit.adapter = EditAdapter(filteredList)

        binding.ivPSBack.setOnClickListener {
            finish()
        }

        binding.btnSaveProduct.setOnClickListener {
            val sku = binding.etPSProductSKU.text.toString()
            val name = binding.etPSProductName.text.toString()
            val quantity = binding.etPSQty.text.toString().toIntOrNull() ?: 0
            val price = binding.etPSPrice.text.toString().toDoubleOrNull() ?: 0.0
            val restock = binding.etPSRestockQty.text.toString().toIntOrNull() ?: 0
            val item = Item(sku, base64, name, price, quantity, restock)
            if (name.isEmpty() || quantity == 0 || price == 0.0 || restock == 0) {
                Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                saveItemToDatabase(item)
            }
        }

        binding.btnPSAdd.setOnClickListener {
            if (state != "ADD") {
                setButtonState(binding.btnPSAdd, true)
                toggleVisibility(
                    listOf(
                        binding.tvPSRestockQtyLabel,
                        binding.etPSRestockQty,
                        binding.tvPSProductSKULabel,
                        binding.etPSProductSKU,
                        binding.btnScanSKU,
                        binding.tvPSProductNameLabel,
                        binding.tvPSPriceLabel,
                        binding.tvPSQtyLabel,
                        binding.tvPSImageLabel,
                        binding.etPSProductName,
                        binding.etPSPrice,
                        binding.etPSQty,
                        binding.etPSImage,
                        binding.ivPSImage,
                        binding.btnSaveProduct
                    ),
                    emptyList()
                )

                when (state) {
                    "EDIT" -> {
                        setButtonState(binding.btnPSEdit, false)
                        binding.recyclerEdit.visibility = View.GONE
                        binding.etSearchEdit.visibility = View.GONE
                        binding.ivSearchIconEdit.visibility = View.GONE
                    }

                    "LOGS" -> {
                        setButtonState(binding.btnPSLogs, false)
                        toggleVisibility(
                            emptyList(),
                            listOf(
                                binding.recyclerLogs,
                                binding.tvPSDateAndTime,
                                binding.tvPSInOut,
                                binding.tvPSQty
                            )
                        )
                    }
                }
                state = "ADD"
            }
        }

        binding.btnScanSKU.setOnClickListener {
            val intent = Intent(this, ScanSKUActivity::class.java)
            barcodeResultLauncher.launch(intent)
        }

        binding.btnPSEdit.setOnClickListener {
            if (state != "EDIT") {
                binding.recyclerEdit.visibility = View.VISIBLE
                binding.etSearchEdit.visibility = View.VISIBLE
                binding.ivSearchIconEdit.visibility = View.VISIBLE
                setButtonState(binding.btnPSEdit, true)

                when (state) {
                    "ADD" -> {
                        setButtonState(binding.btnPSAdd, false)
                        toggleVisibility(
                            emptyList(),
                            listOf(
                                binding.tvPSRestockQtyLabel,
                                binding.etPSRestockQty,
                                binding.tvPSProductSKULabel,
                                binding.etPSProductSKU,
                                binding.btnScanSKU,
                                binding.tvPSProductNameLabel,
                                binding.tvPSPriceLabel,
                                binding.tvPSQtyLabel,
                                binding.tvPSImageLabel,
                                binding.etPSProductName,
                                binding.etPSPrice,
                                binding.etPSQty,
                                binding.etPSImage,
                                binding.ivPSImage,
                                binding.btnSaveProduct
                            )
                        )
                    }

                    "LOGS" -> {
                        setButtonState(binding.btnPSLogs, false)
                        toggleVisibility(
                            emptyList(),
                            listOf(
                                binding.recyclerLogs,
                                binding.tvPSDateAndTime,
                                binding.tvPSInOut,
                                binding.tvPSQty
                            )
                        )
                    }
                }
                state = "EDIT"
            }
        }

        binding.btnPSLogs.setOnClickListener {
            if (state != "LOGS") {
                toggleVisibility(
                    listOf(
                        binding.recyclerLogs,
                        binding.tvPSDateAndTime,
                        binding.tvPSInOut,
                        binding.tvPSQty
                    ),
                    emptyList()
                )
                setButtonState(binding.btnPSLogs, true)

                when (state) {
                    "ADD" -> {
                        setButtonState(binding.btnPSAdd, false)
                        toggleVisibility(
                            emptyList(),
                            listOf(
                                binding.tvPSRestockQtyLabel,
                                binding.etPSRestockQty,
                                binding.tvPSProductSKULabel,
                                binding.etPSProductSKU,
                                binding.btnScanSKU,
                                binding.tvPSProductNameLabel,
                                binding.tvPSPriceLabel,
                                binding.tvPSQtyLabel,
                                binding.tvPSImageLabel,
                                binding.etPSProductName,
                                binding.etPSPrice,
                                binding.etPSQty,
                                binding.etPSImage,
                                binding.ivPSImage,
                                binding.btnSaveProduct
                            )
                        )
                    }

                    "EDIT" -> {
                        setButtonState(binding.btnPSEdit, false)
                        binding.recyclerEdit.visibility = View.GONE
                        binding.etSearchEdit.visibility = View.GONE
                        binding.ivSearchIconEdit.visibility = View.GONE
                    }
                }
                state = "LOGS"
            }
        }

        // Handle image selection via gallery
        binding.etPSImage.setOnClickListener {
            showImagePickerDialog()
        }

        binding.etSearchEdit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: android.text.Editable?) {
                searchProduct()
            }
        })
    }

    private fun searchProduct() {
        val searchQuery = binding.etSearchEdit.text.toString().lowercase()
        filteredList.clear()
        filteredList.addAll(
            itemList.filter {
                it.name.lowercase().contains(searchQuery)
            }
        )
        binding.recyclerEdit.adapter!!.notifyDataSetChanged()
    }

    private fun saveItemToDatabase(item: Item) {
        val database = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val itemsRef = database.collection("users")
                .document(userId)  // Reference to the user's document
                .collection("items")  // Subcollection to store items

            // if sku already exists, throw error
            for (i in itemList) {
                if (i.itemSKU == item.itemSKU) {
                    Toast.makeText(this, "Product SKU already exists", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            item.itemID = itemsRef.document().id

            itemsRef.add(item)
                .addOnSuccessListener {
                    itemList.add(item)
                    itemWithQuantityList.add(ItemWithQuantity(item, 0))
                    val key = "qty_${item.itemID}"
                    var sharedPreferences = getSharedPreferences("item_preferences", MODE_PRIVATE)
                    sharedPreferences.edit().putInt(key, 0).apply()

                    // Clear the input fields
                    binding.etPSProductSKU.text.clear()
                    binding.etPSProductName.text.clear()
                    binding.etPSQty.text.clear()
                    binding.etPSPrice.text.clear()
                    binding.ivPSImage.setImageResource(R.drawable.default_product)
                    binding.etPSRestockQty.text.clear()
                    Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
                    searchProduct()
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                    Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Choose from Gallery", "Take a Photo")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image Source")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openGallery() // Choose from Gallery
                1 -> openCamera() // Take a Photo
            }
        }
        builder.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResultLauncher.launch(intent)
    }

    private fun openCamera() {
        // Check if the CAMERA permission is granted
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission granted, open the camera
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                // Specify that we want the image returned directly as a Bitmap (without saving it to the storage)

                cameraResultLauncher.launch(intent)
            }

            else -> {
                // Request permission
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}