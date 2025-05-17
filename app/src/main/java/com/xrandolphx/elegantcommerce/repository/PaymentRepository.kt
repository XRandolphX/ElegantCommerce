package com.xrandolphx.elegantcommerce.repository

import android.util.Log
import com.xrandolphx.elegant_commerce.util.Resource
import com.xrandolphx.elegantcommerce.data.PaymentRequest
import com.xrandolphx.elegantcommerce.data.PaymentResponse
import com.xrandolphx.elegantcommerce.network.PaymentService
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PaymentRepository @Inject constructor(private val paymentService: PaymentService) {
    fun createPaymentPreference(
        products: List<com.xrandolphx.elegantcommerce.data.CartProduct>,
        totalPrice: Float,
        address: com.xrandolphx.elegantcommerce.data.Address,
        userEmail: String
    ): Flow<Resource<PaymentResponse>> = flow {
        Log.d("PaymentRepository", "Iniciando creación de preferencia de pago ...")
        emit(Resource.Loading())
        try {
            val request = PaymentRequest(
                products = products,
                totalPrice = totalPrice,
                address = address,
                userEmail = userEmail
            )
            Log.d("PaymentRepository", "Enviando request: $request")
            val response = paymentService.createPayment(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Log.d("PaymentRepository", "Respuesta exitosa: $it")
                    emit(Resource.Success(it))
                } ?: run {
                    Log.e("PaymentRepository", "Respuesta vacía del servidor")
                    emit(Resource.Error("Respuesta vacía del servidor"))
                }
            } else {
                Log.e("PaymentRepository", "Error en respuesta: ${response.code()}")
                emit(Resource.Error("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PaymentRepository", "Error al crear la preferencia de pago", e)
            emit(Resource.Error("Excepción: ${e.message}"))
        }
    }
}