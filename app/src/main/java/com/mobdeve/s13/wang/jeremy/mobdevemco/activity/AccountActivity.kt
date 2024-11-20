package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.AcctDeletionConfirmationBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.AcctSettingBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.LogoutConfirmationBinding

class AccountActivity : ComponentActivity() {
    private lateinit var binding: AcctSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcctSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        // Handle back button
        binding.ivAcctBack.setOnClickListener {
            finish()
        }

        // Handle logout button
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        // Inflate the logout confirmation layout
        val dialogBinding = LogoutConfirmationBinding.inflate(LayoutInflater.from(this))

        // Create the dialog
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)

        // Set transparent background for the dialog
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Cancel button logic
        dialogBinding.btnLogoutCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Confirm Logout button logic
        dialogBinding.btnLogoutConfirm.setOnClickListener {
            dialog.dismiss()
            performLogout()
        }

        // Show the dialog
        dialog.show()

    }

    private fun performLogout() {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        Log.d("AccountActivity", "User logged out")
        finish()
    }

    private fun showDeleteConfirmationDialog() {
        // Inflate the logout confirmation layout
        val dialogBinding = AcctDeletionConfirmationBinding.inflate(LayoutInflater.from(this))

        // Create the dialog
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)

        // Set transparent background for the dialog
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Cancel button logic
        dialogBinding.btnDeletionCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Confirm Logout button logic
        dialogBinding.btnDeletionConfirm.setOnClickListener {
            dialog.dismiss()
            performDeletion()
        }

        // Show the dialog
        dialog.show()
    }

    private fun performDeletion() {

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            // Delete user data from Firestore
            deleteUserDataFromFirestore(userId) { success ->
                if (success) {
                    // Delete user from Firebase Authentication only after Firestore data is deleted
                    currentUser.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("Deletion", "Account and data deleted successfully")
                                Toast.makeText(this, "Account and data deleted successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.e("Deletion", "Failed to delete account: ${task.exception?.message}")
                                Toast.makeText(
                                    this,
                                    "Failed to delete account: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            finish()
                        }
                } else {
                    Log.e("Deletion", "Failed to delete user data from Firestore")
                    Toast.makeText(this, "Failed to delete user data from Firestore", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.e("Deletion", "No user is currently signed in")
            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show()
        }
    }


    private fun deleteUserDataFromFirestore(userId: String, onComplete: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Reference to the user's main document
        val userDocRef = db.collection("users").document(userId)

        // Reference to the 'items' subcollection
        val itemsCollectionRef = userDocRef.collection("items")

        // First, delete all documents in the 'items' subcollection
        itemsCollectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                val batch = db.batch()

                for (document in querySnapshot.documents) {
                    batch.delete(document.reference) // Add each document to the batch
                }

                // Commit the batch to delete all 'items'
                batch.commit()
                    .addOnSuccessListener {
                        // Once 'items' are deleted, delete the user's main document
                        userDocRef.delete()
                            .addOnSuccessListener {
                                Log.d("DeleteFirestore", "User Firestore data deleted successfully")
                                onComplete(true)  // Firestore deletion succeeded
                            }
                            .addOnFailureListener { e ->
                                Log.e("DeleteFirestore", "Failed to delete user document: ${e.message}")
                                onComplete(false)  // Firestore deletion failed
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("DeleteFirestore", "Failed to delete items: ${e.message}")
                        onComplete(false)  // Failed to delete items
                    }
            }
            .addOnFailureListener { e ->
                Log.e("DeleteFirestore", "Failed to retrieve items: ${e.message}")
                onComplete(false)  // Failed to retrieve items
            }
    }



}
