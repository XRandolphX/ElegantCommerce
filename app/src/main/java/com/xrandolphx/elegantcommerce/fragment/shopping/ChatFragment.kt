package com.xrandolphx.elegantcommerce.fragment.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import com.xrandolphx.elegantcommerce.viewmodel.ChatViewModel


class ChatFragment : Fragment() {

    private lateinit var chatViewModel: ChatViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Iniciarlizando el viewModel en OnCreateView

        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        return ComposeView(requireContext()).apply {
            // Establece la estrategia de composición de la vista
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            // Establece el contenido de Jetpack Compose
            setContent {
                Scaffold(
                    modifier = Modifier.fillMaxSize() // Ocupa el máximo tamaño disponible
                ) { paddingValues ->
                    ChatPage(
                        modifier = Modifier.padding(paddingValues),
                        viewModel = chatViewModel // Pasar ViewModel a ChatPage si es necesario
                    )
                }
            }
        }
    }
}
