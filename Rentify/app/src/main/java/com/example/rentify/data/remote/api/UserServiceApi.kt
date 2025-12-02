package com.example.rentify.data.remote.api

import com.example.rentify.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface UserServiceApi {

    @POST("api/usuarios")
    suspend fun registrarUsuario(@Body usuario: UsuarioRemoteDTO): Response<UsuarioRemoteDTO>

    @POST("api/usuarios/login")
    suspend fun login(@Body loginDTO: LoginRemoteDTO): Response<LoginResponseRemoteDTO>

    @GET("api/usuarios")
    suspend fun obtenerTodosUsuarios(@Query("includeDetails") includeDetails: Boolean = false): Response<List<UsuarioRemoteDTO>>

    @GET("api/usuarios/{id}")
    suspend fun obtenerUsuarioPorId(@Path("id") id: Long, @Query("includeDetails") includeDetails: Boolean = true): Response<UsuarioRemoteDTO>

    @GET("api/usuarios/email/{email}")
    suspend fun obtenerUsuarioPorEmail(@Path("email") email: String, @Query("includeDetails") includeDetails: Boolean = true): Response<UsuarioRemoteDTO>

    @GET("api/usuarios/rol/{rolId}")
    suspend fun obtenerUsuariosPorRol(@Path("rolId") rolId: Long, @Query("includeDetails") includeDetails: Boolean = false): Response<List<UsuarioRemoteDTO>>

    @GET("api/usuarios/vip")
    suspend fun obtenerUsuariosVIP(@Query("includeDetails") includeDetails: Boolean = false): Response<List<UsuarioRemoteDTO>>

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(@Path("id") id: Long, @Body usuario: UsuarioRemoteDTO): Response<UsuarioRemoteDTO>

    @PATCH("api/usuarios/{id}")
    suspend fun actualizarUsuarioParcial(@Path("id") id: Long, @Body fields: Map<String, @JvmSuppressWildcards Any?>): Response<UsuarioRemoteDTO>

    @DELETE("api/usuarios/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Long): Response<Unit>

    @PATCH("api/usuarios/{id}/rol")
    suspend fun cambiarRol(@Path("id") id: Long, @Query("rolId") rolId: Long): Response<UsuarioRemoteDTO>

    @PATCH("api/usuarios/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Long, @Query("estadoId") estadoId: Long): Response<UsuarioRemoteDTO>

    @PATCH("api/usuarios/{id}/puntos")
    suspend fun agregarPuntos(@Path("id") id: Long, @Query("puntos") puntos: Int): Response<UsuarioRemoteDTO>

    @GET("api/usuarios/{id}/exists")
    suspend fun existeUsuario(@Path("id") id: Long): Response<Boolean>

    @GET("api/roles")
    suspend fun obtenerTodosRoles(): Response<List<RolRemoteDTO>>

    @GET("api/roles/{id}")
    suspend fun obtenerRolPorId(@Path("id") id: Long): Response<RolRemoteDTO>

    @GET("api/roles/nombre/{nombre}")
    suspend fun obtenerRolPorNombre(@Path("nombre") nombre: String): Response<RolRemoteDTO>

    @GET("api/estados")
    suspend fun obtenerTodosEstados(): Response<List<EstadoRemoteDTO>>

    @GET("api/estados/{id}")
    suspend fun obtenerEstadoPorId(@Path("id") id: Long): Response<EstadoRemoteDTO>

    @GET("api/estados/nombre/{nombre}")
    suspend fun obtenerEstadoPorNombre(@Path("nombre") nombre: String): Response<EstadoRemoteDTO>
}
