package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s13.wang.jeremy.mobdevemco.adapter.PullOutAdapter
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.PullOutBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemWithQuantityList.Companion.itemWithQuantityList

class PullOutActivity : ComponentActivity() {
    private lateinit var binding: PullOutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PullOutBinding.inflate(layoutInflater)
        initUI()
        setContentView(binding.root)
    }


    private fun initUI() {
        binding.btnPullOut.isAllCaps = false
        binding.ivPullOutBack.setOnClickListener {
            finish()
        }
        binding.btnPullOut.setOnClickListener {
            finish()
        }
        binding.recyclerPullOut.layoutManager = LinearLayoutManager(this)
        val filteredList = itemWithQuantityList.filter { it.quantity > 0 }.toMutableList()
        binding.recyclerPullOut.adapter = PullOutAdapter(filteredList, this)
    }
}