package com.mobdeve.s13.wang.jeremy.mobdevemco.model

data class Items(
    val itemID: Int,
    val quantity: Int
)

class Log(
    val logID: Int,
    val items: List<Items>,
    val total: Float,
    val date: String
)