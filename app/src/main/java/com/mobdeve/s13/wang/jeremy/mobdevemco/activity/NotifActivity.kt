package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s13.wang.jeremy.mobdevemco.adapter.NotifAdapter
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.NotifBinding

class NotifActivity: ComponentActivity() {
    private lateinit var binding: NotifBinding
    private val itemList = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for (i in 0 until 11) {
            itemList.add(1)
        }
        binding = NotifBinding.inflate(layoutInflater)
        initUI()
        setContentView(binding.root)
    }


    private fun initUI(){
        binding.ivNotifBack.setOnClickListener {
            finish()
        }
        binding.recyclerNotif.layoutManager = LinearLayoutManager(this)
        binding.recyclerNotif.adapter = NotifAdapter(itemList)
        val dividerItemDecoration = DividerItemDecoration(binding.recyclerNotif.context, LinearLayoutManager.VERTICAL)
        binding.recyclerNotif.addItemDecoration(dividerItemDecoration)

    }
}