

package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s13.wang.jeremy.mobdevemco.R
import com.mobdeve.s13.wang.jeremy.mobdevemco.adapter.EditAdapter
import com.mobdeve.s13.wang.jeremy.mobdevemco.adapter.LogsAdapter
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.ProductSettingBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.Item

class ProductSettingsActivity: ComponentActivity() {
    private lateinit var binding: ProductSettingBinding
    private val numList = mutableListOf<Int>()
    private val itemList = mutableListOf<Item>()
    private var state = "ADD"

    override fun onCreate(savedInstanceState: Bundle?) {
        for (i in 0 until 11) {
            itemList.add(
                Item(
                    imageId = R.drawable.image1,
                    name = "Item${i + 1}",
                    price = 100.55f,
                    stock = 10
                )
            )
        }
        super.onCreate(savedInstanceState)
        for (i in 0 until 11) {
            numList.add(1)
        }
        binding = ProductSettingBinding.inflate(layoutInflater)
        initUI()
        setContentView(binding.root)
    }


    private fun initUI() {
        binding.btnPSAdd.isAllCaps = false
        binding.btnPSEdit.isAllCaps = false
        binding.btnPSLogs.isAllCaps = false
        binding.btnSaveProduct.isAllCaps = false
        binding.recyclerLogs.layoutManager = LinearLayoutManager(this)
        binding.recyclerLogs.adapter = LogsAdapter(numList)
        val dividerItemDecoration =
            DividerItemDecoration(binding.recyclerLogs.context, LinearLayoutManager.VERTICAL)
        binding.recyclerLogs.addItemDecoration(dividerItemDecoration)

        binding.recyclerEdit.layoutManager = LinearLayoutManager(this)
        binding.recyclerEdit.adapter = EditAdapter(itemList)

        binding.ivPSBack.setOnClickListener {
            finish()
        }

        fun setButtonState(button: Button, isActive: Boolean) {
            button.setBackgroundResource(if (isActive) R.drawable.active_button else R.drawable.inactive_button)
            button.setTextColor(ContextCompat.getColor(this, if (isActive) R.color.white else R.color.black))
        }

        fun toggleVisibility(visibleViews: List<View>, goneViews: List<View>) {
            visibleViews.forEach { it.visibility = View.VISIBLE }
            goneViews.forEach { it.visibility = View.GONE }
        }
        binding.btnSaveProduct.setOnClickListener {
            finish()
        }
        binding.btnPSAdd.setOnClickListener {
            if (state != "ADD") {
                setButtonState(binding.btnPSAdd, true)
                toggleVisibility(
                    listOf(binding.tvPSProductNameLabel, binding.tvPSPriceLabel, binding.tvPSQtyLabel, binding.tvPSImageLabel, binding.etPSProductNumber, binding.etPSPrice, binding.etPSQty, binding.etPSImage, binding.btnSaveProduct),
                    emptyList()
                )

                when (state) {
                    "EDIT" -> {
                        setButtonState(binding.btnPSEdit, false)
                        binding.recyclerEdit.visibility = View.GONE
                    }
                    "LOGS" -> {
                        setButtonState(binding.btnPSLogs, false)
                        toggleVisibility(emptyList(), listOf(binding.recyclerLogs, binding.tvPSDateAndTime, binding.tvPSInOut, binding.tvPSQty, binding.tvPSProduct))
                    }
                }
                state = "ADD"
            }
        }

        binding.btnPSEdit.setOnClickListener {
            if (state != "EDIT") {
                binding.recyclerEdit.visibility = View.VISIBLE
                setButtonState(binding.btnPSEdit, true)

                when (state) {
                    "ADD" -> {
                        setButtonState(binding.btnPSAdd, false)
                        toggleVisibility(emptyList(), listOf(binding.tvPSProductNameLabel, binding.tvPSPriceLabel, binding.tvPSQtyLabel, binding.tvPSImageLabel, binding.etPSProductNumber, binding.etPSPrice, binding.etPSQty, binding.etPSImage, binding.btnSaveProduct))
                    }
                    "LOGS" -> {
                        setButtonState(binding.btnPSLogs, false)
                        toggleVisibility(emptyList(), listOf(binding.recyclerLogs, binding.tvPSDateAndTime, binding.tvPSInOut, binding.tvPSQty, binding.tvPSProduct))
                    }
                }
                state = "EDIT"
            }
        }

        binding.btnPSLogs.setOnClickListener {
            if (state != "LOGS") {
                toggleVisibility(
                    listOf(binding.recyclerLogs, binding.tvPSDateAndTime, binding.tvPSInOut, binding.tvPSQty, binding.tvPSProduct),
                    emptyList()
                )
                setButtonState(binding.btnPSLogs, true)

                when (state) {
                    "ADD" -> {
                        setButtonState(binding.btnPSAdd, false)
                        toggleVisibility(emptyList(), listOf(binding.tvPSProductNameLabel, binding.tvPSPriceLabel, binding.tvPSQtyLabel, binding.tvPSImageLabel, binding.etPSProductNumber, binding.etPSPrice, binding.etPSQty, binding.etPSImage, binding.btnSaveProduct))
                    }
                    "EDIT" -> {
                        setButtonState(binding.btnPSEdit, false)
                        binding.recyclerEdit.visibility = View.GONE
                    }
                }
                state = "LOGS"
            }
        }

    }
}