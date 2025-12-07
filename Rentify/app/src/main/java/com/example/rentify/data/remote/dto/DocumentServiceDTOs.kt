package com.example.rentify.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.util.Date

// ==================== DOCUMENT SERVICE DTOs ====================

/**
 * DTO para Documento
 * Compatible con DocumentoDTO.java del backend
 *
 * NOTA: El campo 'observaciones' requiere actualización del backend.
 * Ver PROPUESTA_BACKEND_OBSERVACIONES.md
 */
data class DocumentoRemoteDTO(
    val id: Long? = null,

    val nombre: String,

    @SerializedName("fechaSubido")
    val fechaSubido: Date? = null,

    @SerializedName("usuarioId")
    val usuarioId: Long,

    @SerializedName("estadoId")
    val estadoId: Long,

    @SerializedName("tipoDocId")
    val tipoDocId: Long,

    // Campos expandidos (cuando includeDetails=true)
    @SerializedName("estadoNombre")
    val estadoNombre: String? = null,

    @SerializedName("tipoDocNombre")
    val tipoDocNombre: String? = null,

    val usuario: UsuarioDocDTO? = null,

    // ✅ NUEVO: Campo para observaciones/motivo de rechazo
    // Requiere actualización del backend (ver documentación)
    @SerializedName("observaciones")
    val observaciones: String? = null,

    // ✅ NUEVO: Fecha de última actualización de estado
    @SerializedName("fechaActualizacion")
    val fechaActualizacion: Date? = null,

    // ✅ NUEVO: ID del admin que revisó el documento
    @SerializedName("revisadoPor")
    val revisadoPor: Long? = null
)

/**
 * DTO de Usuario simplificado para documentos
 */
data class UsuarioDocDTO(
    val id: Long,
    val pnombre: String,
    val papellido: String,
    val email: String,
    val rol: RolInfo? = null,
    val estado: EstadoInfo? = null
) {
    data class RolInfo(
        val id: Long,
        val nombre: String
    )

    data class EstadoInfo(
        val id: Long,
        val nombre: String
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
 * DTO para actualizar estado de documento CON motivo.
 * Usar con PATCH /api/documentos/{id}/estado
 */
data class ActualizarEstadoDocumentoRequest(
    @SerializedName("estadoId")
    val estadoId: Long,

    @SerializedName("observaciones")
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