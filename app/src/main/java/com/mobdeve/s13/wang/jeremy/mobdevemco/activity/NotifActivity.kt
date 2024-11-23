package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s13.wang.jeremy.mobdevemco.adapter.NotifAdapter
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.NotifBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.list.itemList.Companion.itemList
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item

class NotifActivity : ComponentActivity() {
    private lateinit var binding: NotifBinding
    private var notifList = mutableListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NotifBinding.inflate(layoutInflater)
        initData()
        initUI()
        setContentView(binding.root)
    }

    private fun initData(){
        notifList = itemList.filter { it.stock <= it.restock }.toMutableList()
    }


    private fun initUI() {
        binding.ivNotifBack.setOnClickListener {
            finish()
        }
        binding.recyclerNotif.layoutManager = LinearLayoutManager(this)
        binding.recyclerNotif.adapter = NotifAdapter(notifList)
        val dividerItemDecoration =
            DividerItemDecoration(binding.recyclerNotif.context, LinearLayoutManager.VERTICAL)
        binding.recyclerNotif.addItemDecoration(dividerItemDecoration)

    }
}