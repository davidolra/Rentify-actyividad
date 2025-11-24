package com.example.rentify.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==================== APPLICATION SERVICE DTOs ====================

/**
 * DTO para Solicitud de Arriendo
 * Coincide exactamente con SolicitudArriendoDTO.java del backend
 */
data class SolicitudArriendoDTO(
    val id: Long? = null,

    @SerializedName("usuarioId")
    val usuarioId: Long,

    @SerializedName("propiedadId")
    val propiedadId: Long,

    val estado: String? = null,  // PENDIENTE, ACEPTADA, RECHAZADA

    @SerializedName("fechaSolicitud")
    val fechaSolicitud: String? = null,  // ISO 8601 format

    val usuario: UsuarioDTO? = null,
    val propiedad: PropiedadDTO? = null
)

/**
 * DTO para Registro de Arriendo
 * Coincide con RegistroArriendoDTO.java del backend
 */
data class RegistroArriendoDTO(
    val id: Long? = null,

    @SerializedName("solicitudId")
    val solicitudId: Long,

    @SerializedName("fechaInicio")
    val fechaInicio: String,  // Formato: "yyyy-MM-dd"

    @SerializedName("fechaFin")
    val fechaFin: String? = null,

    @SerializedName("montoMensual")
    val montoMensual: Double,

    val activo: Boolean? = null,
    val solicitud: SolicitudArriendoDTO? = null
)

/**
 * DTO de Usuario (desde User Service)
 * Coincide con UsuarioDTO.java del Application Service
 */
data class UsuarioDTO(
    val id: Long? = null,
    val pnombre: String,
    val snombre: String,
    val papellido: String,
    val email: String,
    val ntelefono: String? = null,

    @SerializedName("rolId")
    val rolId: Int? = null,

    val rol: RolInfo? = null,
    val estado: EstadoInfo? = null,

    @SerializedName("duocVip")
    val duocVip: Boolean? = null
) {
    data class RolInfo(
        val id: Int,
        val nombre: String
    )

    data class EstadoInfo(
        val id: Int,
        val nombre: String
    )
}

/**
 * DTO de Propiedad (desde Property Service)
 * Coincide con PropiedadDTO.java del Application Service
 */
data class PropiedadDTO(
    val id: Long? = null,
    val codigo: String,
    val titulo: String,
    val direccion: String,

    @SerializedName("precioMensual")
    val precioMensual: Double,

    val divisa: String = "CLP",
    val m2: Double,

    @SerializedName("nHabit")
    val nHabit: Int,

    @SerializedName("nBanos")
    val nBanos: Int,

    @SerializedName("petFriendly")
    val petFriendly: Boolean,

    @SerializedName("tipoId")
    val tipoId: Int,

    @SerializedName("comunaId")
    val comunaId: Int,

    val fcreacion: String? = null,
    val tipo: TipoInfo? = null,
    val comuna: ComunaInfo? = null
) {
    data class TipoInfo(
        val id: Int,
        val nombre: String
    )

    data class ComunaInfo(
        val id: Int,
        val nombre: String
    )
}

/**
 * Respuesta de Error del Backend
 */
data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val validationErrors: Map<String, String>? = null
)