package com.example.rentify.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.util.Date

// ==================== APPLICATION SERVICE DTOs ====================

/**
 * DTO para Solicitud de Arriendo
 */
data class SolicitudArriendoDTO(
    val id: Long? = null,

    @SerializedName("usuarioId")
    val usuarioId: Long,

    @SerializedName("propiedadId")
    val propiedadId: Long,

    val estado: String? = null,

    @SerializedName("fechaSolicitud")
    val fechaSolicitud: Date? = null,

    val usuario: UsuarioDTO? = null,
    val propiedad: PropiedadDTO? = null
)

/**
 * DTO para Registro de Arriendo
 */
data class RegistroArriendoDTO(
    val id: Long? = null,

    @SerializedName("solicitudId")
    val solicitudId: Long,

    @SerializedName("fechaInicio")
    val fechaInicio: Date,

    @SerializedName("fechaFin")
    val fechaFin: Date? = null,

    @SerializedName("montoMensual")
    val montoMensual: Double,

    val activo: Boolean? = null,
    val solicitud: SolicitudArriendoDTO? = null
)

/**
 * DTO de Usuario usado en la UI
 */
data class UsuarioDTO(
    val id: Long? = null,
    val pnombre: String? = null,
    val snombre: String? = null,
    val papellido: String? = null,
    val email: String? = null,
    val ntelefono: String? = null,

    @SerializedName("rolId")
    val rolId: Int? = null,

    @SerializedName("estadoId")
    val estadoId: Int? = null,

    val rol: RolInfo? = null,
    val estado: EstadoInfo? = null,

    @SerializedName("duocVip")
    val duocVip: Boolean? = null
) {
    // Helper para obtener nombre completo
    val nombre: String
        get() = listOfNotNull(pnombre, snombre, papellido)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifEmpty { "Usuario" }

    val telefono: String?
        get() = ntelefono

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
 * DTO de Propiedad
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
    val nHabit: Int? = null,

    @SerializedName("nBanos")
    val nBanos: Int? = null,

    @SerializedName("petFriendly")
    val petFriendly: Boolean,

    @SerializedName("tipoId")
    val tipoId: Int,

    @SerializedName("comunaId")
    val comunaId: Int,

    @SerializedName("propietarioId")
    val propietarioId: Long? = null,

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