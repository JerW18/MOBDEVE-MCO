package com.mobdeve.s13.wang.jeremy.mobdevemco.helper

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

// Helper class to handle Base64 conversions
class Base64Converter {

    companion object {

        // Function to convert Bitmap to Base64 string
        fun convertBitmapToBase64(bitmap: Bitmap): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)  // Compress to JPEG (quality 100)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }

        // Function to convert Uri to ByteArray
            fun uriToBase64(context: Context, uri: Uri): String {
            try {
                // Open input stream and convert it to byte array
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.use {
                    return Base64.encodeToString(it.readBytes(), Base64.DEFAULT)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }
    }
}
