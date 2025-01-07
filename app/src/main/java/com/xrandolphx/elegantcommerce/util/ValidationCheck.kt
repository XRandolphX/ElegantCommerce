package com.xrandolphx.elegant_commerce.util

import android.util.Patterns

fun validateEmail(email: String): RegisterValidation {
    // Verifica si el campo de email está vacío
    if (email.isEmpty())
        return RegisterValidation.Failed("El email no puede estar vacío")

    // Verifica si el formato del email es válido usando Patterns
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        return RegisterValidation.Failed("Formato de email incorrecto")

    // Lista de dominios de email comunes para verificar
    val commonDomains = listOf("example.com", "test.com")

    // Verifica si el dominio del email es común
    val emailDomain = email.substringAfter("@")
    if (commonDomains.any { emailDomain.equals(it, ignoreCase = true) }) {
        return RegisterValidation.Failed("Por favor usa un email con un dominio válido")
    }

    // Si todas las validaciones pasan, retorna éxito
    return RegisterValidation.Success
}


fun validatePassword(password: String): RegisterValidation {
    val passwordPattern =
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$".toRegex()
    val commonPasswords =
        listOf("password", "123456", "qwerty", "admin") // Agrega más según sea necesario

    return when {
        password.length < 8 -> RegisterValidation.Failed(getPasswordRequirements())
        !password.matches(passwordPattern) -> RegisterValidation.Failed("La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial")
        commonPasswords.any { it.equals(password, ignoreCase = true) } -> RegisterValidation.Failed(
            "La contraseña es demasiado común, por favor elige una más segura"
        )

        else -> RegisterValidation.Success
    }
}

// Función auxiliar para mostrar los requisitos de la contraseña al usuario
fun getPasswordRequirements(): String {
    return """
        La contraseña debe cumplir los siguientes requisitos:
        - Al menos 8 caracteres de longitud
        - Al menos una letra mayúscula
        - Al menos una letra minúscula
        - Al menos un número
        - Al menos un carácter especial (@#$%^&+=)
        - No debe ser una contraseña común
    """.trimIndent()
}