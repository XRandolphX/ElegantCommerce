package com.xrandolphx.elegantcommerce.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    val fullName: String,
    val location: String,
    val addressBill: String,
    val phone: String,
) : Parcelable {
    constructor() : this("", "", "", "")
}