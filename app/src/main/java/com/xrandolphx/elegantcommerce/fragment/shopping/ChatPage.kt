package com.xrandolphx.elegantcommerce.fragment.shopping

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xrandolphx.elegantcommerce.viewmodel.ChatViewModel


@Composable
fun ChatPage(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPlayingIndex by viewModel.currentPlayingIndex.collectAsState()
    val isImageSelectionLocked by viewModel.isImageSelectionLocked.collectAsState()
    val scrollState = rememberScrollState()
    var currentPrompt by remember { mutableStateOf("") }

    // Definición del launcher para la selección de imágenes
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.setImage(uri)
    }

    LaunchedEffect(Unit) {
        viewModel.initTextToSpeech(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopTextToSpeech()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Asesor de Imagen Personal",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Consultas restantes: ${state.remainingQueries}",
            style = MaterialTheme.typography.bodyLarge,
            color = if (state.remainingQueries <= 1) Color.Red else MaterialTheme.colorScheme.onSurface
        )

        // Área de imagen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = !isImageSelectionLocked) {
                    if (!isImageSelectionLocked) {
                        launcher.launch("image/*")
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (state.imageUri != null) {
                AsyncImage(
                    model = state.imageUri,
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar foto",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isImageSelectionLocked) "Imagen bloqueada" else "Toca para seleccionar una foto",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Área de nueva consulta (siempre visible si hay consultas restantes)
        if (state.imageUri != null && state.remainingQueries > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = currentPrompt,
                        onValueChange = { currentPrompt = it },
                        label = { Text("¿En qué te gustaría que te asesore?") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading
                    )

                    Button(
                        onClick = {
                            viewModel.analyzeImage(currentPrompt, context)
                            currentPrompt = "" // Limpiar el prompt después de enviar
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = currentPrompt.isNotEmpty() && !state.isLoading
                    ) {
                        Text(if (state.isLoading) "Veamos..." else "Analizar imagen")
                    }
                }
            }
        }

        // Historial de conversaciones
        state.conversations.asReversed().forEachIndexed { index, conversation ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Consulta: ${conversation.prompt}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Respuesta",
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(
                            onClick = {
                                if (isPlaying && currentPlayingIndex == index) {
                                    viewModel.stopTextToSpeech()
                                } else {
                                    viewModel.playResponse(index)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isPlaying && currentPlayingIndex == index)
                                    Icons.Default.Clear else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying && currentPlayingIndex == index)
                                    "Detener audio" else "Reproducir audio"
                            )
                        }
                    }

                    Text(
                        text = conversation.response,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Mensaje cuando se agotan las consultas
        if (state.remainingQueries <= 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Has alcanzado el límite de consultas permitidas.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Botón para reiniciar
        if (state.conversations.isNotEmpty()) {
            TextButton(
                onClick = { viewModel.unlockImageSelection() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Analizar otra imagen")
            }
        }
    }
}