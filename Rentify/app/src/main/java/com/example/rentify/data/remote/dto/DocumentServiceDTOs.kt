package com.example.rentify.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.util.Date

// ==================== DOCUMENT SERVICE DTOs ====================

/**
 * DTO para Documento
 * Compatible con DocumentoDTO.java del backend
 */
data class DocumentoRemoteDTO(
    val id: Long? = null,

    val nombre: String? = null,

    @SerializedName("fechaSubido")
    val fechaSubido: Date? = null,

    @SerializedName("usuarioId")
    val usuarioId: Long,

    @SerializedName("estadoId")
    val estadoId: Long,

    @SerializedName("tipoDocId")
    val tipoDocId: Long? = null,

    // Campos para observaciones/motivo de rechazo
    val observaciones: String? = null,

    @SerializedName("fechaActualizacion")
    val fechaActualizacion: Date? = null,

    @SerializedName("revisadoPor")
    val revisadoPor: Long? = null,

    // Campos expandidos (cuando includeDetails=true)
    @SerializedName("estadoNombre")
    val estadoNombre: String? = null,

    @SerializedName("tipoDocNombre")
    val tipoDocNombre: String? = null,

    val usuario: UsuarioDocDTO? = null
)

/**
 * DTO de Usuario simplificado para documentos
 */
data class UsuarioDocDTO(
    val id: Long,
    val pnombre: String? = null,
    val papellido: String? = null,
    val email: String? = null,
    val rol: RolInfo? = null,
    val estado: EstadoInfo? = null
) {
    data class RolInfo(
        val id: Long,
        val nombre: String? = null
    )

    data class EstadoInfo(
        val id: Long,
        val nombre: String? = null
    )
}

/**
 * DTO para Estado de documento
 */
data class EstadoDocumentoDTO(
    val id: Long? = null,
    val nombre: String
)

/**
 * DTO para Tipo de Documento
 */
data class TipoDocumentoRemoteDTO(
    val id: Long? = null,
    val nombre: String
)

/**
 * Request para actualizar estado CON observaciones
 */
data class ActualizarEstadoRequest(
    @SerializedName("estadoId")
    val estadoId: Long,

    val observaciones: String? = null,

    @SerializedName("revisadoPor")
    val revisadoPor: Long? = null
)

/**
 * Respuesta de error del backend Document Service
 */
data class DocumentServiceErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val validationErrors: Map<String, String>? = null
)