package com.example.rentify.data.remote.api

import com.example.rentify.data.remote.dto.MensajeContactoDTO
import com.example.rentify.data.remote.dto.RespuestaMensajeDTO
import com.example.rentify.data.remote.dto.EstadisticasMensajesDTO
import retrofit2.Response
import retrofit2.http.*

/**
 * API para comunicación con Contact Service (Puerto 8085)
 * Coincide exactamente con los controllers del backend
 */
interface ContactServiceApi {

    // ==================== MENSAJES DE CONTACTO ====================

    /**
     * Crear nuevo mensaje de contacto
     * POST /api/contacto
     */
    @POST("api/contacto")
    suspend fun crearMensaje(
        @Body mensaje: MensajeContactoDTO
    ): Response<MensajeContactoDTO>

    /**
     * Listar todos los mensajes
     * GET /api/contacto
     */
    @GET("api/contacto")
    suspend fun listarTodosMensajes(
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<MensajeContactoDTO>>

    /**
     * Obtener mensaje por ID
     * GET /api/contacto/{id}
     */
    @GET("api/contacto/{id}")
    suspend fun obtenerMensajePorId(
        @Path("id") id: Long,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<MensajeContactoDTO>

    /**
     * Listar mensajes por email
     * GET /api/contacto/email/{email}
     */
    @GET("api/contacto/email/{email}")
    suspend fun listarMensajesPorEmail(
        @Path("email") email: String
    ): Response<List<MensajeContactoDTO>>

    /**
     * Listar mensajes por usuario
     * GET /api/contacto/usuario/{usuarioId}
     */
    @GET("api/contacto/usuario/{usuarioId}")
    suspend fun listarMensajesPorUsuario(
        @Path("usuarioId") usuarioId: Long
    ): Response<List<MensajeContactoDTO>>

    /**
     * Listar mensajes por estado
     * GET /api/contacto/estado/{estado}
     */
    @GET("api/contacto/estado/{estado}")
    suspend fun listarMensajesPorEstado(
        @Path("estado") estado: String  // PENDIENTE, EN_PROCESO, RESUELTO
    ): Response<List<MensajeContactoDTO>>

    /**
     * Listar mensajes sin responder
     * GET /api/contacto/sin-responder
     */
    @GET("api/contacto/sin-responder")
    suspend fun listarMensajesSinResponder(): Response<List<MensajeContactoDTO>>

    /**
     * Buscar mensajes por palabra clave
     * GET /api/contacto/buscar
     */
    @GET("api/contacto/buscar")
    suspend fun buscarMensajesPorPalabraClave(
        @Query("keyword") keyword: String
    ): Response<List<MensajeContactoDTO>>

    /**
     * Actualizar estado de mensaje
     * PATCH /api/contacto/{id}/estado
     */
    @PATCH("api/contacto/{id}/estado")
    suspend fun actualizarEstadoMensaje(
        @Path("id") id: Long,
        @Query("estado") estado: String  // PENDIENTE, EN_PROCESO, RESUELTO
    ): Response<MensajeContactoDTO>

    /**
     * Responder mensaje de contacto (solo admin)
     * POST /api/contacto/{id}/responder
     */
    @POST("api/contacto/{id}/responder")
    suspend fun responderMensaje(
        @Path("id") id: Long,
        @Body respuesta: RespuestaMensajeDTO
    ): Response<MensajeContactoDTO>

    /**
     * Eliminar mensaje (solo admin)
     * DELETE /api/contacto/{id}
     */
    @DELETE("api/contacto/{id}")
    suspend fun eliminarMensaje(
        @Path("id") id: Long,
        @Query("adminId") adminId: Long
    ): Response<Void>

    /**
     * Obtener estadísticas de mensajes (solo admin)
     * GET /api/contacto/estadisticas
     */
    @GET("api/contacto/estadisticas")
    suspend fun obtenerEstadisticas(): Response<Map<String, Long>>
}