package com.mobdeve.s13.wang.jeremy.mobdevemco.model

// Data class for ItemDetails
data class ItemDetails(
    val itemID: String,
    val name: String,
    val price: Double,
    val stock: Int
)

// Data class for ItemLog
data class ItemLog(
    val item: ItemDetails,
    val quantity: Int
)
