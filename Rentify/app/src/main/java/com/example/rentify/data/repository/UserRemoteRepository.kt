package com.example.rentify.data.repository

import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.LoginRemoteDTO
import com.example.rentify.data.remote.dto.LoginResponseRemoteDTO
import com.example.rentify.data.remote.dto.UsuarioRemoteDTO
import com.example.rentify.data.remote.safeApiCall
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repositorio para sincronización entre User Service (remoto)
 * y autenticación de usuarios
 */
class UserRemoteRepository {

    private val api = RetrofitClient.userServiceApi

    // ==================== AUTENTICACIÓN ====================

    /**
     * Registra un nuevo usuario en el backend
     */
    suspend fun registrarUsuario(
        pnombre: String,
        snombre: String,
        papellido: String,
        fnacimiento: String,  // Formato: "yyyy-MM-dd"
        email: String,
        rut: String,
        ntelefono: String,
        clave: String,
        rolId: Long? = null  // Opcional, backend asigna ARRIENDATARIO por defecto
    ): ApiResult<UsuarioRemoteDTO> {
        val usuarioDTO = UsuarioRemoteDTO(
            pnombre = pnombre,
            snombre = snombre,
            papellido = papellido,
            fnacimiento = fnacimiento,
            email = email,
            rut = rut,
            ntelefono = ntelefono,
            clave = clave,
            rolId = rolId
        )

        return safeApiCall {
            api.registrarUsuario(usuarioDTO)
        }
    }

    /**
     * Login de usuario
     */
    suspend fun login(
        email: String,
        password: String
    ): ApiResult<LoginResponseRemoteDTO> {
        val loginDTO = LoginRemoteDTO(
            email = email,
            clave = password
        )

        return safeApiCall {
            api.login(loginDTO)
        }
    }

    // ==================== CONSULTAS ====================

    /**
     * Obtiene un usuario por ID
     */
    suspend fun obtenerUsuarioPorId(
        userId: Long,
        includeDetails: Boolean = true
    ): ApiResult<UsuarioRemoteDTO> {
        return safeApiCall {
            api.obtenerUsuarioPorId(userId, includeDetails)
        }
    }

    /**
     * Obtiene un usuario por email
     */
    suspend fun obtenerUsuarioPorEmail(
        email: String,
        includeDetails: Boolean = true
    ): ApiResult<UsuarioRemoteDTO> {
        return safeApiCall {
            api.obtenerUsuarioPorEmail(email, includeDetails)
        }
    }

    /**
     * Obtiene todos los usuarios
     */
    suspend fun obtenerTodosUsuarios(
        includeDetails: Boolean = false
    ): ApiResult<List<UsuarioRemoteDTO>> {
        return safeApiCall {
            api.obtenerTodosUsuarios(includeDetails)
        }
    }

    /**
     * Obtiene usuarios por rol
     */
    suspend fun obtenerUsuariosPorRol(
        rolId: Long,
        includeDetails: Boolean = false
    ): ApiResult<List<UsuarioRemoteDTO>> {
        return safeApiCall {
            api.obtenerUsuariosPorRol(rolId, includeDetails)
        }
    }

    /**
     * Verifica si un usuario existe
     */
    suspend fun existeUsuario(userId: Long): ApiResult<Boolean> {
        return safeApiCall {
            api.existeUsuario(userId)
        }
    }

    // ==================== ACTUALIZACIÓN ====================

    /**
     * Actualiza los datos de un usuario
     */
    suspend fun actualizarUsuario(
        userId: Long,
        usuarioDTO: UsuarioRemoteDTO
    ): ApiResult<UsuarioRemoteDTO> {
        return safeApiCall {
            api.actualizarUsuario(userId, usuarioDTO)
        }
    }

    /**
     * Cambia el rol de un usuario
     */
    suspend fun cambiarRol(
        userId: Long,
        nuevoRolId: Long
    ): ApiResult<UsuarioRemoteDTO> {
        return safeApiCall {
            api.cambiarRol(userId, nuevoRolId)
        }
    }

    /**
     * Agrega puntos RentifyPoints
     */
    suspend fun agregarPuntos(
        userId: Long,
        puntos: Int
    ): ApiResult<UsuarioRemoteDTO> {
        return safeApiCall {
            api.agregarPuntos(userId, puntos)
        }
    }

    // ==================== HELPERS ====================

    /**
     * Convierte LocalDate a String en formato "yyyy-MM-dd"
     */
    fun formatearFecha(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Convierte String "yyyy-MM-dd" a timestamp
     */
    fun parsearFecha(fechaString: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.parse(fechaString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}