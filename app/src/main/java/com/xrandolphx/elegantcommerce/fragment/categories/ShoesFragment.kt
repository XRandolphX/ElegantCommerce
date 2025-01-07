package com.xrandolphx.elegantcommerce.fragment.categories

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.xrandolphx.elegant_commerce.util.Resource
import com.xrandolphx.elegantcommerce.R
import com.xrandolphx.elegantcommerce.data.Category
import com.xrandolphx.elegantcommerce.viewmodel.CategoryViewModel
import com.xrandolphx.elegantcommerce.viewmodel.factory.BaseCategoryViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShoesFragment : BaseCategoryFragment() {
    @Inject
    lateinit var firestore: FirebaseFirestore

    val viewModel by viewModels<CategoryViewModel> {
        BaseCategoryViewModelFactory(firestore, Category.zapatos)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración de SwipeRefreshLayout para pull-to-refresh
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayoutBaseCat)
        swipeRefreshLayout.setOnRefreshListener {
            // Llamar a las funciones del ViewModel para recargar los productos
            viewModel.fetchOfferProducts()
            viewModel.fetchBestProducts()
            // Detener la animación de refresco después de cargar los datos
            swipeRefreshLayout.isRefreshing = false
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.offerProducts.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            showOfferLoading()
                        }

                        is Resource.Success -> {
                            offerAdapter.differ.submitList(it.data)
                            hideOfferLoading()
                        }

                        is Resource.Error -> {
                            Snackbar.make(
                                requireView(),
                                it.message.toString(),
                                Snackbar.LENGTH_LONG
                            ).show()
                            hideOfferLoading()
                            Log.d("test", it.message.toString())

                        }

                        else -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bestProducts.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            showBestProductsLoading()
                        }

                        is Resource.Success -> {
                            bestProductAdapter.differ.submitList(it.data)
                            hideBestProductsLoading()
                        }

                        is Resource.Error -> {
                            Snackbar.make(
                                requireView(),
                                it.message.toString(),
                                Snackbar.LENGTH_LONG
                            ).show()
                            hideBestProductsLoading()
                        }

                        else -> Unit
                    }
                }
            }
        }

    }

    override fun onBestProductsPagingRequest() {
        super.onBestProductsPagingRequest()
    }

    override fun onOfferPagingRequest() {
        super.onOfferPagingRequest()
    }
}