package com.example.rentify.data.repository

import android.util.Log
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.LoginRemoteDTO
import com.example.rentify.data.remote.dto.LoginResponseRemoteDTO
import com.example.rentify.data.remote.dto.UsuarioRemoteDTO
import com.example.rentify.data.remote.dto.UsuarioUpdateRemoteDTO
import com.example.rentify.data.remote.safeApiCall
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repositorio con logging detallado para User Service
 */
class UserRemoteRepository {

    private val api = RetrofitClient.userServiceApi

    companion object {
        private const val TAG = "UserRemoteRepository"
    }

    // ==================== AUTENTICACION ====================

    /**
     * Registra un nuevo usuario con logging
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
        Log.d(TAG, "Registrando nuevo usuario")
        Log.d(TAG, "   Email: $email")
        Log.d(TAG, "   Rol: ${rolId ?: "ARRIENDATARIO (default)"}")

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

        return when (val result = safeApiCall { api.registrarUsuario(usuarioDTO) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Usuario registrado exitosamente")
                Log.d(TAG, "   ID: ${result.data.id}")
                Log.d(TAG, "   DUOC VIP: ${result.data.duocVip}")
                Log.d(TAG, "   Codigo Ref: ${result.data.codigoRef}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al registrar usuario: ${result.message}")
                Log.e(TAG, "   Codigo HTTP: ${result.code}")
                val friendlyMessage = parseBackendError(result.message, result.code)
                ApiResult.Error(friendlyMessage, result.code)
            }
            else -> result
        }
    }

    /**
     * Login con logging y parsing de errores
     */
    suspend fun login(
        email: String,
        password: String
    ): ApiResult<LoginResponseRemoteDTO> {
        Log.d(TAG, "Intentando login para: $email")

        val loginDTO = LoginRemoteDTO(
            email = email,
            clave = password
        )

        return when (val result = safeApiCall { api.login(loginDTO) }) {
            is ApiResult.Success -> {
                val usuario = result.data.usuario
                Log.d(TAG, "Login exitoso")
                Log.d(TAG, "   Usuario ID: ${usuario.id}")
                Log.d(TAG, "   Rol: ${usuario.rol?.nombre ?: usuario.rolId}")
                Log.d(TAG, "   Estado: ${usuario.estado?.nombre ?: usuario.estadoId}")
                Log.d(TAG, "   DUOC VIP: ${usuario.duocVip}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error en login: ${result.message}")
                val friendlyMessage = parseBackendError(result.message, result.code)
                ApiResult.Error(friendlyMessage, result.code)
            }
            else -> result
        }
    }

    // ==================== CONSULTAS ====================

    /**
     * Obtiene un usuario por ID con logging
     */
    suspend fun obtenerUsuarioPorId(
        userId: Long,
        includeDetails: Boolean = true
    ): ApiResult<UsuarioRemoteDTO> {
        Log.d(TAG, "Obteniendo usuario: ID=$userId, includeDetails=$includeDetails")

        return when (val result = safeApiCall { api.obtenerUsuarioPorId(userId, includeDetails) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Usuario obtenido: ${result.data.email}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al obtener usuario: ${result.message}")
                result
            }
            else -> result
        }
    }

