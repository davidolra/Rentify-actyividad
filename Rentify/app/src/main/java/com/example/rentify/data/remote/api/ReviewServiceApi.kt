package com.example.rentify.data.remote.api

import com.example.rentify.data.remote.dto.ResenaDTO
import com.example.rentify.data.remote.dto.TipoResenaDTO
import retrofit2.Response
import retrofit2.http.*

/**
 * API para comunicación con Review Service (Puerto 8086)
 * Coincide exactamente con los controllers del backend
 */
interface ReviewServiceApi {

    // ==================== RESEÑAS ====================

    /**
     * Crear nueva reseña
     * POST /api/reviews
     */
    @POST("api/reviews")
    suspend fun crearResena(
        @Body resena: ResenaDTO
    ): Response<ResenaDTO>

    /**
     * Listar todas las reseñas
     * GET /api/reviews
     */
    @GET("api/reviews")
    suspend fun listarTodasResenas(
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<ResenaDTO>>

    /**
     * Obtener reseña por ID
     * GET /api/reviews/{id}
     */
    @GET("api/reviews/{id}")
    suspend fun obtenerResenaPorId(
        @Path("id") id: Long,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<ResenaDTO>

    /**
     * Obtener reseñas por usuario (creadas por el usuario)
     * GET /api/reviews/usuario/{usuarioId}
     */
    @GET("api/reviews/usuario/{usuarioId}")
    suspend fun obtenerResenasPorUsuario(
        @Path("usuarioId") usuarioId: Long,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<ResenaDTO>>

    /**
     * Obtener reseñas por propiedad
     * GET /api/reviews/propiedad/{propiedadId}
     */
    @GET("api/reviews/propiedad/{propiedadId}")
    suspend fun obtenerResenasPorPropiedad(
        @Path("propiedadId") propiedadId: Long,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<ResenaDTO>>

    /**
     * Obtener reseñas sobre un usuario (reseñas que han escrito sobre él)
     * GET /api/reviews/usuario-resenado/{usuarioResenadoId}
     */
    @GET("api/reviews/usuario-resenado/{usuarioResenadoId}")
    suspend fun obtenerResenasSobreUsuario(
        @Path("usuarioResenadoId") usuarioResenadoId: Long,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<ResenaDTO>>

    /**
     * Calcular promedio de reseñas de propiedad
     * GET /api/reviews/propiedad/{propiedadId}/promedio
     */
    @GET("api/reviews/propiedad/{propiedadId}/promedio")
    suspend fun calcularPromedioPorPropiedad(
        @Path("propiedadId") propiedadId: Long
    ): Response<Double>

    /**
     * Calcular promedio de reseñas de usuario
     * GET /api/reviews/usuario-resenado/{usuarioResenadoId}/promedio
     */
    @GET("api/reviews/usuario-resenado/{usuarioResenadoId}/promedio")
    suspend fun calcularPromedioPorUsuario(
        @Path("usuarioResenadoId") usuarioResenadoId: Long
    ): Response<Double>

    /**
     * Actualizar estado de reseña
     * PATCH /api/reviews/{id}/estado
     */
    @PATCH("api/reviews/{id}/estado")
    suspend fun actualizarEstadoResena(
        @Path("id") id: Long,
        @Query("estado") estado: String  // ACTIVA, BANEADA, OCULTA
    ): Response<ResenaDTO>

    /**
     * Eliminar reseña
     * DELETE /api/reviews/{id}
     */
    @DELETE("api/reviews/{id}")
    suspend fun eliminarResena(
        @Path("id") id: Long
    ): Response<Void>

    // ==================== TIPOS DE RESEÑA ====================

    /**
     * Listar todos los tipos de reseña
     * GET /api/tipo-resenas
     */
    @GET("api/tipo-resenas")
    suspend fun listarTiposResena(): Response<List<TipoResenaDTO>>

    /**
     * Obtener tipo de reseña por ID
     * GET /api/tipo-resenas/{id}
     */
    @GET("api/tipo-resenas/{id}")
    suspend fun obtenerTipoResenaPorId(
        @Path("id") id: Long
    ): Response<TipoResenaDTO>

    /**
     * Crear nuevo tipo de reseña (solo admin)
     * POST /api/tipo-resenas
     */
    @POST("api/tipo-resenas")
    suspend fun crearTipoResena(
        @Body tipoResena: TipoResenaDTO
    ): Response<TipoResenaDTO>

    /**
     * Actualizar tipo de reseña (solo admin)
     * PUT /api/tipo-resenas/{id}
     */
    @PUT("api/tipo-resenas/{id}")
    suspend fun actualizarTipoResena(
        @Path("id") id: Long,
        @Body tipoResena: TipoResenaDTO
    ): Response<TipoResenaDTO>

    /**
     * Eliminar tipo de reseña (solo admin)
     * DELETE /api/tipo-resenas/{id}
     */
    @DELETE("api/tipo-resenas/{id}")
    suspend fun eliminarTipoResena(
        @Path("id") id: Long
    ): Response<Void>
}