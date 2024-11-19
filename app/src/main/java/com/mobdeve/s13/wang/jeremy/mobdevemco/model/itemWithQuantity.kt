package com.mobdeve.s13.wang.jeremy.mobdevemco.model

import android.os.Parcel
import android.os.Parcelable

data class ItemWithQuantity(val item: Item, var quantity: Int) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Item::class.java.classLoader)!!, // Read the Item object
        parcel.readInt() // Read the quantity
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(item, flags) // Write the Item object
        parcel.writeInt(quantity) // Write the quantity
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemWithQuantity> {
        override fun createFromParcel(parcel: Parcel): ItemWithQuantity {
            return ItemWithQuantity(parcel)
        }

        override fun newArray(size: Int): Array<ItemWithQuantity?> {
            return arrayOfNulls(size)
        }
    }
}