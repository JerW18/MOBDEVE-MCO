package com.mobdeve.s13.wang.jeremy.mobdevemco.model

import android.os.Parcel
import android.os.Parcelable

data class Item(
    var itemSKU: String = "",
    var imageUri: String? = null,
    var name: String = "",
    var price: Float = 0f,
    var stock: Int = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "", // itemSKU
        parcel.readString(), // imageUri
        parcel.readString() ?: "", // name
        parcel.readFloat(), // price
        parcel.readInt() // stock
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(itemSKU) // itemSKU
        parcel.writeString(imageUri) // imageUri
        parcel.writeString(name) // name
        parcel.writeFloat(price) // price
        parcel.writeInt(stock) // stock
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Item> {
        override fun createFromParcel(parcel: Parcel): Item {
            return Item(parcel)
        }

        override fun newArray(size: Int): Array<Item?> {
            return arrayOfNulls(size)
        }
    }
}
