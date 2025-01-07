package com.xrandolphx.elegantcommerce.fragment.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.xrandolphx.elegant_commerce.util.Resource
import com.xrandolphx.elegantcommerce.R
import com.xrandolphx.elegantcommerce.adapter.BestProductAdapter
import com.xrandolphx.elegantcommerce.adapter.OfferProductAdapter
import com.xrandolphx.elegantcommerce.adapter.PopularProductAdapter
import com.xrandolphx.elegantcommerce.databinding.FragmentMainCategoryBinding
import com.xrandolphx.elegantcommerce.util.showBottomNavigationView
import com.xrandolphx.elegantcommerce.viewmodel.MainCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val TAG = "MainCategoryFragment"

@AndroidEntryPoint
class MainCategoryFragment : Fragment(R.layout.fragment_main_category) {
    private lateinit var binding: FragmentMainCategoryBinding
    private lateinit var popularProductAdapter: PopularProductAdapter
    private lateinit var offerProductAdapter: OfferProductAdapter
    private lateinit var bestProductAdapter: BestProductAdapter
    private val viewModel by viewModels<MainCategoryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainCategoryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPopularProductRv()
        setupOfferProductRv()
        setupBestProductRv()

        popularProductAdapter.onClick = { product ->
            val b = Bundle().apply { putParcelable("product", product) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailFragment, b)
        }

        offerProductAdapter.onClick = { product ->
            val b = Bundle().apply { putParcelable("product", product) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailFragment, b)
        }

        bestProductAdapter.onClick = { product ->
            val b = Bundle().apply { putParcelable("product", product) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailFragment, b)
        }


        // Configuración del SwipeRefreshLayout
        binding.swipeRefreshLayoutMainCat.setOnRefreshListener {
            // Llamada a las funciones para recargar los datos
            viewModel.fetchPopularProducts()
            viewModel.fetchOfferProducts()
            viewModel.fetchBestProducts()

            // Luego de cargar los datos, se detiene la animación de refresco
            binding.swipeRefreshLayoutMainCat.isRefreshing = false
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.popularProducts.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            showLoading()
                        }

                        is Resource.Success -> {
                            popularProductAdapter.differ.submitList(it.data)
                            hideLoading()
                        }

                        is Resource.Error -> {
                            hideLoading()
                            Log.e(TAG, it.message.toString())
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.offerProducts.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            showLoading()
                        }

                        is Resource.Success -> {
                            offerProductAdapter.differ.submitList(it.data)
                            hideLoading()
                        }

                        is Resource.Error -> {
                            hideLoading()
                            Log.e(TAG, it.message.toString())
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
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
                            binding.progressBarProducts.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            bestProductAdapter.differ.submitList(it.data)
                            binding.progressBarProducts.visibility = View.GONE
                        }

                        is Resource.Error -> {
                            binding.progressBarProducts.visibility = View.GONE
                            Log.e(TAG, it.message.toString())
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        binding.nestedScrollMainCategory.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (v.getChildAt(0).bottom <= v.height + scrollY) {
                viewModel.fetchBestProducts()
            }
        })

    }

    private fun setupOfferProductRv() {
        offerProductAdapter = OfferProductAdapter()
        binding.rvOfferProduct.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = offerProductAdapter

            // Se añade el Item Decoration
            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_between_items)
            addItemDecoration(HorizontalSpaceItemDecoration(spacingInPixels))
        }
    }


    private fun setupPopularProductRv() {
        popularProductAdapter = PopularProductAdapter()
        binding.rvPopularProduct.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = popularProductAdapter
        }
    }

    private fun setupBestProductRv() {
        bestProductAdapter = BestProductAdapter()
        binding.rvProducts.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = bestProductAdapter
        }
    }


    // Puede ser solo 1, en este caso he usado dos a la vez
    private fun showLoading() {
        binding.progressBarPopular.visibility = View.VISIBLE
        binding.progressBarOffer.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBarPopular.visibility = View.GONE
        binding.progressBarOffer.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()

        showBottomNavigationView()
    }
}