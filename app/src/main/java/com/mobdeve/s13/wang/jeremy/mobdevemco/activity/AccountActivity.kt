package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
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
        // Add logout logic here (e.g., clear user session, redirect to login screen)
        finish() // Example: Close the activity
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
        // Add account deletion logic here (e.g., clear user session, redirect to login screen)
        finish() // Example: Close the activity
    }
}
