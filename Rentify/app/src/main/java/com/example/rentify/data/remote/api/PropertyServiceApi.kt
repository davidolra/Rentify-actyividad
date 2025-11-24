package com.example.rentify.data.remote.api

import com.example.rentify.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API para comunicación con Property Service (Puerto 8082)
 * Coincide exactamente con los controllers del backend
 */
interface PropertyServiceApi {

    // ==================== PROPIEDADES ====================

    /**
     * Crear nueva propiedad
     * POST /api/propiedades
     */
    @POST("api/propiedades")
    suspend fun crearPropiedad(
        @Body propiedad: PropertyRemoteDTO
    ): Response<PropertyRemoteDTO>

    /**
     * Listar todas las propiedades
     * GET /api/propiedades
     */
    @GET("api/propiedades")
    suspend fun listarTodasPropiedades(
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<PropertyRemoteDTO>>

    /**
     * Obtener propiedad por ID
     * GET /api/propiedades/{id}
     */
    @GET("api/propiedades/{id}")
    suspend fun obtenerPropiedadPorId(
        @Path("id") id: Long,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<PropertyRemoteDTO>

    /**
     * Obtener propiedad por código
     * GET /api/propiedades/codigo/{codigo}
     */
    @GET("api/propiedades/codigo/{codigo}")
    suspend fun obtenerPropiedadPorCodigo(
        @Path("codigo") codigo: String,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<PropertyRemoteDTO>

    /**
     * Actualizar propiedad
     * PUT /api/propiedades/{id}
     */
    @PUT("api/propiedades/{id}")
    suspend fun actualizarPropiedad(
        @Path("id") id: Long,
        @Body propiedad: PropertyRemoteDTO
    ): Response<PropertyRemoteDTO>

    /**
     * Eliminar propiedad
     * DELETE /api/propiedades/{id}
     */
    @DELETE("api/propiedades/{id}")
    suspend fun eliminarPropiedad(
        @Path("id") id: Long
    ): Response<Void>

    /**
     * Buscar propiedades con filtros
     * GET /api/propiedades/buscar
     */
    @GET("api/propiedades/buscar")
    suspend fun buscarPropiedadesConFiltros(
        @Query("comunaId") comunaId: Long? = null,
        @Query("tipoId") tipoId: Long? = null,
        @Query("minPrecio") minPrecio: Double? = null,
        @Query("maxPrecio") maxPrecio: Double? = null,
        @Query("nHabit") nHabit: Int? = null,
        @Query("nBanos") nBanos: Int? = null,
        @Query("petFriendly") petFriendly: Boolean? = null,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<PropertyRemoteDTO>>

    /**
     * Verificar existencia de propiedad
     * GET /api/propiedades/{id}/existe
     */
    @GET("api/propiedades/{id}/existe")
    suspend fun existePropiedad(
        @Path("id") id: Long
    ): Response<Boolean>

    // ==================== FOTOS ====================

    /**
     * Subir foto a propiedad
     * POST /api/propiedades/{id}/fotos
     */
    @Multipart
    @POST("api/propiedades/{id}/fotos")
    suspend fun subirFoto(
        @Path("id") propiedadId: Long,
        @Part file: MultipartBody.Part
    ): Response<FotoRemoteDTO>

    /**
     * Listar fotos de propiedad
     * GET /api/propiedades/{id}/fotos
     */
    @GET("api/propiedades/{id}/fotos")
    suspend fun listarFotos(
        @Path("id") propiedadId: Long
    ): Response<List<FotoRemoteDTO>>

    /**
     * Obtener foto por ID
     * GET /api/fotos/{fotoId}
     */
    @GET("api/fotos/{fotoId}")
    suspend fun obtenerFoto(
        @Path("fotoId") fotoId: Long
    ): Response<FotoRemoteDTO>

    /**
     * Eliminar foto
     * DELETE /api/fotos/{fotoId}
     */
    @DELETE("api/fotos/{fotoId}")
    suspend fun eliminarFoto(
        @Path("fotoId") fotoId: Long
    ): Response<Void>

    /**
     * Reordenar fotos de propiedad
     * PUT /api/propiedades/{id}/fotos/reordenar
     */
    @PUT("api/propiedades/{id}/fotos/reordenar")
    suspend fun reordenarFotos(
        @Path("id") propiedadId: Long,
        @Body fotosIds: List<Long>
    ): Response<Void>

    // ==================== TIPOS ====================

    /**
     * Listar todos los tipos
     * GET /api/tipos
     */
    @GET("api/tipos")
    suspend fun listarTipos(): Response<List<TipoRemoteDTO>>

    /**
     * Obtener tipo por ID
     * GET /api/tipos/{id}
     */
    @GET("api/tipos/{id}")
    suspend fun obtenerTipoPorId(
        @Path("id") id: Long
    ): Response<TipoRemoteDTO>

    // ==================== COMUNAS ====================

    /**
     * Listar todas las comunas
     * GET /api/comunas
     */
    @GET("api/comunas")
    suspend fun listarComunas(): Response<List<ComunaRemoteDTO>>

    /**
     * Obtener comuna por ID
     * GET /api/comunas/{id}
     */
    @GET("api/comunas/{id}")
    suspend fun obtenerComunaPorId(
        @Path("id") id: Long
    ): Response<ComunaRemoteDTO>

    /**
     * Obtener comunas por región
     * GET /api/comunas/region/{regionId}
     */
    @GET("api/comunas/region/{regionId}")
    suspend fun obtenerComunasPorRegion(
        @Path("regionId") regionId: Long
    ): Response<List<ComunaRemoteDTO>>

    // ==================== REGIONES ====================

    /**
     * Listar todas las regiones
     * GET /api/regiones
     */
    @GET("api/regiones")
    suspend fun listarRegiones(): Response<List<RegionRemoteDTO>>

    /**
     * Obtener región por ID
     * GET /api/regiones/{id}
     */
    @GET("api/regiones/{id}")
    suspend fun obtenerRegionPorId(
        @Path("id") id: Long
    ): Response<RegionRemoteDTO>

    // ==================== CATEGORÍAS ====================

    /**
     * Listar todas las categorías
     * GET /api/categorias
     */
    @GET("api/categorias")
    suspend fun listarCategorias(): Response<List<CategoriaRemoteDTO>>

    /**
     * Obtener categoría por ID
     * GET /api/categorias/{id}
     */
    @GET("api/categorias/{id}")
    suspend fun obtenerCategoriaPorId(
        @Path("id") id: Long
    ): Response<CategoriaRemoteDTO>
}