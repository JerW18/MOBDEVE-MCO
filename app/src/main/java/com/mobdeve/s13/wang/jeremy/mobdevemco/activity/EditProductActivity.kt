package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.EditProductBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.helper.Base64Converter.Companion.convertBitmapToBase64
import com.mobdeve.s13.wang.jeremy.mobdevemco.helper.Base64Converter.Companion.decodeBase64ToBitmap
import com.mobdeve.s13.wang.jeremy.mobdevemco.helper.Base64Converter.Companion.uriToBase64
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemList.Companion.itemList
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemWithQuantityList.Companion.itemWithQuantityList

class EditProductActivity: ComponentActivity() {
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var binding: EditProductBinding
    private lateinit var base64: String;
    private lateinit var itemSKU: String;
    private lateinit var name: String;
    private lateinit var price: String;
    private lateinit var stock: String;
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    private fun initUI(){
        itemSKU = intent.getStringExtra("itemSKU").toString()
        name = intent.getStringExtra("name").toString()
        price = intent.getStringExtra("price").toString()
        stock = intent.getStringExtra("stock").toString()
        base64 = intent.getStringExtra("imageUri").toString()
        binding.etPSProductSKU.isFocusable = false
        binding.etPSProductSKU.setText(itemSKU)
        binding.etPSProductName.setText(name)
        binding.etPSPrice.setText(price)
        binding.etPSQty.setText(stock)
        binding.ivPSImage.setImageBitmap(decodeBase64ToBitmap(base64))
        binding.ivPSBack.setOnClickListener {
            finish()
        }
        binding.etPSImage.setOnClickListener {
            showImagePickerDialog()
        }
        binding.btnSaveProduct.setOnClickListener {
            itemList.find { it.itemSKU == itemSKU }?.let {
                it.name = binding.etPSProductName.text.toString()
                it.price = binding.etPSPrice.text.toString().toFloat()
                it.stock = binding.etPSQty.text.toString().toInt()
                it.imageUri = base64
            }
            itemWithQuantityList.find { it.item.itemSKU == itemSKU }?.let {
                it.item.name = binding.etPSProductName.text.toString()
                it.item.price = binding.etPSPrice.text.toString().toFloat()
                it.item.stock = binding.etPSQty.text.toString().toInt()
                it.item.imageUri = base64
            }
            val currentUser = FirebaseAuth.getInstance().currentUser
            val db = FirebaseFirestore.getInstance()
            val userId = currentUser!!.uid
            val itemsRef = db.collection("users").document(userId).collection("items")
            itemsRef.whereEqualTo("itemSKU", itemSKU).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // Get the first document that matches the itemSKU
                        val document = querySnapshot.documents.first()

                        val updatedData = hashMapOf(
                            "name" to binding.etPSProductName.text.toString(),
                            "price" to binding.etPSPrice.text.toString().toFloat(),
                            "stock" to binding.etPSQty.text.toString().toInt(),
                            "imageUri" to base64
                        )
                        document.reference.update(updatedData as Map<String, Any>)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Item successfully updated")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error updating item", e)
                            }
                        finish()
                    } else {
                        Log.d("Firestore", "No item found with SKU: $itemSKU")
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error getting documents", e)
                }
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