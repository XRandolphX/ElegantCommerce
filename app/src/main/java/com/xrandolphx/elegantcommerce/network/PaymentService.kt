package com.xrandolphx.elegantcommerce.network

import com.xrandolphx.elegantcommerce.data.PaymentRequest
import com.xrandolphx.elegantcommerce.data.PaymentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentService {
    @POST("create")
    suspend fun createPayment(@Body request: PaymentRequest): Response<PaymentResponse>
}