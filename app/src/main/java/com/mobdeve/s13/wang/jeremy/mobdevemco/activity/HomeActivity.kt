package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.mobdeve.s13.wang.jeremy.mobdevemco.R
import com.mobdeve.s13.wang.jeremy.mobdevemco.adapter.HomeAdapter
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.HomeBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item

class HomeActivity: ComponentActivity() {
    private lateinit var binding: HomeBinding
    private val itemList = mutableListOf<Item>()
    override fun onCreate(savedInstanceState: Bundle?) {
        for (i in 0 until 11) {
            itemList.add(Item(itemSKU = 1, imageUri = R.drawable.image1.toString(), name = "Item${i + 1}", price = 100.55f, stock = 10))
        }

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
        binding.btnReview.isAllCaps = false
        binding.recyclerHome.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerHome.adapter = HomeAdapter(itemList)

        binding.btnReview.setOnClickListener{
            val intent = Intent(this, PullOutActivity::class.java)
            startActivity(intent)
        }

        binding.ivNotif.setOnClickListener{
            val intent = Intent(this, NotifActivity::class.java)
            startActivity(intent)
        }
        binding.ivSetting.setOnClickListener{
            val intent = Intent(this, ProductSettingsActivity::class.java)
            startActivity(intent)
        }

        binding.ivScan.setOnClickListener{
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }

    }
}