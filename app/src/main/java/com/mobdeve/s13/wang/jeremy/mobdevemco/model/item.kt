package com.mobdeve.s13.wang.jeremy.mobdevemco.model

class Item() {  // Explicit no-argument constructor
    var itemSKU: String = ""
    var imageUri: String? = null
    var name: String = ""
    var price: Float = 0f
    var stock: Int = 0
    // You can create a secondary constructor for easier initialization if needed
    constructor(itemSKU: String, imageUri: String?, name: String, price: Float, stock: Int) : this() {
        this.itemSKU = itemSKU
        this.imageUri = imageUri
        this.name = name
        this.price = price
        this.stock = stock
    }
}