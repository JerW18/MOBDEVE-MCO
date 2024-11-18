package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s13.wang.jeremy.mobdevemco.R
import com.mobdeve.s13.wang.jeremy.mobdevemco.adapter.PullOutAdapter
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.PullOutBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item

class PullOutActivity: ComponentActivity() {
    private lateinit var binding: PullOutBinding
    private val itemList = mutableListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PullOutBinding.inflate(layoutInflater)
        initUI()
        setContentView(binding.root)
    }


    private fun initUI(){
        binding.btnPullOut.isAllCaps = false
        binding.ivPullOutBack.setOnClickListener {
            finish()
        }
        binding.btnPullOut.setOnClickListener {
            finish()
        }
        binding.recyclerPullOut.layoutManager = LinearLayoutManager(this)
        binding.recyclerPullOut.adapter = PullOutAdapter(itemList)

    }
}