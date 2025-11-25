package com.example.rentify.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.util.Date

// ==================== CONTACT SERVICE DTOs ====================

/**
 * DTO para Mensaje de Contacto
 * ✅ Compatible 100% con backend Contact Service (Puerto 8085)
 */
data class MensajeContactoDTO(
    val id: Long? = null,

    val nombre: String,
    val email: String,
    val asunto: String,
    val mensaje: String,

    @SerializedName("numeroTelefono")
    val numeroTelefono: String? = null,

    @SerializedName("usuarioId")
    val usuarioId: Long? = null,

    val estado: String? = null,  // PENDIENTE, EN_PROCESO, RESUELTO

    @SerializedName("fechaCreacion")
    val fechaCreacion: Date? = null,

    @SerializedName("fechaActualizacion")
    val fechaActualizacion: Date? = null,

    val respuesta: String? = null,

    @SerializedName("respondidoPor")
    val respondidoPor: Long? = null,

    // Relación opcional (solo con includeDetails=true)
    val usuario: UsuarioDTO? = null
)

/**
 * DTO para Respuesta de Mensaje
 * ✅ Usado por admins para responder mensajes
 */
data class RespuestaMensajeDTO(
    val respuesta: String,

    @SerializedName("respondidoPor")
    val respondidoPor: Long,

    @SerializedName("nuevoEstado")
    val nuevoEstado: String? = null  // PENDIENTE, EN_PROCESO, RESUELTO
)

/**
 * DTO para crear mensaje simplificado
 */
data class CrearMensajeContactoRequest(
    val nombre: String,
    val email: String,
    val asunto: String,
    val mensaje: String,
    val numeroTelefono: String? = null,
    val usuarioId: Long? = null
)

/**
 * DTO para estadísticas de mensajes
 */
data class EstadisticasMensajesDTO(
    val total: Long,
    val pendientes: Long,
    val enProceso: Long,
    val resueltos: Long
)