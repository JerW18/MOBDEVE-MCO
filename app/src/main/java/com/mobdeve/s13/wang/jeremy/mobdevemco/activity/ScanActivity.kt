package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextWatcher
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.ScanBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.mobdeve.s13.wang.jeremy.mobdevemco.helper.Base64Converter.Companion.decodeBase64ToBitmap
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemList.Companion.itemList
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.ItemWithQuantity
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemWithQuantityList.Companion.itemWithQuantityList

class ScanActivity : ComponentActivity() {
    private lateinit var binding: ScanBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var barcodeScanner: BarcodeScanner
    private var rawValue: String = ""
    private lateinit var sharedPreferences: SharedPreferences

    private var hasScanned = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("item_preferences", MODE_PRIVATE)

        binding = ScanBinding.inflate(layoutInflater)
        initUI()
        setContentView(binding.root)
        initCamera()
        barcodeScanner = BarcodeScanning.getClient()
    }

    private fun initUI() {
        binding.btnScanPullOut.isAllCaps = false
        binding.btnRestock.isAllCaps = false

        binding.btnRestock.setOnClickListener {
            val firestore = FirebaseFirestore.getInstance()
            val userUid = FirebaseAuth.getInstance().currentUser?.uid

            if (userUid != null) {
                firestore.collection("users")
                    .document(userUid)
                    .collection("items")
                    .whereEqualTo("itemSKU", rawValue)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val item = querySnapshot.documents[0].toObject(Item::class.java)
                            if (item != null) {
                                val newStock = item.stock + binding.etScanQty.text.toString().toInt()
                                firestore.collection("users")
                                    .document(userUid)
                                    .collection("items")
                                    .document(querySnapshot.documents[0].id)
                                    .update("stock", newStock)
                                    .addOnSuccessListener {
                                        //update all associated views
                                        binding.tvScanStock.text = newStock.toString()
                                        itemList.forEach { item ->
                                            if (item.itemSKU == rawValue) {
                                                item.stock = newStock
                                            }
                                        }
                                        itemWithQuantityList.forEach { item ->
                                            if (item.item.itemSKU == rawValue) {
                                                item.item.stock = newStock
                                            }
                                        }
                                        Toast.makeText(this, "Stock updated.", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error updating stock: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(this, "Item format error.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Item not found.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error fetching item: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            }

        }

        binding.btnScanPullOut.setOnClickListener {
            val item = itemWithQuantityList.find { it.item.itemSKU == rawValue }

            if (item != null) {
                val newQty = binding.etScanQty.text.toString().toInt() + item.quantity

                if (newQty > item.item.stock) {
                    Toast.makeText(this, "Quantity exceeds stock.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (newQty >= 0) {
                    item.quantity = newQty
                    val key = "qty_${item.item.itemID}"
                    sharedPreferences.edit().putInt(key, newQty).apply()
                    Toast.makeText(this, "Quantity updated.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Invalid quantity.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Item not found.", Toast.LENGTH_SHORT).show()
            }
            finish()
        }

        binding.ivScanBack.setOnClickListener {
            finish()
        }

        binding.ivScanMinus.setOnClickListener {
            if (binding.etScanQty.text.toString().toInt() > 0) {
                binding.etScanQty.setText(
                    (binding.etScanQty.text.toString().toInt() - 1).toString()
                )
            }
        }
        binding.ivScanAdd.setOnClickListener {
            binding.etScanQty.setText((binding.etScanQty.text.toString().toInt() + 1).toString())
        }

        binding.btnScanAddNew.setOnClickListener {
            val intent = Intent(this, ProductSettingsActivity::class.java).apply {
                putExtra("itemSKU", rawValue)
            }
            startActivity(intent)
        }
    }

    private fun initCamera() {
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    startCamera()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, { imageProxy ->
                        if (!hasScanned) {
                            processImageProxy(imageProxy)
                        } else {
                            imageProxy.close()
                        }
                    })
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)

            } catch (exc: Exception) {
                Log.e("ScanActivity", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        hasScanned = true
                        processBarcode(barcodes.first()) // Process the first barcode
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ScanActivity", "Barcode scanning failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun processBarcode(barcode: Barcode) {
        rawValue = barcode.rawValue

        if (rawValue != null) {
            val item = itemWithQuantityList.find { it.item.itemSKU == rawValue }

            if (item != null) {
                toggleVisibility(
                    listOf(
                        binding.ivScanImage,
                        binding.tvScanProduct,
                        binding.etScanQty,
                        binding.ivScanAdd,
                        binding.ivScanMinus,
                        binding.tvScanPrice,
                        binding.tvScanStock,
                        binding.tvScanStockLabel,
                        binding.tvScanPesoLabel
                    ),
                    listOf(
                        binding.btnScanAddNew,
                        binding.tvScanAddNew
                    )
                )

                binding.ivScanImage.setImageBitmap(decodeBase64ToBitmap(item.item.imageUri))
                binding.tvScanProduct.text = item.item.name
                binding.tvScanPrice.text = item.item.price.toString()
                binding.tvScanStock.text = item.item.stock.toString()

            } else {
                Toast.makeText(this, "Item not found in the list.", Toast.LENGTH_SHORT).show()

                toggleVisibility(
                    listOf(
                        binding.btnScanAddNew,
                        binding.tvScanAddNew
                    ),
                    listOf(
                        binding.ivScanImage,
                        binding.tvScanProduct,
                        binding.etScanQty,
                        binding.ivScanAdd,
                        binding.ivScanMinus,
                        binding.tvScanPrice,
                        binding.tvScanStock,
                        binding.tvScanStockLabel,
                        binding.tvScanPesoLabel
                    )
                )
            }
        } else {
            Toast.makeText(this, "Invalid barcode scanned.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleVisibility(visibleViews: List<View>, goneViews: List<View>) {
        visibleViews.forEach { it.visibility = View.VISIBLE }
        goneViews.forEach { it.visibility = View.GONE }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
