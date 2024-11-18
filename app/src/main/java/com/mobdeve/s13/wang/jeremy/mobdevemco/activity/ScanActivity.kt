package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.ScanBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanActivity : ComponentActivity() {
    private lateinit var binding: ScanBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var barcodeScanner: BarcodeScanner

    private var hasScanned = false // Flag to prevent further scanning

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ScanBinding.inflate(layoutInflater)
        initUI()
        setContentView(binding.root)
        initCamera()
        barcodeScanner = BarcodeScanning.getClient()
    }

    private fun initUI() {
        binding.btnScanPullOut.isAllCaps = false
        binding.btnScanAgain.isAllCaps = false

        binding.btnScanAgain.setOnClickListener {
            hasScanned = false
        }
        binding.btnScanPullOut.setOnClickListener {
            finish()
        }

        binding.ivScanMinus.setOnClickListener {
            if (binding.etScanQty.text.toString().toInt() > 1) {
                binding.etScanQty.setText((binding.etScanQty.text.toString().toInt() - 1).toString())
            }
        }
        binding.ivScanAdd.setOnClickListener {
            binding.etScanQty.setText((binding.etScanQty.text.toString().toInt() + 1).toString())
        }
    }

    private fun initCamera() {
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
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
                        if (!hasScanned) { // Only process if scanning is still active
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
                        hasScanned = true // Stop further scanning
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
        val rawValue = barcode.rawValue
        Toast.makeText(this, "Scanned barcode: $rawValue", Toast.LENGTH_SHORT).show()
        // You can perform additional actions with the barcode data, like opening a URL or adding it to a list
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
