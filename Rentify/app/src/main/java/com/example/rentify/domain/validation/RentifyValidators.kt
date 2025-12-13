package com.example.rentify.domain.validation


/**
 * Validadores específicos para Rentify
 */

// Valida email
fun validateEmail(email: String): String? {
    if (email.isBlank()) return "El email es obligatorio"
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()
    return if (!emailRegex.matches(email)) "Formato de email inválido" else null
}

// Valida que el nombre contenga solo letras y espacios
fun validateName(name: String): String? {
    if (name.isBlank()) return "El nombre es obligatorio"
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")
    return if (!regex.matches(name)) "Solo letras y espacios" else null
}

// Valida RUT chileno (formato: 12345678-9 o 12.345.678-9)
fun validateRut(rut: String): String? {
    if (rut.isBlank()) return "El RUT es obligatorio"

    // Limpiar el RUT (quitar puntos y guión)
    val rutLimpio = rut.replace(".", "").replace("-", "").trim()

    // Validar longitud (7-8 dígitos + 1 dígito verificador)
    if (rutLimpio.length < 8 || rutLimpio.length > 9) {
        return "RUT debe tener 8-9 caracteres"
    }

    // Separar número y dígito verificador
    val numero = rutLimpio.dropLast(1)
    val dv = rutLimpio.last().toString().uppercase()

    // Validar que el número contenga solo dígitos
    if (!numero.all { it.isDigit() }) {
        return "RUT inválido"
    }

    // Calcular dígito verificador
    var suma = 0
    var multiplo = 2

    for (i in numero.reversed()) {
        suma += i.toString().toInt() * multiplo
        multiplo = if (multiplo == 7) 2 else multiplo + 1
    }

    val resto = suma % 11
    val dvCalculado = when (11 - resto) {
        11 -> "0"
        10 -> "K"
        else -> (11 - resto).toString()
    }

    return if (dv == dvCalculado) null else "RUT inválido (dígito verificador incorrecto)"
}

// Valida teléfono chileno (+56912345678 o 912345678)
fun validatePhoneChileno(phone: String): String? {
    if (phone.isBlank()) return "El teléfono es obligatorio"

    val phoneLimpio = phone.replace("+", "").replace(" ", "").replace("-", "")

    // Formato: +56912345678 (13 dígitos) o 912345678 (9 dígitos)
    if (phoneLimpio.startsWith("56")) {
        // Formato internacional
        if (phoneLimpio.length != 11 && phoneLimpio.length != 13) {
            return "Formato: +56912345678"
        }
    } else {
        // Formato nacional
        if (phoneLimpio.length != 9) {
            return "Debe tener 9 dígitos (ej: 912345678)"
        }
        if (phoneLimpio[0] != '9') {
            return "Debe comenzar con 9"
        }
    }

    if (!phoneLimpio.all { it.isDigit() }) {
        return "Solo números"
    }

    return null
}

// Valida fecha de nacimiento (debe ser mayor de 18 años)
fun validateFechaNacimiento(fechaNacimiento: Long): String? {
    val now = System.currentTimeMillis()
    val edad18Anios = 18L * 365 * 24 * 60 * 60 * 1000 // 18 años en milisegundos

    val edadUsuario = now - fechaNacimiento

    return if (edadUsuario < edad18Anios) {
        "Debes ser mayor de 18 años para registrarte"
    } else null
}

// Valida contraseña segura (Rentify: min 8, mayús, minús, número, símbolo)
fun validateStrongPassword(pass: String): String? {
    if (pass.isBlank()) return "La contraseña es obligatoria"
    if (pass.length < 8) return "Mínimo 8 caracteres"
    if (!pass.any { it.isUpperCase() }) return "Debe incluir una mayúscula"
    if (!pass.any { it.isLowerCase() }) return "Debe incluir una minúscula"
    if (!pass.any { it.isDigit() }) return "Debe incluir un número"
    if (!pass.any { !it.isLetterOrDigit() }) return "Debe incluir un símbolo"
    if (pass.contains(' ')) return "No debe contener espacios"
    return null
}

// Valida confirmación de contraseña
fun validateConfirm(pass: String, confirm: String): String? {
    if (confirm.isBlank()) return "Confirma tu contraseña"
    return if (pass != confirm) "Las contraseñas no coinciden" else null
}

// Valida código de referido (8 caracteres alfanuméricos, opcional)
fun validateCodigoReferido(codigo: String): String? {
    if (codigo.isBlank()) return null // Es opcional

    val regex = Regex("^[A-Z0-9]{8}$")
    return if (!regex.matches(codigo.uppercase())) {
        "Código debe tener 8 caracteres alfanuméricos"
    } else null
}

// Valida precio mensual (debe ser positivo)
fun validatePrecio(precio: String): String? {
    if (precio.isBlank()) return "El precio es obligatorio"

    val precioInt = precio.toIntOrNull()
    return when {
        precioInt == null -> "Precio inválido"
        precioInt <= 0 -> "El precio debe ser mayor a 0"
        else -> null
    }
}

// Valida metros cuadrados (debe ser positivo)
fun validateM2(m2: String): String? {
    if (m2.isBlank()) return "Los m² son obligatorios"

    val m2Double = m2.toDoubleOrNull()
    return when {
        m2Double == null -> "Valor inválido"
        m2Double <= 0 -> "Debe ser mayor a 0"
        else -> null
    }
}

// Valida número de habitaciones/baños (0 o positivo)
fun validateNumero(numero: String, campo: String): String? {
    if (numero.isBlank()) return "$campo es obligatorio"

    val num = numero.toIntOrNull()
    return when {
        num == null -> "Valor inválido"
        num < 0 -> "No puede ser negativo"
        else -> null
    }
}