    /**
     * Obtiene un usuario por email
     */
    suspend fun obtenerUsuarioPorEmail(
        email: String,
        includeDetails: Boolean = true
    ): ApiResult<UsuarioRemoteDTO> {
        Log.d(TAG, "Obteniendo usuario por email: $email")

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
        Log.d(TAG, "Obteniendo todos los usuarios")

        return when (val result = safeApiCall { api.obtenerTodosUsuarios(includeDetails) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "${result.data.size} usuarios obtenidos")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al obtener usuarios: ${result.message}")
                result
            }
            else -> result
        }
    }

    /**
     * Obtiene usuarios por rol
     */
    suspend fun obtenerUsuariosPorRol(
        rolId: Long,
        includeDetails: Boolean = false
    ): ApiResult<List<UsuarioRemoteDTO>> {
        Log.d(TAG, "Obteniendo usuarios con rol: $rolId")

        return safeApiCall {
            api.obtenerUsuariosPorRol(rolId, includeDetails)
        }
    }

    /**
     * Verifica si un usuario existe
     */
    suspend fun existeUsuario(userId: Long): ApiResult<Boolean> {
        Log.d(TAG, "Verificando existencia de usuario: $userId")

        return safeApiCall {
            api.existeUsuario(userId)
        }
    }

    // ==================== ACTUALIZACION ====================

    /**
     * Actualiza usuario con logging
     * Usa UsuarioUpdateRemoteDTO que no requiere campos sensibles
     */
    suspend fun actualizarUsuario(
        userId: Long,
        updateDTO: UsuarioUpdateRemoteDTO
    ): ApiResult<UsuarioRemoteDTO> {
        Log.d(TAG, "Actualizando usuario: $userId")

        return when (val result = safeApiCall { api.actualizarUsuario(userId, updateDTO) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Usuario actualizado exitosamente")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al actualizar usuario: ${result.message}")
                val friendlyMessage = parseBackendError(result.message, result.code)
                ApiResult.Error(friendlyMessage, result.code)
            }
            else -> result
        }
    }

    /**
     * Actualiza usuario desde UsuarioRemoteDTO (convierte internamente)
     */
    suspend fun actualizarUsuarioDesdeRemoteDTO(
        userId: Long,
        usuarioDTO: UsuarioRemoteDTO
    ): ApiResult<UsuarioRemoteDTO> {
        val updateDTO = UsuarioUpdateRemoteDTO(
            pnombre = usuarioDTO.pnombre,
            snombre = usuarioDTO.snombre,
            papellido = usuarioDTO.papellido,
            email = usuarioDTO.email,
            ntelefono = usuarioDTO.ntelefono,
            rolId = usuarioDTO.rolId,
            estadoId = usuarioDTO.estadoId
        )
        return actualizarUsuario(userId, updateDTO)
    }

    /**
     * Cambia el rol de un usuario
     */
    suspend fun cambiarRol(
        userId: Long,
        nuevoRolId: Long
    ): ApiResult<UsuarioRemoteDTO> {
        Log.d(TAG, "Cambiando rol de usuario $userId a rol $nuevoRolId")

        return when (val result = safeApiCall { api.cambiarRol(userId, nuevoRolId) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Rol cambiado exitosamente")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al cambiar rol: ${result.message}")
                result
            }
            else -> result
        }
    }

    /**
     * Cambia el estado de un usuario
     */
    suspend fun cambiarEstado(
        userId: Long,
        nuevoEstadoId: Long
    ): ApiResult<UsuarioRemoteDTO> {
        Log.d(TAG, "Cambiando estado de usuario $userId a estado $nuevoEstadoId")

        return when (val result = safeApiCall { api.cambiarEstado(userId, nuevoEstadoId) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Estado cambiado exitosamente")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al cambiar estado: ${result.message}")
                result
            }
            else -> result
        }
    }

    /**
     * Agrega puntos RentifyPoints
     */
    suspend fun agregarPuntos(
        userId: Long,
        puntos: Int
    ): ApiResult<UsuarioRemoteDTO> {
        Log.d(TAG, "Agregando $puntos puntos al usuario $userId")

        return when (val result = safeApiCall { api.agregarPuntos(userId, puntos) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Puntos agregados. Total: ${result.data.puntos}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al agregar puntos: ${result.message}")
                result
            }
            else -> result
        }
    }

    // ==================== HELPERS ====================

    /**
     * Parsea errores del backend para mostrar mensajes amigables
     */
    private fun parseBackendError(rawMessage: String, code: Int?): String {
        Log.d(TAG, "Parseando error: code=$code")

        return when (code) {
            400 -> {
                when {
                    rawMessage.contains("email", ignoreCase = true) &&
                            rawMessage.contains("registrado", ignoreCase = true) ->
                        "Este correo ya esta registrado. Intenta con otro."

                    rawMessage.contains("RUT", ignoreCase = true) &&
                            rawMessage.contains("registrado", ignoreCase = true) ->
                        "Este RUT ya esta registrado en el sistema."

                    rawMessage.contains("18", ignoreCase = true) ||
                            rawMessage.contains("edad", ignoreCase = true) ->
                        "Debes ser mayor de 18 anos para registrarte."

                    rawMessage.contains("formato", ignoreCase = true) &&
                            rawMessage.contains("RUT", ignoreCase = true) ->
                        "Formato de RUT invalido. Usa el formato: 12345678-9"

                    rawMessage.contains("contrasena", ignoreCase = true) &&
                            rawMessage.contains("8", ignoreCase = true) ->
                        "La contrasena debe tener al menos 8 caracteres."

                    rawMessage.contains("obligatorio", ignoreCase = true) ->
                        "Todos los campos son obligatorios."

                    else -> {
                        Log.w(TAG, "Error 400 no categorizado: $rawMessage")
                        rawMessage
                    }
                }
            }
            401 -> {
                when {
                    rawMessage.contains("incorrectos", ignoreCase = true) ->
                        "Email o contrasena incorrectos. Verifica tus datos."

                    rawMessage.contains("inactiva", ignoreCase = true) ->
                        "Tu cuenta esta inactiva. Contacta al administrador."

                    rawMessage.contains("suspendida", ignoreCase = true) ->
                        "Tu cuenta ha sido suspendida. Contacta a soporte."

                    else -> "No se pudo autenticar. Verifica tus credenciales."
                }
            }
            404 -> "Usuario no encontrado en el sistema."
            500 -> "Error interno del servidor. Por favor intenta mas tarde."
            503 -> "Servicio no disponible. Intenta nuevamente en unos momentos."
            else -> {
                Log.w(TAG, "Error no categorizado: code=$code, message=$rawMessage")
                rawMessage
            }
        }
    }

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