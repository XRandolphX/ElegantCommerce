package com.xrandolphx.elegantcommerce.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xrandolphx.elegant_commerce.util.Resource
import com.xrandolphx.elegantcommerce.data.Address
import com.xrandolphx.elegantcommerce.data.CartProduct
import com.xrandolphx.elegantcommerce.data.PaymentResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.xrandolphx.elegantcommerce.repository.PaymentRepository

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val paymentRepository: PaymentRepository
) : ViewModel() {
    // Gestión de direcciones
    private val _address = MutableStateFlow<Resource<List<Address>>>(Resource.Unspecified())
    val address = _address.asStateFlow()

    init {
        getUserAddresses()
    }

    fun getUserAddresses() {
        viewModelScope.launch { _address.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("address")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    viewModelScope.launch { _address.emit(Resource.Error(error.message.toString())) }
                    return@addSnapshotListener
                }
                val addresses = value?.toObjects(Address::class.java)
                viewModelScope.launch { _address.emit(Resource.Success(addresses!!)) }
            }
    }

    // Gestión del pago: preferencia y obtención del init_point
    private val _paymentPreference =
        MutableStateFlow<Resource<PaymentResponse>>(Resource.Unspecified())
    val paymentPreference = _paymentPreference.asStateFlow()

    fun createPaymentPreference(
        products: List<CartProduct>,
        totalprice: Float,
        address: Address
    ) {
        viewModelScope.launch {
            paymentRepository.createPaymentPreference(products, totalprice, address)
                .collect { resource ->
                    _paymentPreference.value = resource
                }
        }
    }

}