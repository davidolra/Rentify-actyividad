package com.example.rentify.data.remote.api

import com.example.rentify.data.remote.dto.RegistroArriendoDTO
import com.example.rentify.data.remote.dto.SolicitudArriendoDTO
import retrofit2.Response
import retrofit2.http.*

/**
 * API para comunicación con Application Service (Puerto 8084)
 * Coincide exactamente con los controllers del backend
 */
interface ApplicationServiceApi {

    // ==================== SOLICITUDES ====================

    /**
     * Crear nueva solicitud de arriendo
     * POST /api/solicitudes
     */
    @POST("api/solicitudes")
    suspend fun crearSolicitud(
        @Body solicitud: SolicitudArriendoDTO
    ): Response<SolicitudArriendoDTO>

    /**
     * Listar todas las solicitudes
     * GET /api/solicitudes
     */
    @GET("api/solicitudes")
    suspend fun listarTodasSolicitudes(
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<SolicitudArriendoDTO>>

    /**
     * Obtener solicitud por ID
     * GET /api/solicitudes/{id}
     */
    @GET("api/solicitudes/{id}")
    suspend fun obtenerSolicitudPorId(
        @Path("id") id: Long,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<SolicitudArriendoDTO>

    /**
     * Obtener solicitudes por usuario
     * GET /api/solicitudes/usuario/{usuarioId}
     */
    @GET("api/solicitudes/usuario/{usuarioId}")
    suspend fun obtenerSolicitudesPorUsuario(
        @Path("usuarioId") usuarioId: Long
    ): Response<List<SolicitudArriendoDTO>>

    /**
     * Obtener solicitudes por propiedad
     * GET /api/solicitudes/propiedad/{propiedadId}
     */
    @GET("api/solicitudes/propiedad/{propiedadId}")
    suspend fun obtenerSolicitudesPorPropiedad(
        @Path("propiedadId") propiedadId: Long
    ): Response<List<SolicitudArriendoDTO>>

    /**
     * Actualizar estado de solicitud
     * PATCH /api/solicitudes/{id}/estado
     */
    @PATCH("api/solicitudes/{id}/estado")
    suspend fun actualizarEstadoSolicitud(
        @Path("id") id: Long,
        @Query("estado") estado: String  // PENDIENTE, ACEPTADA, RECHAZADA
    ): Response<SolicitudArriendoDTO>

    // ==================== REGISTROS ====================

    /**
     * Crear nuevo registro de arriendo
     * POST /api/registros
     */
    @POST("api/registros")
    suspend fun crearRegistro(
        @Body registro: RegistroArriendoDTO
    ): Response<RegistroArriendoDTO>

    /**
     * Listar todos los registros
     * GET /api/registros
     */
    @GET("api/registros")
    suspend fun listarTodosRegistros(
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<RegistroArriendoDTO>>

    /**
     * Obtener registro por ID
     * GET /api/registros/{id}
     */
    @GET("api/registros/{id}")
    suspend fun obtenerRegistroPorId(
        @Path("id") id: Long,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<RegistroArriendoDTO>

    /**
     * Obtener registros por solicitud
     * GET /api/registros/solicitud/{solicitudId}
     */
    @GET("api/registros/solicitud/{solicitudId}")
    suspend fun obtenerRegistrosPorSolicitud(
        @Path("solicitudId") solicitudId: Long
    ): Response<List<RegistroArriendoDTO>>

    /**
     * Finalizar registro (marcar como inactivo)
     * PATCH /api/registros/{id}/finalizar
     */
    @PATCH("api/registros/{id}/finalizar")
    suspend fun finalizarRegistro(
        @Path("id") id: Long
    ): Response<RegistroArriendoDTO>
}