package com.example.rentify.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UsuarioRemoteDTO(
    val id: Long? = null,
    val pnombre: String,
    val snombre: String,
    val papellido: String,
    @SerializedName("fnacimiento")
    val fnacimiento: String,
    val email: String,
    val rut: String,
    val ntelefono: String,
    @SerializedName(value = "clave", alternate = ["password"]) // La clave puede ser nula al actualizar
    val clave: String? = null,
    val direccion: String? = null,
    val comuna: String? = null,
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
    val rol: RolRemoteDTO? = null,
    val estado: EstadoRemoteDTO? = null
)

data class LoginRemoteDTO(
    val email: String,
    val clave: String
)

data class LoginResponseRemoteDTO(
    val mensaje: String,
    val usuario: UsuarioRemoteDTO
)

data class RolRemoteDTO(
    val id: Long,
    val nombre: String
)

data class EstadoRemoteDTO(
    val id: Long,
    val nombre: String
)

data class UserServiceErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val validationErrors: Map<String, String>? = null
) {
    fun getUserFriendlyMessage(): String {
        if (!validationErrors.isNullOrEmpty()) {
            return validationErrors.values.joinToString("\n")
        }
        return when (status) {
            400 -> "Error de validación: $message"
            401 -> "Error de autenticación: $message"
            404 -> "Recurso no encontrado: $message"
            409 -> "Conflicto: $message"
            500 -> "Error del servidor. Por favor intente nuevamente."
            503 -> "Servicio no disponible. Por favor intente más tarde."
            else -> message
        }
    }

    fun getFirstValidationError(): String? {
        return validationErrors?.values?.firstOrNull()
    }

    fun hasFieldError(field: String): Boolean {
        return validationErrors?.containsKey(field) == true
    }

    fun getFieldError(field: String): String? {
        return validationErrors?.get(field)
    }
}
