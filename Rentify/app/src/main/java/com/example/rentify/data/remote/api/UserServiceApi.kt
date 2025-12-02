package com.example.rentify.data.remote.api

import com.example.rentify.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API para comunicación con User Service (Puerto 8081)
 * Coincide exactamente con los controllers del backend
 */
interface UserServiceApi {

    // ==================== USUARIOS ====================

    /**
     * Registra un nuevo usuario
     * POST /api/usuarios
     */
    @POST("api/usuarios")
    suspend fun registrarUsuario(
        @Body usuario: UsuarioRemoteDTO
    ): Response<UsuarioRemoteDTO>

    /**
     * Login de usuario
     * POST /api/usuarios/login
     */
    @POST("api/usuarios/login")
    suspend fun login(
        @Body loginDTO: LoginRemoteDTO
    ): Response<LoginResponseRemoteDTO>

    /**
     * Obtiene todos los usuarios
     * GET /api/usuarios
     */
    @GET("api/usuarios")
    suspend fun obtenerTodosUsuarios(
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<UsuarioRemoteDTO>>

    /**
     * Obtiene un usuario por ID
     * GET /api/usuarios/{id}
     */
    @GET("api/usuarios/{id}")
    suspend fun obtenerUsuarioPorId(
        @Path("id") id: Long,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<UsuarioRemoteDTO>

    /**
     * Obtiene un usuario por email
     * GET /api/usuarios/email/{email}
     */
    @GET("api/usuarios/email/{email}")
    suspend fun obtenerUsuarioPorEmail(
        @Path("email") email: String,
        @Query("includeDetails") includeDetails: Boolean = true
    ): Response<UsuarioRemoteDTO>

    /**
     * Obtiene usuarios por rol
     * GET /api/usuarios/rol/{rolId}
     */
    @GET("api/usuarios/rol/{rolId}")
    suspend fun obtenerUsuariosPorRol(
        @Path("rolId") rolId: Long,
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<UsuarioRemoteDTO>>

    /**
     * Obtiene usuarios VIP de DUOC
     * GET /api/usuarios/vip
     */
    @GET("api/usuarios/vip")
    suspend fun obtenerUsuariosVIP(
        @Query("includeDetails") includeDetails: Boolean = false
    ): Response<List<UsuarioRemoteDTO>>

    /**
     * Actualiza un usuario
     * PUT /api/usuarios/{id}
     */
    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: Long,
        @Body usuario: UsuarioRemoteDTO
    ): Response<UsuarioRemoteDTO>

    /**
     * Cambia el rol de un usuario
     * PATCH /api/usuarios/{id}/rol
     */
    @PATCH("api/usuarios/{id}/rol")
    suspend fun cambiarRol(
        @Path("id") id: Long,
        @Query("rolId") rolId: Long
    ): Response<UsuarioRemoteDTO>

    /**
     * Cambia el estado de un usuario
     * PATCH /api/usuarios/{id}/estado
     */
    @PATCH("api/usuarios/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") id: Long,
        @Query("estadoId") estadoId: Long
    ): Response<UsuarioRemoteDTO>

    /**
     * Agrega puntos RentifyPoints
     * PATCH /api/usuarios/{id}/puntos
     */
    @PATCH("api/usuarios/{id}/puntos")
    suspend fun agregarPuntos(
        @Path("id") id: Long,
        @Query("puntos") puntos: Int
    ): Response<UsuarioRemoteDTO>

    /**
     * Verifica si un usuario existe
     * GET /api/usuarios/{id}/exists
     */
    @GET("api/usuarios/{id}/exists")
    suspend fun existeUsuario(
        @Path("id") id: Long
    ): Response<Boolean>

    // ==================== ROLES ====================

    /**
     * Obtiene todos los roles
     * GET /api/roles
     */
    @GET("api/roles")
    suspend fun obtenerTodosRoles(): Response<List<RolRemoteDTO>>

    /**
     * Obtiene un rol por ID
     * GET /api/roles/{id}
     */
    @GET("api/roles/{id}")
    suspend fun obtenerRolPorId(
        @Path("id") id: Long
    ): Response<RolRemoteDTO>

    /**
     * Obtiene un rol por nombre
     * GET /api/roles/nombre/{nombre}
     */
    @GET("api/roles/nombre/{nombre}")
    suspend fun obtenerRolPorNombre(
        @Path("nombre") nombre: String
    ): Response<RolRemoteDTO>

    // ==================== ESTADOS ====================

    /**
     * Obtiene todos los estados
     * GET /api/estados
     */
    @GET("api/estados")
    suspend fun obtenerTodosEstados(): Response<List<EstadoRemoteDTO>>

    /**
     * Obtiene un estado por ID
     * GET /api/estados/{id}
     */
    @GET("api/estados/{id}")
    suspend fun obtenerEstadoPorId(
        @Path("id") id: Long
    ): Response<EstadoRemoteDTO>

    /**
     * Obtiene un estado por nombre
     * GET /api/estados/nombre/{nombre}
     */
    @GET("api/estados/nombre/{nombre}")
    suspend fun obtenerEstadoPorNombre(
        @Path("nombre") nombre: String
    ): Response<EstadoRemoteDTO>
}