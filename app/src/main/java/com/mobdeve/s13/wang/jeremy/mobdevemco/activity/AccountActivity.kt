package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s13.wang.jeremy.mobdevemco.adapter.NotifAdapter
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.AcctSettingBinding

class AccountActivity: ComponentActivity() {
    private lateinit var binding: AcctSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcctSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }
    private fun initUI() {
        binding.ivAcctBack.setOnClickListener {
            finish()
        }
    }
}