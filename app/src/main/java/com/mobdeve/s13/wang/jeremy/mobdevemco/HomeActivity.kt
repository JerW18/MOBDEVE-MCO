package com.mobdeve.s13.wang.jeremy.mobdevemco

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.HomeBinding

class HomeActivity: ComponentActivity() {
    private lateinit var binding: HomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeBinding.inflate(layoutInflater)
        initUI()
        setContentView(binding.root)
    }

    private fun initUI(){
        val text = binding.tvAppName.text.toString()
        val spannableString = SpannableString(text)

        val startIndex = text.indexOf("IT")
        val endIndex = startIndex + 2
        val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.dark_green))
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvAppName.text = spannableString
    }
}