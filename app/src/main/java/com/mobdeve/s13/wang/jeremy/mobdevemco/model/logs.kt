package com.mobdeve.s13.wang.jeremy.mobdevemco.model

class Logs() {
    var date: String = ""
    var items: List<ItemWithQuantity> = mutableListOf()
    var total : Double = 0.0
    var type: String = ""

    constructor(date: String, items: List<ItemWithQuantity>, total: Double, type: String) : this() {
        this.date = date
        this.items = items
        this.total = total
        this.type = type
    }
}


