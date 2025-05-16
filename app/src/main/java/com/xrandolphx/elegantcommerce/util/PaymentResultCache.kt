package com.xrandolphx.elegantcommerce.util

import com.xrandolphx.elegantcommerce.data.Address
import com.xrandolphx.elegantcommerce.data.CartProduct

/**
 * Clase para almacenar temporalmente los datos del pedido mientras se procesa el pago
 * y recuperarlos cuando regresamos del proceso de pago de MercadoPago
 */
object PaymentResultCache {

    // Datos de la orden
    private var cachedProducts: List<CartProduct>? = null
    private var cachedTotalPrice: Float = 0f
    private var cachedAddress: Address? = null

    // Clase de datos para encapsular todos los datos del pedido
    data class OrderData(
        val products: List<CartProduct>,
        val totalPrice: Float,
        val address: Address
    )

    /**
     * Guarda los datos del pedido en el cache
     */
    fun saveOrderData(products: List<CartProduct>, totalPrice: Float, address: Address) {
        cachedProducts = products
        cachedTotalPrice = totalPrice
        cachedAddress = address
    }

    /**
     * Recupera los datos del pedido desde el cache
     * @return OrderData o null si no hay datos en cache
     */
    fun getLastOrderData(): OrderData? {
        val products = cachedProducts
        val address = cachedAddress

        return if (products != null && address != null) {
            OrderData(products, cachedTotalPrice, address)
        } else {
            null
        }
    }

    /**
     * Limpia los datos del cache
     */
    fun clearLastOrderData() {
        cachedProducts = null
        cachedTotalPrice = 0f
        cachedAddress = null
    }
}
