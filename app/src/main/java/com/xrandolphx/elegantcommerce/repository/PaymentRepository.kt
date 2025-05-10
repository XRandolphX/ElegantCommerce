package com.xrandolphx.elegantcommerce.repository

import com.xrandolphx.elegant_commerce.util.Resource
import com.xrandolphx.elegantcommerce.data.PaymentRequest
import com.xrandolphx.elegantcommerce.data.PaymentResponse
import com.xrandolphx.elegantcommerce.network.PaymentService
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow

class PaymentRepository(private val paymentService: PaymentService) {
    fun createPaymentPreference(
        products: List<com.xrandolphx.elegantcommerce.data.CartProduct>,
        totalPrice: Float,
        address: com.xrandolphx.elegantcommerce.data.Address
    ): Flow<Resource<PaymentResponse>> = flow {
        emit(Resource.Loading())
        try {
            val request = PaymentRequest(
                products = products,
                totalPrice = totalPrice,
                address = address
            )
            val response = paymentService.createPayment(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Respuesta vacía del servidor"))

            } else {
                emit(Resource.Error("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Excepción: ${e.message}"))
        }
    }
}