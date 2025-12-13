package com.example.rentify.data.repository

import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.ResenaDTO
import com.example.rentify.data.remote.dto.TipoResenaDTO
import com.example.rentify.data.remote.safeApiCall

/**
 * ✅ REPOSITORIO PARA REVIEW SERVICE
 * Maneja todas las operaciones relacionadas con reseñas y valoraciones
 */
class ReviewRemoteRepository {

    private val api = RetrofitClient.reviewServiceApi

    // ==================== RESEÑAS ====================

    /**
     * Crear una nueva reseña
     */
    suspend fun crearResena(
        usuarioId: Long,
        propiedadId: Long?,
        usuarioResenadoId: Long?,
        puntuacion: Int,
        comentario: String?,
        tipoResenaId: Long
    ): ApiResult<ResenaDTO> {
        val resenaDTO = ResenaDTO(
            usuarioId = usuarioId,
            propiedadId = propiedadId,
            usuarioResenadoId = usuarioResenadoId,
            puntuacion = puntuacion,
            comentario = comentario,
            tipoResenaId = tipoResenaId
        )

        return safeApiCall { api.crearResena(resenaDTO) }
    }

    /**
     * Obtener todas las reseñas
     */
    suspend fun listarTodasResenas(
        includeDetails: Boolean = false
    ): ApiResult<List<ResenaDTO>> {
        return safeApiCall {
            api.listarTodasResenas(includeDetails)
        }
    }

    /**
     * Obtener reseña por ID
     */
    suspend fun obtenerResenaPorId(
        resenaId: Long,
        includeDetails: Boolean = true
    ): ApiResult<ResenaDTO> {
        return safeApiCall {
            api.obtenerResenaPorId(resenaId, includeDetails)
        }
    }

    /**
     * Obtener reseñas creadas por un usuario
     */
    suspend fun obtenerResenasPorUsuario(
        usuarioId: Long,
        includeDetails: Boolean = false
    ): ApiResult<List<ResenaDTO>> {
        return safeApiCall {
            api.obtenerResenasPorUsuario(usuarioId, includeDetails)
        }
    }

    /**
     * Obtener reseñas de una propiedad
     */
    suspend fun obtenerResenasPorPropiedad(
        propiedadId: Long,
        includeDetails: Boolean = true
    ): ApiResult<List<ResenaDTO>> {
        return safeApiCall {
            api.obtenerResenasPorPropiedad(propiedadId, includeDetails)
        }
    }

    /**
     * Obtener reseñas escritas sobre un usuario
     */
    suspend fun obtenerResenasSobreUsuario(
        usuarioResenadoId: Long,
        includeDetails: Boolean = true
    ): ApiResult<List<ResenaDTO>> {
        return safeApiCall {
            api.obtenerResenasSobreUsuario(usuarioResenadoId, includeDetails)
        }
    }

    /**
     * Calcular promedio de calificación de una propiedad
     */
    suspend fun calcularPromedioPorPropiedad(
        propiedadId: Long
    ): ApiResult<Double> {
        return safeApiCall {
            api.calcularPromedioPorPropiedad(propiedadId)
        }
    }

    /**
     * Calcular promedio de calificación de un usuario
     */
    suspend fun calcularPromedioPorUsuario(
        usuarioResenadoId: Long
    ): ApiResult<Double> {
        return safeApiCall {
            api.calcularPromedioPorUsuario(usuarioResenadoId)
        }
    }

    /**
     * Actualizar estado de una reseña (ACTIVA, BANEADA, OCULTA)
     */
    suspend fun actualizarEstadoResena(
        resenaId: Long,
        nuevoEstado: String
    ): ApiResult<ResenaDTO> {
        return safeApiCall {
            api.actualizarEstadoResena(resenaId, nuevoEstado)
        }
    }

    /**
     * Eliminar una reseña
     */
    suspend fun eliminarResena(
        resenaId: Long
    ): ApiResult<Void> {
        return safeApiCall {
            api.eliminarResena(resenaId)
        }
    }

    // ==================== TIPOS DE RESEÑA ====================

    /**
     * Obtener todos los tipos de reseña disponibles
     */
    suspend fun listarTiposResena(): ApiResult<List<TipoResenaDTO>> {
        return safeApiCall {
            api.listarTiposResena()
        }
    }

    /**
     * Obtener tipo de reseña por ID
     */
    suspend fun obtenerTipoResenaPorId(
        tipoResenaId: Long
    ): ApiResult<TipoResenaDTO> {
        return safeApiCall {
            api.obtenerTipoResenaPorId(tipoResenaId)
        }
    }

    // ==================== HELPERS ====================

    /**
     * Verificar si un usuario puede reseñar una propiedad
     * (no ha creado una reseña previa para esa propiedad)
     */
    suspend fun puedeResenarPropiedad(
        usuarioId: Long,
        propiedadId: Long
    ): Boolean {
        return when (val result = obtenerResenasPorUsuario(usuarioId)) {
            is ApiResult.Success -> {
                // Verificar si ya existe una reseña para esta propiedad
                !result.data.any { it.propiedadId == propiedadId }
            }
            else -> true // En caso de error, permitir intentar
        }
    }

    /**
     * Obtener reseñas activas de una propiedad
     */
    suspend fun obtenerResenasActivasPorPropiedad(
        propiedadId: Long
    ): ApiResult<List<ResenaDTO>> {
        return when (val result = obtenerResenasPorPropiedad(propiedadId, true)) {
            is ApiResult.Success -> {
                val resenasActivas = result.data.filter { it.estado == "ACTIVA" }
                ApiResult.Success(resenasActivas)
            }
            is ApiResult.Error -> result
            else -> ApiResult.Error("Error desconocido")
        }
    }
}