package com.xrandolphx.elegantcommerce.util

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UsageTracker(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private var sessionStartTime: Long = 0
    private var isTracking: Boolean = false
    private var currentDay: String = getCurrentDay()

    // Obtención del día actual en formato yyyy-MM-dd
    private fun getCurrentDay(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Funciones de conversión de tiempo
    private fun millisecondsToMinutes(ms: Long): Double {
        return (ms / 1000.0) / 60.0
    }

    private fun millisecondsToHours(ms: Long): Double {
        return millisecondsToMinutes(ms) / 60.0
    }

    // Función para formatear los tiempos en un Map
    private fun formatTimeValues(sessionTimeMs: Long): Map<String, Any> {
        val minutes = millisecondsToMinutes(sessionTimeMs)
        val hours = millisecondsToHours(sessionTimeMs)

        return mapOf(
            "minutes" to minutes.roundToDecimals(2),
            "hours" to hours.roundToDecimals(2),
            "milliseconds" to sessionTimeMs,
            "lastUpdated" to FieldValue.serverTimestamp(),
            "currentDay" to getCurrentDay()
        )
    }

    // Extensión para redondear decimales
    private fun Double.roundToDecimals(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }

    fun startTracking() {
        if (!isTracking) {
            checkAndResetIfNewDay()
            sessionStartTime = System.currentTimeMillis()
            isTracking = true
            Log.d("UsageTracker", "Tracking started at $sessionStartTime")
        }
    }

    fun stopTracking() {
        if (isTracking) {
            checkAndResetIfNewDay()
            val sessionTime = System.currentTimeMillis() - sessionStartTime
            saveSessionTime(sessionTime)
            isTracking = false
            Log.d(
                "UsageTracker",
                "Tracking stopped. Session duration: ${millisecondsToMinutes(sessionTime)} minutes"
            )
        }
    }

    fun updateTracking() {
        if (isTracking) {
            checkAndResetIfNewDay()
            val currentTime = System.currentTimeMillis()
            val sessionTime = currentTime - sessionStartTime
            saveSessionTime(sessionTime)
            Log.d(
                "UsageTracker",
                "Tracking updated. Duration: ${millisecondsToMinutes(sessionTime)} minutes"
            )
        }
    }

    private fun checkAndResetIfNewDay() {
        val newDay = getCurrentDay()
        if (newDay != currentDay) {
            resetUsageTime()
            currentDay = newDay
        }
    }

    private fun resetUsageTime() {
        val userId = auth.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userDocRef = db.collection("usersTime").document(userId)
                val resetTimesValues = mapOf(
                    "minutes" to 0.0,
                    "hours" to 0.0,
                    "milliseconds" to 0L,
                    "lastUpdated" to FieldValue.serverTimestamp(),
                    "currentDay" to getCurrentDay()
                )
                userDocRef.set(mapOf("totalUsageTime" to resetTimesValues))
                Log.d("UsageTracker", "Tiempo de uso restablecido para nuevo día")
            } catch (e: Exception) {
                Log.e("UsageTracker", "Error al restablecer el tiempo de uso: ${e.message}", e)
            }
        }
    }

    private fun saveSessionTime(sessionTime: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = auth.uid
                if (userId == null) {
                    Log.e("UsageTracker", "Error: Usuario no autenticado")
                    return@launch
                }

                val userDocRef = db.collection("usersTime").document(userId)

                // Formatear los tiempos antes de guardar
                val timeValues = formatTimeValues(sessionTime)

                db.runTransaction { transaction ->
                    val userDoc = transaction.get(userDocRef)
                    if (!userDoc.exists()) {
                        transaction.set(userDocRef, mapOf("totalUsageTime" to timeValues))
                    } else {
                        // Obtener los valores actuales
                        val currentTotal = userDoc.get("totalUsageTime") as? Map<*, *>
                        val currentDay = currentTotal?.get("currentDay") as? String

                        // Si es un nuevo día, reiniciar los valores
                        if (currentDay != getCurrentDay()) {
                            transaction.set(userDocRef, mapOf("totalUsageTime" to timeValues))
                        } else {
                            // Actualizar los valores existentes
                            val currentMinutes = (currentTotal?.get("minutes") as? Double) ?: 0.0
                            val currentHours = (currentTotal?.get("hours") as? Double) ?: 0.0
                            val currentMillis = (currentTotal?.get("milliseconds") as? Long) ?: 0L

                            val updatedTotals = mapOf(
                                "totalUsageTime.minutes" to (currentMinutes + timeValues["minutes"] as Double),
                                "totalUsageTime.hours" to (currentHours + timeValues["hours"] as Double),
                                "totalUsageTime.milliseconds" to (currentMillis + sessionTime),
                                "totalUsageTime.lastUpdated" to FieldValue.serverTimestamp(),
                                "totalUsageTime.currentDay" to getCurrentDay()
                            )

                            updatedTotals.forEach { (field, value) ->
                                transaction.update(userDocRef, field, value)
                            }
                        }
                    }
                }.await()

                Log.d(
                    "UsageTracker",
                    "Tiempo de sesión guardado con éxito: ${timeValues["minutes"]} minutos"
                )
            } catch (e: Exception) {
                Log.e("UsageTracker", "Error al guardar el tiempo de sesión: ${e.message}", e)
            }
        }
    }
}