package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.LoginBinding
import android.util.TypedValue
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s13.wang.jeremy.mobdevemco.R

class LoginActivity : ComponentActivity() {
    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 101
    }
    private lateinit var binding: LoginBinding
    private lateinit var auth: FirebaseAuth
    private var state: String = "login"
    private var passwordState: String = "hidden"
    private var confirmPasswordState: String = "hidden"
    private var fStore: FirebaseFirestore? = null
    private var documentReference: DocumentReference? = null
    private var user: Map<String, Any>? = null

    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginBinding.inflate(layoutInflater)
        fStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        if (!arePermissionsGranted()) {
            requestPermissions(requiredPermissions, PERMISSIONS_REQUEST_CODE)
        } else {
            initUI()
            setContentView(binding.root)
        }
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToHome()
        }
    }

    private fun arePermissionsGranted(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // All permissions granted, proceed with functionality
                    initUI()
                    setContentView(binding.root)
                } else {
                    // Handle permission denial (show a message, etc.)
                    Toast.makeText(this, "Permissions are required to proceed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initUI(){
        val text = binding.tvWelcome.text.toString()
        val spannableString = SpannableString(text)

        val startIndex = text.indexOf("IT")
        val endIndex = startIndex + 2
        val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.green))
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvWelcome.text = spannableString
        binding.btnLoginHeader.isAllCaps = false
        binding.btnSignUpHeader.isAllCaps = false
        binding.btnSubmit.isAllCaps = false

        binding.ivEyePassword.setOnClickListener{
            val currentCursorPosition = binding.etPassword.selectionStart
            if (passwordState=="hidden"){
                binding.ivEyePassword.setImageResource(R.drawable.closed_eye)
                binding.etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordState="visible"
            }
            else{
                binding.ivEyePassword.setImageResource(R.drawable.opened_eye)
                binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordState="hidden"
            }
            binding.etPassword.setSelection(currentCursorPosition)
        }
        binding.ivEyeConfirmPassword.setOnClickListener{
            val currentCursorPosition = binding.etPassword.selectionStart
            if (confirmPasswordState=="hidden"){
                binding.ivEyeConfirmPassword.setImageResource(R.drawable.closed_eye)
                binding.etConfirmPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                confirmPasswordState="visible"
            }
            else{
                binding.ivEyeConfirmPassword.setImageResource(R.drawable.opened_eye)
                binding.etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                confirmPasswordState="hidden"
            }
            binding.etConfirmPassword.setSelection(currentCursorPosition)
        }

        binding.btnSignUpHeader.setOnClickListener {
            binding.etUsername.text.clear()
            binding.etPassword.text.clear()
            binding.etConfirmPassword.text.clear()
            if (state=="login"){
                binding.btnSignUpHeader.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.btnLoginHeader.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.tvConfirmPasswordLabel.visibility = TextView.VISIBLE
                binding.etConfirmPassword.visibility = EditText.VISIBLE
                binding.ivEyeConfirmPassword.visibility = ImageView.VISIBLE
                val params = binding.tvConfirmPasswordLabel.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(params.leftMargin, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 100f, resources.displayMetrics).toInt(), params.rightMargin, params.bottomMargin)
                binding.tvConfirmPasswordLabel.layoutParams = params
                binding.btnLoginHeader.background = ContextCompat.getDrawable(this, R.drawable.inactive_button)
                binding.btnSignUpHeader.background = ContextCompat.getDrawable(this, R.drawable.active_button)
                state="signup"
                binding.btnSubmit.text = getString(R.string.signup_button)
            }
        }
        binding.btnLoginHeader.setOnClickListener {
            binding.etUsername.text.clear()
            binding.etPassword.text.clear()
            binding.etConfirmPassword.text.clear()
            if (state=="signup"){
                binding.btnSignUpHeader.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.btnLoginHeader.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.etConfirmPassword.text.clear()
                binding.tvConfirmPasswordLabel.visibility = TextView.INVISIBLE
                binding.etConfirmPassword.visibility = EditText.INVISIBLE
                binding.ivEyeConfirmPassword.visibility = ImageView.INVISIBLE
                val params = binding.tvConfirmPasswordLabel.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(params.leftMargin, 0, params.rightMargin, params.bottomMargin)
                binding.tvConfirmPasswordLabel.layoutParams = params
                binding.btnLoginHeader.background = ContextCompat.getDrawable(this, R.drawable.active_button)
                binding.btnSignUpHeader.background = ContextCompat.getDrawable(this, R.drawable.inactive_button)
                state="login"
                binding.btnSubmit.text = getString(R.string.login_button)
            }
        }

        binding.btnSubmit.setOnClickListener {
            val email = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (state == "login") {
                loginUser(email, password)
            } else {
                val confirmPassword = binding.etConfirmPassword.text.toString().trim()
                if (password == confirmPassword) {
                    registerUser(email, password)
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun loginUser(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                        // Redirect to HomeActivity
                        navigateToHome()
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerUser(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

                        // Initialize fStore if not done yet
                        fStore = FirebaseFirestore.getInstance()

                        documentReference = fStore!!.collection("users").document(auth.currentUser!!.uid)

                        user = hashMapOf(
                            "email" to email
                        )

                        documentReference!!.set(user!!).addOnSuccessListener {
                            Log.d("REGISTER", "onSuccess: user Profile is created for ${auth.currentUser!!.uid}")
                        }.addOnFailureListener {
                            Log.d("REGISTER", "onFailure: ${it.message}")
                        }

                        // Redirect to HomeActivity
                        navigateToHome()
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}