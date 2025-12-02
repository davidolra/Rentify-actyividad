package com.example.rentify.data.repository

import android.util.Log
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.LoginRemoteDTO
import com.example.rentify.data.remote.dto.LoginResponseRemoteDTO
import com.example.rentify.data.remote.dto.UsuarioRemoteDTO
import com.example.rentify.data.remote.safeApiCall
import java.text.SimpleDateFormat
import java.util.*

class UserRemoteRepository {

    private val api = RetrofitClient.userServiceApi

    companion object {
        private const val TAG = "UserRemoteRepository"
    }

    suspend fun registrarUsuario(
        pnombre: String,
        snombre: String,
        papellido: String,
        fnacimiento: String,
        email: String,
        rut: String,
        ntelefono: String,
        clave: String,
        rolId: Long? = null
    ): ApiResult<UsuarioRemoteDTO> {
        Log.d(TAG, "🚀 Registrando nuevo usuario")
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
                Log.d(TAG, "Usuario registrado exitosamente: ID=${result.data.id}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al registrar usuario: ${result.message}")
                val friendlyMessage = parseBackendError(result.message, result.code)
                ApiResult.Error(friendlyMessage, result.code)
            }
            else -> result
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): ApiResult<LoginResponseRemoteDTO> {
        Log.d(TAG, "Intentando login para: $email")
        val loginDTO = LoginRemoteDTO(email = email, clave = password)

        return when (val result = safeApiCall { api.login(loginDTO) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Login exitoso para usuario ID: ${result.data.usuario.id}")
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

    suspend fun obtenerUsuarioPorId(
        userId: Long,
        includeDetails: Boolean = true
    ): ApiResult<UsuarioRemoteDTO> {
        Log.d(TAG, "📥 Obteniendo usuario: ID=$userId, includeDetails=$includeDetails")
        return safeApiCall { api.obtenerUsuarioPorId(userId, includeDetails) }
    }

    suspend fun obtenerTodosUsuarios(
        includeDetails: Boolean = false
    ): ApiResult<List<UsuarioRemoteDTO>> {
        Log.d(TAG, "Obteniendo todos los usuarios")
        return safeApiCall { api.obtenerTodosUsuarios(includeDetails) }
    }

    suspend fun actualizarUsuario(userId: Long, updateData: Map<String, Any?>): ApiResult<UsuarioRemoteDTO> {
        Log.d(TAG, "Actualizando usuario: $userId con datos parciales")
        return safeApiCall { api.actualizarUsuarioParcial(userId, updateData) }
    }

    suspend fun eliminarUsuario(userId: Long): ApiResult<Unit> {
        Log.d(TAG, "🗑️ Eliminando usuario: $userId")
        return safeApiCall { api.eliminarUsuario(userId) }
    }

    private fun parseBackendError(rawMessage: String, code: Int?): String {
        return when (code) {
            400 -> "Solicitud incorrecta: $rawMessage"
            401 -> "No autorizado: $rawMessage"
            404 -> "Recurso no encontrado."
            500 -> "Error interno del servidor."
            else -> "Error inesperado: $rawMessage"
        }
    }
}
