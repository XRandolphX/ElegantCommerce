package com.xrandolphx.elegantcommerce.di

import com.xrandolphx.elegantcommerce.network.PaymentService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    //    private const val BASE_URL = "http://10.0.2.2:3000/api/payments/"
    private const val BASE_URL = "https://mercadopago-nodejs-production.up.railway.app/api/payments"

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Singleton
    @Provides
    fun providePaymentService(retrofit: Retrofit): PaymentService =
        retrofit.create(PaymentService::class.java)
}