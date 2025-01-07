package com.xrandolphx.elegantcommerce.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xrandolphx.elegantcommerce.BuildConfig
import com.xrandolphx.elegantcommerce.data.Conversation
import com.xrandolphx.elegantcommerce.data.ImageAnalysisState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.max

class ChatViewModel : ViewModel() {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.API_KEY
    )

    private val _state = MutableStateFlow(ImageAnalysisState())
    val state = _state.asStateFlow()

    private var textToSpeech: TextToSpeech? = null
    private var utteranceId = "TTSUtterance"

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPlayingIndex = MutableStateFlow<Int?>(null)
    val currentPlayingIndex = _currentPlayingIndex.asStateFlow()

    private val _isImageSelectionLocked = MutableStateFlow(false)
    val isImageSelectionLocked = _isImageSelectionLocked.asStateFlow()

    fun initTextToSpeech(context: Context) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                _state.value = _state.value.copy(
                    response = "Error: No se pudo inicializar el texto a voz"
                )
            } else {
                textToSpeech?.apply {
                    language = Locale("es", "LA")
                    setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            viewModelScope.launch {
                                _isPlaying.value = true
                            }
                        }

                        override fun onDone(utteranceId: String?) {
                            viewModelScope.launch {
                                _isPlaying.value = false
                                _currentPlayingIndex.value = null
                            }
                        }

                        override fun onError(utteranceId: String?) {
                            viewModelScope.launch {
                                _isPlaying.value = false
                                _currentPlayingIndex.value = null
                            }
                        }
                    })
                }
            }
        }
    }

    fun playResponse(index: Int? = null) {
        val textToPlay = if (index != null) {
            _currentPlayingIndex.value = index
            // Invertimos el índice para que coincida con el orden visual
            val reversedIndex = _state.value.conversations.size - 1 - index
            _state.value.conversations[reversedIndex].response
        } else {
            _state.value.response
        }

        val cleanText = textToPlay
            .replace(Regex("[*_`#]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun analyzeImage(prompt: String, context: Context) {
        if (_state.value.remainingQueries <= 0) {
            _state.value = _state.value.copy(
                response = "Has alcanzado el límite de consultas permitidas."
            )
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val bitmap = context.contentResolver.openInputStream(_state.value.imageUri!!)?.use {
                    BitmapFactory.decodeStream(it)
                } ?: throw Exception("No se pudo cargar la imagen")

                val compressedBitmap = compressBitmap(bitmap)

                // Primero las instrucciones principales, luego el historial
                val fullPrompt = """
                Actúa como un asesor de imagen profesional y analiza detalladamente la imagen proporcionada.
                
                Considera en tu análisis:
                - Estilo actual
                - Combinación de colores
                - Peinado y accesorios
                - Tipo de evento (formal/informal)
                
                Da recomendaciones específicas y constructivas sobre:
                - Posibles mejoras en el estilo
                - Combinaciones de colores alternativas
                - Accesorios que podrían complementar el look
                - Sugerencias según la ocasión
                
                Consulta actual: $prompt
                
                Historial de conversación reciente:
                ${_state.value.conversations
                    .takeLastWhile { it.timestamp >= System.currentTimeMillis() - 3600000 }
                    .joinToString("\n") {
                        "Usuario: ${it.prompt}\nAnálisis previo: ${it.response}\n---"
                    }}
                
                Usa el historial de conversación para dar una respuesta más contextualizada si es relevante.
                Mantén la respuesta breve y precisa.
            """.trimIndent()

                val response = retry(
                    times = 3,
                    initialDelay = 1000L
                ) {
                    try {
                        generativeModel.generateContent(
                            content {
                                image(compressedBitmap)
                                text(fullPrompt)
                            }
                        )
                    } catch (e: Exception) {
                        when {
                            e.message?.contains("OTHER") == true ->
                                throw RetryableException("Error temporal de Gemini", e)
                            else -> throw e
                        }
                    }
                }

                saveQueryToFirestore(prompt)

                val responseText = response.text?.trim() ?: "Sin respuesta"

                _state.value = _state.value.copy(
                    conversations = _state.value.conversations + Conversation(
                        prompt = prompt,
                        response = responseText,
                        timestamp = System.currentTimeMillis()
                    ),
                    remainingQueries = _state.value.remainingQueries - 1,
                    isLoading = false
                )

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is RetryableException -> "Error temporal: Por favor, intenta nuevamente en unos momentos"
                    is QuotaExceededException -> "Has excedido el límite de consultas. Intenta más tarde"
                    else -> "Error: ${e.message}"
                }

                _state.value = _state.value.copy(
                    response = errorMessage,
                    isLoading = false
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }

    fun setImage(uri: Uri?) {
        if (!_isImageSelectionLocked.value) {
            _state.value = _state.value.copy(
                imageUri = uri,
                response = ""  // Limpiar respuesta anterior
            )
            // Bloquear la selección de imagen una vez que se haya subido una
            if (uri != null) {
                _isImageSelectionLocked.value = true
            }
        }
    }

    fun unlockImageSelection() {
        _isImageSelectionLocked.value = false
        _state.value = _state.value.copy(
            imageUri = null,
            response = "",
            conversations = emptyList(),
            remainingQueries = 5 // Reiniciar el contador de consultas
        )
        stopTextToSpeech()
    }

    fun stopTextToSpeech() {
        textToSpeech?.stop()
        _isPlaying.value = false
    }

    suspend fun saveQueryToFirestore(prompt: String) = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                Log.e("Firestore", "No hay usuario autenticado")
                return@withContext
            }

            val userId = user.uid
            val userDoc = db.collection("user").document(userId).get().await()

            if (!userDoc.exists()) {
                Log.d("Firestore", "No se encontró el documento del usuario")
                return@withContext
            }

            val firstName = userDoc.getString("firstname") ?: "unknown_firstname"

            val queryData = hashMapOf(
                "userId" to userId,
                "userName" to firstName,
                "prompt" to prompt,
                "timestamp" to Timestamp.now()
            )

            val result = db.collection("userQueries").add(queryData).await()
            Log.d("Firestore", "Consulta registrada con ID: ${result.id}")
        } catch (e: Exception) {
            Log.e("Firestore", "Error al registrar la consulta", e)
        }
    }

    private fun compressBitmap(original: Bitmap): Bitmap {
        val maxDimension = 1024
        if (original.width > maxDimension || original.height > maxDimension) {
            val ratio = maxDimension.toFloat() / max(original.width, original.height)
            return Bitmap.createScaledBitmap(
                original,
                (original.width * ratio).toInt(),
                (original.height * ratio).toInt(),
                true
            )
        }
        return original
    }

    private suspend fun <T> retry(
        times: Int,
        initialDelay: Long = 100,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(times - 1) { attempt ->
            try {
                return block()
            } catch (e: RetryableException) {
                Log.w("ChatViewModel", "Reintento ${attempt + 1} de $times")
            }
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong()
        }
        return block() // Último intento
    }

    private class RetryableException(message: String, cause: Throwable? = null) :
        Exception(message, cause)

    private class QuotaExceededException(message: String) : Exception(message)

}



