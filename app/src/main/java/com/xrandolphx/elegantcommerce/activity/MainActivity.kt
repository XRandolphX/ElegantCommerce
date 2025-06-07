package com.xrandolphx.elegantcommerce.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.xrandolphx.elegant_commerce.util.Resource
import com.xrandolphx.elegantcommerce.R
import com.xrandolphx.elegantcommerce.databinding.ActivityShoppingBinding
import com.xrandolphx.elegantcommerce.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var binding: ActivityShoppingBinding
    private val viewModel by viewModels<CartViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityShoppingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeCartProducts()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // Event to register that MainActivity was opened
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, "MainActivity")
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        firebaseAnalytics.logEvent("app_started", null)
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
                        else -> {} // Handle other states if necessary
                    }
                }
            }
        }
    }

    private fun updateCartBadge(count: Int) {
        binding.bottomNavigation.getOrCreateBadge(R.id.cartFragment).apply {
            number = count
            backgroundColor = ContextCompat.getColor(this@MainActivity, R.color.g_orange_yellow)
        }
    }

}
