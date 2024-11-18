package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
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
import com.mobdeve.s13.wang.jeremy.mobdevemco.R

class LoginActivity : ComponentActivity() {
    private lateinit var binding: LoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var state: String
    private lateinit var passwordState: String
    private lateinit var confirmPasswordState: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        initUI()
        setContentView(binding.root)
    }

    private fun initUI(){
        state="login"
        passwordState="hidden"
        confirmPasswordState="hidden"
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
                binding.btnSubmit.text = "Sign Up"
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
                binding.btnSubmit.text = "Log In"
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
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
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

                        // Redirect to HomeActivity
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
        }
    }
}