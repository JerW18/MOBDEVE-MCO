package com.mobdeve.s13.wang.jeremy.mobdevemco.model

class ItemWithQuantity() {
    var item = Item()
    var quantity = 0
    constructor(item: Item, quantity: Int) : this() {
        this.item = item
        this.quantity = quantity
    }

}

