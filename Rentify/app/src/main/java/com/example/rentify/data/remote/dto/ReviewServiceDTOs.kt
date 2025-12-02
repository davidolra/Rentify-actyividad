package com.example.rentify.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.util.Date

// ==================== REVIEW SERVICE DTOs ====================

/**
 * DTO para Reseña/Valoración
 * ✅ Compatible 100% con backend Review Service (Puerto 8086)
 */
data class ResenaDTO(
    val id: Long? = null,

    @SerializedName("usuarioId")
    val usuarioId: Long,

    @SerializedName("propiedadId")
    val propiedadId: Long? = null,

    @SerializedName("usuarioResenadoId")
    val usuarioResenadoId: Long? = null,

    val puntaje: Int,  // 1-10

    val comentario: String? = null,

    @SerializedName("tipoResenaId")
    val tipoResenaId: Long,

    @SerializedName("fechaResena")
    val fechaResena: Date? = null,

    @SerializedName("fechaBaneo")
    val fechaBaneo: Date? = null,

    val estado: String? = null,  // ACTIVA, BANEADA, OCULTA

    // Relaciones opcionales (solo con includeDetails=true)
    val usuario: UsuarioDTO? = null,
    val propiedad: PropiedadDTO? = null,
    val usuarioResenado: UsuarioDTO? = null,
    val tipoResenaNombre: String? = null
)

/**
 * DTO para Tipo de Reseña
 * ✅ Compatible 100% con backend
 */
data class TipoResenaDTO(
    val id: Long? = null,
    val nombre: String  // RESENA_PROPIEDAD, RESENA_USUARIO, etc.
)

/**
 * DTO para respuesta de promedio de calificaciones
 */
data class PromedioCalificacionDTO(
    val promedio: Double,
    val totalResenas: Int
)

/**
 * Request DTO para crear reseña de propiedad
 */
data class CrearResenaRequest(
    val usuarioId: Long,
    val propiedadId: Long?,
    val usuarioResenadoId: Long?,
    val puntaje: Int,
    val comentario: String?,
    val tipoResenaId: Long
)