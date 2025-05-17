package com.xrandolphx.elegantcommerce.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.xrandolphx.elegantcommerce.R
import com.xrandolphx.elegantcommerce.data.order.Order
import com.xrandolphx.elegantcommerce.data.order.OrderStatus
import com.xrandolphx.elegantcommerce.databinding.ActivityPaymentResultBinding
import com.xrandolphx.elegantcommerce.util.PaymentResultCache
import com.xrandolphx.elegantcommerce.viewmodel.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentResultBinding
    private val orderViewModel by viewModels<OrderViewModel>()
    private val TAG = "PaymentResultActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_result)

        binding = ActivityPaymentResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val data: Uri? = intent?.data
        if (data != null) {
            when (data.lastPathSegment) {
                "success" -> {
                    handlePaymentSuccess()
                }

                "failure" -> {
                    handlePaymentFailure()
                }

                "pending" -> {
                    handlePaymentPending()
                }

                else -> showUnknownResult()
            }
        } else {
            showUnknownResult()
        }

        binding.buttonContinueShopping.setOnClickListener {
            navigateToHomeScreen()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val appLinkAction = intent.action
        val appLinkData: Uri? = intent.data

        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            val path = appLinkData.pathSegments
            Log.d(TAG, "Deeplink recibido: $appLinkData, path: $path")

            if (path.isNotEmpty()) {
                when (path[0]) {
                    "success" -> handlePaymentSuccess()
                    "failure" -> handlePaymentFailure()
                    "pending" -> handlePaymentPending()
                    else -> {
                        Log.e(TAG, "Ruta de deeplink desconocida: ${path[0]}")
                        navigateToHomeScreen()
                    }
                }
            }
        } else {
            Log.e(TAG, "Intent no reconocido o sin datos de deeplink")
            navigateToHomeScreen()
        }
    }

    private fun handlePaymentSuccess() {
        binding.imagePaymentResult.setImageResource(R.drawable.ic_check_circle)
        binding.tvPaymentResultTitle.text = "¡Pago exitoso!"
        binding.tvPaymentResultDescription.text = "Tu pedido ha sido procesado correctamente."

        // Recuperar los datos del cache para crear la orden
        val orderData = PaymentResultCache.getLastOrderData()
        if (orderData != null) {
            val order = Order(
                orderStatus = OrderStatus.Confirmed.status,
                totalPrice = orderData.totalPrice,
                products = orderData.products,
                addressOrder = orderData.address
            )

            // Guardar la orden en la base de datos
            orderViewModel.placeOrder(order)

            // Limpiar el cache después de usarlo
            PaymentResultCache.clearLastOrderData()
        } else {
            Log.e(TAG, "No se encontraron datos de la orden en caché")
        }

        binding.buttonContinueShopping.setOnClickListener {
            navigateToHomeScreen()
        }
    }

    private fun handlePaymentFailure() {
        binding.imagePaymentResult.setImageResource(R.drawable.ic_error_circle)
        binding.tvPaymentResultTitle.text = "Pago fallido"
        binding.tvPaymentResultDescription.text =
            "Hubo un problema procesando tu pago. Por favor intenta nuevamente."

        binding.buttonContinueShopping.text = "Intentar nuevamente"
        binding.buttonContinueShopping.setOnClickListener {
            finish() // Regresa a la pantalla anterior (BillingFragment)
        }
    }

    private fun handlePaymentPending() {
        binding.imagePaymentResult.setImageResource(R.drawable.ic_info_circle)
        binding.tvPaymentResultTitle.text = "Pago pendiente"
        binding.tvPaymentResultDescription.text =
            "Tu pago está siendo procesado. Te notificaremos cuando se complete."

        binding.buttonContinueShopping.setOnClickListener {
            navigateToHomeScreen()
        }
    }

    private fun navigateToHomeScreen() {
        // Ir a la pantalla principal y limpiar el stack de activities
        val intent = Intent(this, ShoppingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showUnknownResult() {
        binding.imagePaymentResult.setImageResource(R.drawable.ic_info_circle)
        binding.tvPaymentResultTitle.text = "Estado desconocido"
        binding.tvPaymentResultDescription.text =
            "No pudimos verificar si tu pago fue exitoso o no. Por favor revisa tu estado de pago o contacta soporte."
        binding.buttonContinueShopping.text = "Volver al inicio"
    }

}