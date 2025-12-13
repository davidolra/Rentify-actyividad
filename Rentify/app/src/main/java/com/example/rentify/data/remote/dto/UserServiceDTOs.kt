package com.example.rentify.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==================== USER SERVICE DTOs ====================

/**
 * DTO para registro/login de usuario
 */
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

    val rol: RolRemoteDTO? = null,
    val estado: EstadoRemoteDTO? = null
)

/**
 * DTO para actualizacion de usuario (admin)
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

data class RolRemoteDTO(val id: Long, val nombre: String)
data class EstadoRemoteDTO(val id: Long, val nombre: String)

/**
 * Respuesta de error estructurada del backend User Service
 */
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
        return message
    }
}