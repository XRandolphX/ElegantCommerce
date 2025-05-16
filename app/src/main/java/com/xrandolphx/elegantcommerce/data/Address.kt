package com.xrandolphx.elegantcommerce.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    val fullName: String,
    val location: String,
    val addressBill: String,
    val phone: String,
    // Adicionales para Mercado Pago
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
) : Parcelable {
    constructor() : this("", "", "", "", "", "", "" )
}