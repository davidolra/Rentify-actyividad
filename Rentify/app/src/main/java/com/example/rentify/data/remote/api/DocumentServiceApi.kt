package com.example.rentify.data.remote.api

import com.example.rentify.data.remote.dto.DocumentoRemoteDTO
import com.example.rentify.data.remote.dto.EstadoDocumentoDTO
import com.example.rentify.data.remote.dto.TipoDocumentoRemoteDTO
import retrofit2.Response
import retrofit2.http.*

/**
 * API para comunicación con Document Service (Puerto 8083)
 * Coincide exactamente con los controllers del backend
 */
interface DocumentServiceApi {

    // ==================== DOCUMENTOS ====================

    /**
     * Crear/subir nuevo documento
     * POST /api/documentos
     */
    @POST("api/documentos")
    suspend fun crearDocumento(
        @Body documento: DocumentoRemoteDTO
    ): Response<DocumentoRemoteDTO>

    /**
     * Listar todos los documentos
     * GET /api/documentos
     */
    @GET("api/documentos")
    suspend fun listarTodosDocumentos(
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<DocumentoRemoteDTO>>

    /**
     * Obtener documento por ID
     * GET /api/documentos/{id}
     */
    @GET("api/documentos/{id}")
    suspend fun obtenerDocumentoPorId(
        @Path("id") id: Long,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<DocumentoRemoteDTO>

    /**
     * Obtener documentos por usuario
     * GET /api/documentos/usuario/{usuarioId}
     */
    @GET("api/documentos/usuario/{usuarioId}")
    suspend fun obtenerDocumentosPorUsuario(
        @Path("usuarioId") usuarioId: Long,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<List<DocumentoRemoteDTO>>

    /**
     * Verificar si usuario tiene documentos aprobados
     * GET /api/documentos/usuario/{usuarioId}/verificar-aprobados
     */
    @GET("api/documentos/usuario/{usuarioId}/verificar-aprobados")
    suspend fun verificarDocumentosAprobados(
        @Path("usuarioId") usuarioId: Long
    ): Response<Boolean>

    /**
     * Actualizar estado de documento
     * PATCH /api/documentos/{id}/estado/{estadoId}
     */
    @PATCH("api/documentos/{id}/estado/{estadoId}")
    suspend fun actualizarEstadoDocumento(
        @Path("id") id: Long,
        @Path("estadoId") estadoId: Long
    ): Response<DocumentoRemoteDTO>

    /**
     * Eliminar documento
     * DELETE /api/documentos/{id}
     */
    @DELETE("api/documentos/{id}")
    suspend fun eliminarDocumento(
        @Path("id") id: Long
    ): Response<Void>

    // ==================== ESTADOS ====================

    /**
     * Listar todos los estados
     * GET /api/estados
     */
    @GET("api/estados")
    suspend fun listarEstados(): Response<List<EstadoDocumentoDTO>>

    /**
     * Obtener estado por ID
     * GET /api/estados/{id}
     */
    @GET("api/estados/{id}")
    suspend fun obtenerEstadoPorId(
        @Path("id") id: Long
    ): Response<EstadoDocumentoDTO>

    /**
     * Crear nuevo estado
     * POST /api/estados
     */
    @POST("api/estados")
    suspend fun crearEstado(
        @Body estado: EstadoDocumentoDTO
    ): Response<EstadoDocumentoDTO>

    // ==================== TIPOS DE DOCUMENTOS ====================

    /**
     * Listar todos los tipos de documentos
     * GET /api/tipos-documentos
     */
    @GET("api/tipos-documentos")
    suspend fun listarTiposDocumentos(): Response<List<TipoDocumentoRemoteDTO>>

    /**
     * Obtener tipo de documento por ID
     * GET /api/tipos-documentos/{id}
     */
    @GET("api/tipos-documentos/{id}")
    suspend fun obtenerTipoDocumentoPorId(
        @Path("id") id: Long
    ): Response<TipoDocumentoRemoteDTO>

    /**
     * Crear nuevo tipo de documento
     * POST /api/tipos-documentos
     */
    @POST("api/tipos-documentos")
    suspend fun crearTipoDocumento(
        @Body tipoDoc: TipoDocumentoRemoteDTO
    ): Response<TipoDocumentoRemoteDTO>
}