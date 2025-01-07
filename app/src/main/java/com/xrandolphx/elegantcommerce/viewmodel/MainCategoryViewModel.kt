package com.xrandolphx.elegantcommerce.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import com.xrandolphx.elegant_commerce.util.Resource
import com.xrandolphx.elegantcommerce.data.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainCategoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _popularProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val popularProducts: StateFlow<Resource<List<Product>>> = _popularProducts

    private val _offerProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val offerProducts: StateFlow<Resource<List<Product>>> = _offerProducts

    private val _bestProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestProducts: StateFlow<Resource<List<Product>>> = _bestProducts

    private val pagingInfo = PagingInfo()

    init {
        fetchPopularProducts()
        fetchOfferProducts()
        fetchBestProducts()
    }

    fun fetchPopularProducts() {
        viewModelScope.launch {
            _popularProducts.emit(Resource.Loading())
        }
        firestore
            .collection("Products")
            .whereEqualTo("category", "popular").get()
            .addOnSuccessListener { result ->
                val popularProductsList = result.toObjects(Product::class.java)
                viewModelScope.launch {
                    _popularProducts.emit(Resource.Success(popularProductsList))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _popularProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    fun fetchOfferProducts() {
        viewModelScope.launch {
            _offerProducts.emit(Resource.Loading())
        }
        firestore
            .collection("Products")
            .whereEqualTo("category", "oferta").get()
            .addOnSuccessListener { result ->
                val offerProductsList = result.toObjects(Product::class.java)
                viewModelScope.launch {
                    _offerProducts.emit(Resource.Success(offerProductsList))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _offerProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    fun fetchBestProducts() {
        if (!pagingInfo.isPagingEnd) {
            viewModelScope.launch {
                _bestProducts.emit(Resource.Loading())
            }
            firestore
                .collection("Products").limit(pagingInfo.bestProductPage * 10).get()
                .addOnSuccessListener { result ->
                    val bestProductsList = result.toObjects(Product::class.java)
                    pagingInfo.isPagingEnd = bestProductsList == pagingInfo.oldBestProducts
                    pagingInfo.oldBestProducts = bestProductsList
                    viewModelScope.launch {
                        _bestProducts.emit(Resource.Success(bestProductsList))
                    }
                    pagingInfo.bestProductPage++
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _bestProducts.emit(Resource.Error(it.message.toString()))
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