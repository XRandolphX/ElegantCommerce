package com.xrandolphx.elegantcommerce.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.xrandolphx.elegant_commerce.util.Resource
import com.xrandolphx.elegantcommerce.data.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllProductsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _allProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val allProducts: StateFlow<Resource<List<Product>>> = _allProducts

    private val pagingInfo = PagingInfo()

    init {
        fetchAllProducts()
    }

    fun fetchAllProducts() {
        if (!pagingInfo.isPagingEnd) {
            viewModelScope.launch {
                _allProducts.emit(Resource.Loading())
            }
            firestore
                .collection("Products").limit(pagingInfo.bestProductPage * 10).get()
                .addOnSuccessListener { result ->
                    val bestProductsList = result.toObjects(Product::class.java)
                    pagingInfo.isPagingEnd = bestProductsList == pagingInfo.oldBestProducts
                    pagingInfo.oldBestProducts = bestProductsList
                    viewModelScope.launch {
                        _allProducts.emit(Resource.Success(bestProductsList))
                    }
                    pagingInfo.bestProductPage++
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _allProducts.emit(Resource.Error(it.message.toString()))
                    }
                }
        }
    }

    internal data class PagingInfo(
        var bestProductPage: Long = 1,
        var oldBestProducts: List<Product> = emptyList(),
        var isPagingEnd: Boolean = false
    )
}