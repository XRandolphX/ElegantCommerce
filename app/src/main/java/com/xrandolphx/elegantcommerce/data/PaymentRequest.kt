package com.xrandolphx.elegantcommerce.data

data class PaymentRequest (
    val products: List<CartProduct>,
    val totalPrice: Float,
    val address: Address,
    val userEmail: String
)