package com.xrandolphx.elegantcommerce.fragment.shopping

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.xrandolphx.elegant_commerce.util.Resource
import com.xrandolphx.elegantcommerce.R
import com.xrandolphx.elegantcommerce.adapter.AddressAdapter
import com.xrandolphx.elegantcommerce.adapter.BillingProductsAdapter
import com.xrandolphx.elegantcommerce.data.Address
import com.xrandolphx.elegantcommerce.data.CartProduct
import com.xrandolphx.elegantcommerce.data.order.Order
import com.xrandolphx.elegantcommerce.data.order.OrderStatus
import com.xrandolphx.elegantcommerce.databinding.FragmentBillingBinding
import com.xrandolphx.elegantcommerce.util.HorizontalItemDecoration
import com.xrandolphx.elegantcommerce.util.PaymentResultCache
import com.xrandolphx.elegantcommerce.viewmodel.BillingViewModel
import com.xrandolphx.elegantcommerce.viewmodel.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BillingFragment : Fragment() {
    private lateinit var binding: FragmentBillingBinding
    private val addressAdapter by lazy { AddressAdapter() }
    private val billingProductsAdapter by lazy { BillingProductsAdapter() }
    private val billingViewModel by viewModels<BillingViewModel>()
    private val args by navArgs<BillingFragmentArgs>()
    private var products = emptyList<CartProduct>()
    private var totalPrice = 0f

    private var selectedAddress: Address? = null
    private val orderViewModel by viewModels<OrderViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        products = args.products.toList()
        totalPrice = args.totalPrice
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBillingProductsRv()
        setupAddressRv()

        binding.imageCloseBilling.setOnClickListener {
            findNavController().navigateUp()
        }

        if (!args.payment) {
            binding.apply {
                buttonPlaceOrder.visibility = View.INVISIBLE
                totalBoxContainer.visibility = View.INVISIBLE
                middleLine.visibility = View.INVISIBLE
                bottomLine.visibility = View.INVISIBLE
            }
        }

        binding.imageAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_billingFragment_to_addressFragment)
        }

        addressAdapter.onClick = {
            selectedAddress = it
            if (!args.payment) {
                val b = Bundle().apply { putParcelable("address", selectedAddress) }
                findNavController().navigate(R.id.action_billingFragment_to_addressFragment, b)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                billingViewModel.address.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.progressbarAddress.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            addressAdapter.differ.submitList(it.data)
                            binding.progressbarAddress.visibility = View.GONE
                        }

                        is Resource.Error -> {
                            binding.progressbarAddress.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Error ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                orderViewModel.order.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.buttonPlaceOrder.startAnimation()
                        }

                        is Resource.Success -> {
                            binding.buttonPlaceOrder.revertAnimation()
                            findNavController().navigateUp()
                            Snackbar.make(
                                requireView(),
                                "Su pedido ha sido realizado ",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                        is Resource.Error -> {
                            binding.buttonPlaceOrder.revertAnimation()
                            Toast.makeText(
                                requireContext(),
                                "Error ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        billingProductsAdapter.differ.submitList(products)
        binding.tvTotalPrice.text = "S/ $totalPrice"

        binding.buttonPlaceOrder.setOnClickListener {
            if (selectedAddress == null) {
                Toast.makeText(
                    requireContext(),
                    "Por favor selecciona una dirección",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }
            showOrderConfirmationDialog()
        }
    }

    private fun showOrderConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Ordenar Productos")
            setMessage("¿Desea pedir los artículos de su cesta y proceder al pago?")
            setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton("Sí") { dialog, _ ->
                dialog.dismiss()
                inititatePayment()
            }
        }

        alertDialog.create().show()
    }

    private fun setupAddressRv() {
        binding.rvAddress.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = addressAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }

    private fun setupBillingProductsRv() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = billingProductsAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }

    private fun inititatePayment() {
        Log.d("BillingFragment", "Llamando a createPaymentPreference...")

        // Guardamos los datos del pedido en la caché para recuperarlos después del pago
        PaymentResultCache.saveOrderData(products, totalPrice, selectedAddress!!)

        // Mostramos un indicador de carga
        binding.buttonPlaceOrder.startAnimation()

        // Llamamos al metodo para crear la preferencia de pago
        billingViewModel.createPaymentPreference(products, totalPrice, selectedAddress!!)

        // Observamos el StateFlow para reaccionar a los diferentes estados
        viewLifecycleOwner.lifecycleScope.launch {
            billingViewModel.paymentPreference.collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        Log.d("BillingFragment", "Creando preferencia de pago ...")
                        binding.buttonPlaceOrder.revertAnimation()
                    }

                    is Resource.Success -> {
                        Log.d("BillingFragment", "Preferencia creada: ${result.data}")
                        binding.buttonPlaceOrder.revertAnimation()

                        val initPoint = result.data?.initPoint
                        if (initPoint.isNullOrEmpty()) {
                            Log.e("BillingFragment", "Error: initPoint es nulo o vacío")
                            Toast.makeText(
                                requireContext(),
                                "Error al obtener el punto de pago",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@collect
                        }
                        // Lanza la Custom Tab con el init_point recibido
                        try {
                            val customTabsIntent = CustomTabsIntent.Builder()
                                .setShowTitle(true)
                                .build()
                            customTabsIntent.launchUrl(requireContext(), Uri.parse(initPoint))
                        } catch (e: Exception) {
                            Log.e("BillingFragment", "Error al abrir el URL", e)
                            Toast.makeText(
                                requireContext(),
                                "Error al abrir la página de pago: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    is Resource.Error -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        Log.e("BillingFragment", "Error: ${result.message}")
                        AlertDialog.Builder(requireContext())
                            .setTitle("Error")
                            .setMessage("Ocurrió un error al procesar el pago: ${result.message}")
                            .setPositiveButton("Cerrar") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .setCancelable(false)
                            .show()
                    }

                    else -> Unit
                }
            }
        }
    }
}

