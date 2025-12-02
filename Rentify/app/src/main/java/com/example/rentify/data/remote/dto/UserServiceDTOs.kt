package com.example.rentify.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==================== USER SERVICE DTOs ====================

/**
 * DTO para registro/login de usuario
 * Coincide con UsuarioDTO.java del backend
 */
data class UsuarioRemoteDTO(
    val id: Long? = null,

    val pnombre: String,
    val snombre: String,
    val papellido: String,

    @SerializedName("fnacimiento")
    val fnacimiento: String,  // Formato: "yyyy-MM-dd"

    val email: String,
    val rut: String,
    val ntelefono: String,
    val clave: String,

    @SerializedName("duocVip")
    val duocVip: Boolean? = null,

    val puntos: Int? = null,

    @SerializedName("codigoRef")
    val codigoRef: String? = null,

    @SerializedName("fcreacion")
    val fcreacion: String? = null,

    @SerializedName("factualizacion")
    val factualizacion: String? = null,

    @SerializedName("estadoId")
    val estadoId: Long? = null,

    @SerializedName("rolId")
    val rolId: Long? = null,

    // Campos opcionales cuando includeDetails=true
    val rol: RolRemoteDTO? = null,
    val estado: EstadoRemoteDTO? = null
)

/**
 * DTO para actualizacion de usuario (admin)
 * Coincide con UsuarioUpdateDTO.java del backend
 * No requiere campos sensibles como clave, rut, fnacimiento
 */
data class UsuarioUpdateRemoteDTO(
    val pnombre: String,
    val snombre: String,
    val papellido: String,
    val email: String,
    val ntelefono: String,

    @SerializedName("rolId")
    val rolId: Long? = null,

    @SerializedName("estadoId")
    val estadoId: Long? = null
)

/**
 * DTO para login request
 */
data class LoginRemoteDTO(
    val email: String,
    val clave: String
)

/**
 * DTO para respuesta de login
 */
data class LoginResponseRemoteDTO(
    val mensaje: String,
    val usuario: UsuarioRemoteDTO
)

/**
 * DTO de Rol
 */
data class RolRemoteDTO(
    val id: Long,
    val nombre: String
)

/**
 * DTO de Estado
 */
data class EstadoRemoteDTO(
    val id: Long,
    val nombre: String
)

/**
 * Respuesta de error estructurada del backend User Service
 * Coincide con ErrorResponse.java del backend
 */
data class UserServiceErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val validationErrors: Map<String, String>? = null
) {
    /**
     * Obtiene un mensaje de error legible para mostrar al usuario
     */
    fun getUserFriendlyMessage(): String {
        // Si hay errores de validacion especificos, mostrarlos
        if (!validationErrors.isNullOrEmpty()) {
            return validationErrors.values.joinToString("\n")
        }

        // Personalizar mensajes segun el codigo HTTP
        return when (status) {
            400 -> "Error de validacion: $message"
            401 -> "Error de autenticacion: $message"
            404 -> "Recurso no encontrado: $message"
            409 -> "Conflicto: $message"
            500 -> "Error del servidor. Por favor intente nuevamente."
            503 -> "Servicio no disponible. Por favor intente mas tarde."
            else -> message
        }
    }

    /**
     * Obtiene el primer error de validacion
     */
    fun getFirstValidationError(): String? {
        return validationErrors?.values?.firstOrNull()
    }

    /**
     * Verifica si es un error de validacion de campo especifico
     */
    fun hasFieldError(field: String): Boolean {
        return validationErrors?.containsKey(field) == true
    }

    /**
     * Obtiene el error de un campo especifico
     */
    fun getFieldError(field: String): String? {
        return validationErrors?.get(field)
    }
}