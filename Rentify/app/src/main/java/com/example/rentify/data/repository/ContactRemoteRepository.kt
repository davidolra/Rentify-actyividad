package com.example.rentify.data.repository

import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.MensajeContactoDTO
import com.example.rentify.data.remote.dto.RespuestaMensajeDTO
import com.example.rentify.data.remote.safeApiCall

/**
 * ✅ REPOSITORIO PARA CONTACT SERVICE
 * Maneja todas las operaciones relacionadas con mensajes de contacto
 */
class ContactRemoteRepository {

    private val api = RetrofitClient.contactServiceApi

    // ==================== MENSAJES ====================

    /**
     * Crear un nuevo mensaje de contacto
     */
    suspend fun crearMensaje(
        nombre: String,
        email: String,
        asunto: String,
        mensaje: String,
        numeroTelefono: String? = null,
        usuarioId: Long? = null
    ): ApiResult<MensajeContactoDTO> {
        val mensajeDTO = MensajeContactoDTO(
            nombre = nombre,
            email = email,
            asunto = asunto,
            mensaje = mensaje,
            numeroTelefono = numeroTelefono,
            usuarioId = usuarioId
        )

        return safeApiCall { api.crearMensaje(mensajeDTO) }
    }

    /**
     * Listar todos los mensajes
     */
    suspend fun listarTodosMensajes(
        includeDetails: Boolean = false
    ): ApiResult<List<MensajeContactoDTO>> {
        return safeApiCall {
            api.listarTodosMensajes(includeDetails)
        }
    }

    /**
     * Obtener mensaje por ID
     */
    suspend fun obtenerMensajePorId(
        mensajeId: Long,
        includeDetails: Boolean = true
    ): ApiResult<MensajeContactoDTO> {
        return safeApiCall {
            api.obtenerMensajePorId(mensajeId, includeDetails)
        }
    }

    /**
     * Listar mensajes por email
     */
    suspend fun listarMensajesPorEmail(
        email: String
    ): ApiResult<List<MensajeContactoDTO>> {
        return safeApiCall {
            api.listarMensajesPorEmail(email)
        }
    }

    /**
     * Listar mensajes por usuario autenticado
     */
    suspend fun listarMensajesPorUsuario(
        usuarioId: Long
    ): ApiResult<List<MensajeContactoDTO>> {
        return safeApiCall {
            api.listarMensajesPorUsuario(usuarioId)
        }
    }

    /**
     * Listar mensajes por estado
     */
    suspend fun listarMensajesPorEstado(
        estado: String
    ): ApiResult<List<MensajeContactoDTO>> {
        return safeApiCall {
            api.listarMensajesPorEstado(estado)
        }
    }

    /**
     * Listar mensajes pendientes sin responder
     */
    suspend fun listarMensajesSinResponder(): ApiResult<List<MensajeContactoDTO>> {
        return safeApiCall {
            api.listarMensajesSinResponder()
        }
    }

    /**
     * Buscar mensajes por palabra clave
     */
    suspend fun buscarMensajes(
        keyword: String
    ): ApiResult<List<MensajeContactoDTO>> {
        return safeApiCall {
            api.buscarMensajesPorPalabraClave(keyword)
        }
    }

    /**
     * Actualizar estado de mensaje
     */
    suspend fun actualizarEstado(
        mensajeId: Long,
        nuevoEstado: String
    ): ApiResult<MensajeContactoDTO> {
        return safeApiCall {
            api.actualizarEstadoMensaje(mensajeId, nuevoEstado)
        }
    }

    /**
     * Responder mensaje (solo admin)
     */
    suspend fun responderMensaje(
        mensajeId: Long,
        respuesta: String,
        respondidoPor: Long,
        nuevoEstado: String? = null
    ): ApiResult<MensajeContactoDTO> {
        val respuestaDTO = RespuestaMensajeDTO(
            respuesta = respuesta,
            respondidoPor = respondidoPor,
            nuevoEstado = nuevoEstado
        )

        return safeApiCall {
            api.responderMensaje(mensajeId, respuestaDTO)
        }
    }

    /**
     * Eliminar mensaje (solo admin)
     */
    suspend fun eliminarMensaje(
        mensajeId: Long,
        adminId: Long
    ): ApiResult<Void> {
        return safeApiCall {
            api.eliminarMensaje(mensajeId, adminId)
        }
    }

    /**
     * Obtener estadísticas de mensajes
     */
    suspend fun obtenerEstadisticas(): ApiResult<Map<String, Long>> {
        return safeApiCall {
            api.obtenerEstadisticas()
        }
    }

    // ==================== HELPERS ====================

    /**
     * Verificar si un usuario puede enviar más mensajes
     */
    suspend fun puedeEnviarMensaje(usuarioId: Long): Boolean {
        return when (val result = listarMensajesPorUsuario(usuarioId)) {
            is ApiResult.Success -> {
                val pendientes = result.data.count { it.estado == "PENDIENTE" }
                pendientes < 5 // Límite de 5 mensajes pendientes
            }
            else -> true // En caso de error, permitir
        }
    }

    /**
     * Obtener cantidad de mensajes pendientes por usuario
     */
    suspend fun contarMensajesPendientes(usuarioId: Long): Int {
        return when (val result = listarMensajesPorUsuario(usuarioId)) {
            is ApiResult.Success -> {
                result.data.count { it.estado == "PENDIENTE" }
            }
            else -> 0
        }
    }

    /**
     * Obtener mensajes sin responder (para admins)
     */
    suspend fun obtenerMensajesPendientesAdmin(): ApiResult<List<MensajeContactoDTO>> {
        return listarMensajesSinResponder()
    }
}