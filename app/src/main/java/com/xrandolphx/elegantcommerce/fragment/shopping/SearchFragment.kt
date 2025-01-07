package com.xrandolphx.elegantcommerce.fragment.shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.xrandolphx.elegant_commerce.util.Resource
import com.xrandolphx.elegantcommerce.R
import com.xrandolphx.elegantcommerce.adapter.AllProductsAdapter
import com.xrandolphx.elegantcommerce.adapter.BestProductAdapter
import com.xrandolphx.elegantcommerce.adapter.PopularProductAdapter
import com.xrandolphx.elegantcommerce.databinding.FragmentSearchBinding
import com.xrandolphx.elegantcommerce.viewmodel.AllProductsViewModel
import com.xrandolphx.elegantcommerce.viewmodel.MainCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val TAG = "SearchFragment"

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var allProductsAdapter: AllProductsAdapter
    private val viewModel by viewModels<AllProductsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAllProductsRv()

        allProductsAdapter.onClick = { product ->
            val b = Bundle().apply { putParcelable("product", product) }
            findNavController().navigate(R.id.action_searchFragment_to_productDetailFragment, b)
        }

        binding.ivAI.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_chatFragment)
        }

        // Configurando el SearchView
        binding.svSearchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Puedes manejar la búsqueda cuando el usuario envíe la consulta
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filtra los productos a medida que el usuario escribe
                filterProducts(newText)
                return true
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allProducts.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.progressBarProducts.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            allProductsAdapter.differ.submitList(it.data)
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

        binding.nestedScrollAllProducts.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (v.getChildAt(0).bottom <= v.height + scrollY) {
                viewModel.fetchAllProducts()
            }
        })

    }

    private fun setupAllProductsRv() {
        allProductsAdapter = AllProductsAdapter()
        binding.rvAllProducts.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = allProductsAdapter
        }
    }

    // Método para filtrar los productos en función del texto ingresado en el SearchView.
    private fun filterProducts(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            viewModel.allProducts.value.data // Lista original
        } else {
            viewModel.allProducts.value.data?.filter { product ->
                product.name.contains(query, ignoreCase = true)
            }
        }
        // Actualiza la lista de productos mostrados en el adaptador
        allProductsAdapter.differ.submitList(filteredList)
    }


//    private fun showLoading() {
//        binding.progressBarProducts.visibility = View.VISIBLE
//        binding.progressBarProducts.visibility = View.VISIBLE
//    }
//
//    private fun hideLoading() {
//        binding.progressBarProducts.visibility = View.GONE
//        binding.progressBarProducts.visibility = View.GONE
//    }

}