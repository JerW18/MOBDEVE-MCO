package com.mobdeve.s13.wang.jeremy.mobdevemco.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s13.wang.jeremy.mobdevemco.adapter.PullOutAdapter
import com.mobdeve.s13.wang.jeremy.mobdevemco.databinding.PullOutBinding
import com.mobdeve.s13.wang.jeremy.mobdevemco.model.ItemWithQuantity

class PullOutActivity : ComponentActivity() {
    private lateinit var binding: PullOutBinding
    private val itemList = mutableListOf<ItemWithQuantity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PullOutBinding.inflate(layoutInflater)

        val itemListWithQuantity: ArrayList<ItemWithQuantity>? = intent.getParcelableArrayListExtra("item_list")

        itemListWithQuantity?.let {
            itemList.addAll(it)
        }

        initUI()
        setContentView(binding.root)
    }


    private fun initUI() {
        binding.btnPullOut.isAllCaps = false
        binding.ivPullOutBack.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putParcelableArrayListExtra("updated_item_list", ArrayList(itemList))
            setResult(RESULT_OK, resultIntent)
            finish()
        }
        binding.btnPullOut.setOnClickListener {
            finish()
        }
        binding.recyclerPullOut.layoutManager = LinearLayoutManager(this)
        binding.recyclerPullOut.adapter = PullOutAdapter(itemList)
    }
}