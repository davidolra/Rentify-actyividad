package com.example.rentify.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==================== REVIEW SERVICE DTOs ====================

/**
 * DTO para respuesta de promedio de calificaciones*/

data class PromedioCalificacionDTO(
    val promedio: Double,
    val totalResenas: Int
)

/**
 * Request DTO para crear reseña de propiedad*/

data class CrearResenaRequest(
    val usuarioId: Long,
    val propiedadId: Long?,
    val usuarioResenadoId: Long?,
    val puntaje: Int,
    val comentario: String?,
    val tipoResenaId: Long
)