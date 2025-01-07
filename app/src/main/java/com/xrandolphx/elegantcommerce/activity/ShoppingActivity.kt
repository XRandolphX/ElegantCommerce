package com.xrandolphx.elegantcommerce.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xrandolphx.elegant_commerce.util.Resource
import com.xrandolphx.elegantcommerce.R
import com.xrandolphx.elegantcommerce.databinding.ActivityShoppingBinding
import com.xrandolphx.elegantcommerce.util.UsageTracker
import com.xrandolphx.elegantcommerce.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShoppingActivity : AppCompatActivity() {

    private lateinit var usageTracker: UsageTracker
    private var periodicUpdateJob: Job? = null

    private val binding by lazy { ActivityShoppingBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<CartViewModel>()

    companion object {
        private const val UPDATE_INTERVAL = 30 * 1000L // Actualizar cada 30 segundos
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupNavigation()
        observeCartProducts()
        initializeUsageTracker()
    }

    private fun initializeUsageTracker() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        usageTracker = UsageTracker(db, auth)

        // Iniciar el seguimiento solo si el usuario está autenticado
        auth.currentUser?.let {
            usageTracker.startTracking()
            startPeriodicUpdate() // Solo inicia el Job de actualización
        }
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.shoppingHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun observeCartProducts() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cartProducts.collect { result ->
                    when (result) {
                        is Resource.Success -> updateCartBadge(result.data?.size ?: 0)
                        else -> {} // Manejar otros estados si es necesario
                    }
                }
            }
        }
    }

    private fun updateCartBadge(count: Int) {
        binding.bottomNavigation.getOrCreateBadge(R.id.cartFragment).apply {
            number = count
            backgroundColor = ContextCompat.getColor(this@ShoppingActivity, R.color.g_orange_yellow)
        }
    }

    override fun onResume() {
        super.onResume()
        if (FirebaseAuth.getInstance().currentUser != null) {
            startPeriodicUpdate() // Inicia actualizaciones periódicas si el usuario está autenticado
        }
    }

    override fun onPause() {
        super.onPause()
        periodicUpdateJob?.cancel() // Solo cancela el Job de actualización
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTrackingAndUpdates() // Detener seguimiento solo al salir definitivamente
    }

    private fun stopTrackingAndUpdates() {
        usageTracker.stopTracking() // Detiene el seguimiento completamente
        periodicUpdateJob?.cancel()
        periodicUpdateJob = null
    }

    private fun startPeriodicUpdate() {
        periodicUpdateJob?.cancel() // Asegura que no haya Jobs previos

        periodicUpdateJob = lifecycleScope.launch {
            while (isActive) {
                delay(UPDATE_INTERVAL)
                try {
                    usageTracker.updateTracking()
                } catch (e: Exception) {
                    Log.e("ShoppingActivity", "Error updating usage tracking: ${e.message}")
                }
            }
        }
    }
}
