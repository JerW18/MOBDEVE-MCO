package com.mobdeve.s13.wang.jeremy.mobdevemco.model

// Data class for ItemDetails
data class ItemDetails(
    val itemID: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val stock: Int = 0
)

// Data class for ItemLog
data class ItemLog(
    val item: ItemDetails = ItemDetails(),
    val quantity: Int = 0
)

