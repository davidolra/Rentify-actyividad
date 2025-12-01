package com.example.rentify.data.repository

import android.util.Log
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.entities.SolicitudEntity
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.RegistroArriendoDTO
import com.example.rentify.data.remote.dto.SolicitudArriendoDTO
import com.example.rentify.data.remote.safeApiCall
import java.util.Date

/**
 * ✅ REPOSITORIO MEJORADO: Con logging y manejo inteligente de errores
 */
class ApplicationRemoteRepository(
    private val solicitudDao: SolicitudDao,
    private val catalogDao: CatalogDao
) {

    private val api = RetrofitClient.applicationServiceApi

    companion object {
        private const val TAG = "AppRemoteRepository"
    }

    // ==================== SOLICITUDES ====================

    /**
     * MEJORADO: Crear solicitud con logging y manejo de errores
     */
    suspend fun crearSolicitudRemota(
        usuarioId: Long,
        propiedadId: Long
    ): ApiResult<SolicitudArriendoDTO> {
        Log.d(TAG, "Creando solicitud: usuarioId=$usuarioId, propiedadId=$propiedadId")

        val solicitudDTO = SolicitudArriendoDTO(
            usuarioId = usuarioId,
            propiedadId = propiedadId
        )

        return when (val result = safeApiCall { api.crearSolicitud(solicitudDTO) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Solicitud creada: id=${result.data.id}, estado=${result.data.estado}")

                // Guardar en BD local
                val entity = SolicitudEntity(
                    id = result.data.id ?: 0L,
                    fsolicitud = result.data.fechaSolicitud?.time ?: System.currentTimeMillis(),
                    total = 0,
                    usuarios_id = usuarioId,
                    estado_id = mapEstadoNombreToId(result.data.estado ?: "PENDIENTE"),
                    propiedad_id = propiedadId
                )
                solicitudDao.insert(entity)
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al crear solicitud: ${result.message}, code=${result.code}")
                // ✅ MEJORAR: Parsear error del backend
                val friendlyMessage = parseBackendError(result.message, result.code)
                ApiResult.Error(friendlyMessage, result.code)
            }
            else -> result
        }
    }

    /**
     * NUEVO: Parsear errores del backend para mostrar mensajes amigables
     */
    private fun parseBackendError(rawMessage: String, code: Int?): String {
        Log.d(TAG, "Parseando error: code=$code, message=$rawMessage")

        return when (code) {
            400 -> {
                // Bad Request - Validaciones de negocio
                when {
                    rawMessage.contains("rol ARRIENDATARIO", ignoreCase = true) ->
                        "Solo usuarios arriendatarios pueden crear solicitudes"
                    rawMessage.contains("máximo permitido", ignoreCase = true) ||
                            rawMessage.contains("solicitudes activas", ignoreCase = true) ->
                        "Has alcanzado el límite de 3 solicitudes activas"
                    rawMessage.contains("solicitud pendiente", ignoreCase = true) ||
                            rawMessage.contains("solicitud duplicada", ignoreCase = true) ->
                        "Ya tienes una solicitud pendiente para esta propiedad"
                    rawMessage.contains("documentos aprobados", ignoreCase = true) ->
                        "Debes tener tus documentos aprobados antes de solicitar"
                    rawMessage.contains("propiedad no existe", ignoreCase = true) ->
                        "La propiedad seleccionada no existe"
                    rawMessage.contains("propiedad no", ignoreCase = true) &&
                            rawMessage.contains("disponible", ignoreCase = true) ->
                        "La propiedad no está disponible para arriendo"
                    rawMessage.contains("usuario no existe", ignoreCase = true) ->
                        "Usuario no encontrado"
                    rawMessage.contains("obligatorio", ignoreCase = true) ->
                        "Faltan datos obligatorios"
                    else -> {
                        Log.w(TAG, "⚠Error 400 no categorizado: $rawMessage")
                        "Error de validación: $rawMessage"
                    }
                }
            }
            404 -> "Recurso no encontrado"
            503 -> "Servicio no disponible. Intenta nuevamente en unos momentos."
            500 -> "Error interno del servidor. Por favor contacta a soporte."
            else -> {
                Log.w(TAG, "Error no categorizado: code=$code, message=$rawMessage")
                rawMessage
            }
        }
    }

    /**
     * Obtener solicitudes del usuario desde el servidor
     */
    suspend fun obtenerSolicitudesUsuario(
        usuarioId: Long
    ): ApiResult<List<SolicitudArriendoDTO>> {
        Log.d(TAG, "Obteniendo solicitudes del usuario: $usuarioId")

        return when (val result = safeApiCall { api.obtenerSolicitudesPorUsuario(usuarioId) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Solicitudes obtenidas: ${result.data.size} solicitudes")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al obtener solicitudes: ${result.message}")
                result
            }
            else -> result
        }
    }

    /**
     * Obtener solicitud por ID con detalles
     */
    suspend fun obtenerSolicitudPorId(
        solicitudId: Long,
        includeDetails: Boolean = true
    ): ApiResult<SolicitudArriendoDTO> {
        Log.d(TAG, "Obteniendo solicitud: id=$solicitudId, includeDetails=$includeDetails")

        return when (val result = safeApiCall {
            api.obtenerSolicitudPorId(solicitudId, includeDetails)
        }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Solicitud obtenida: ${result.data.id}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al obtener solicitud: ${result.message}")
                result
            }
            else -> result
        }
    }

    /**
     * MEJORADO: Actualizar estado de solicitud con logging
     */
    suspend fun actualizarEstadoSolicitud(
        solicitudId: Long,
        nuevoEstado: String
    ): ApiResult<SolicitudArriendoDTO> {
        Log.d(TAG, "Actualizando estado: solicitudId=$solicitudId, nuevoEstado=$nuevoEstado")

        return when (val result = safeApiCall {
            api.actualizarEstadoSolicitud(solicitudId, nuevoEstado)
        }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Estado actualizado: ${result.data.estado}")

                // Actualizar en BD local
                val estadoId = mapEstadoNombreToId(nuevoEstado)
                solicitudDao.cambiarEstado(solicitudId, estadoId)
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al actualizar estado: ${result.message}")
                val friendlyMessage = parseBackendError(result.message, result.code)
                ApiResult.Error(friendlyMessage, result.code)
            }
            else -> result
        }
    }

    /**
     * Obtener solicitudes por propiedad
     */
    suspend fun obtenerSolicitudesPorPropiedad(
        propiedadId: Long
    ): ApiResult<List<SolicitudArriendoDTO>> {
        Log.d(TAG, "Obteniendo solicitudes de propiedad: $propiedadId")

        return safeApiCall {
            api.obtenerSolicitudesPorPropiedad(propiedadId)
        }
    }

    /**
     * Listar todas las solicitudes
     */
    suspend fun listarTodasSolicitudes(
        includeDetails: Boolean = false
    ): ApiResult<List<SolicitudArriendoDTO>> {
        Log.d(TAG, "Listando todas las solicitudes: includeDetails=$includeDetails")

        return safeApiCall {
            api.listarTodasSolicitudes(includeDetails)
        }
    }

    // ==================== REGISTROS ====================

    /**
     * MEJORADO: Crear registro de arriendo con logging
     */
    suspend fun crearRegistro(
        solicitudId: Long,
        fechaInicio: Date,
        montoMensual: Double,
        fechaFin: Date? = null
    ): ApiResult<RegistroArriendoDTO> {
        Log.d(TAG, "Creando registro: solicitudId=$solicitudId, monto=$montoMensual")

        val registroDTO = RegistroArriendoDTO(
            solicitudId = solicitudId,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            montoMensual = montoMensual
        )

        return when (val result = safeApiCall { api.crearRegistro(registroDTO) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Registro creado: id=${result.data.id}, activo=${result.data.activo}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al crear registro: ${result.message}")
                val friendlyMessage = parseBackendError(result.message, result.code)
                ApiResult.Error(friendlyMessage, result.code)
            }
            else -> result
        }
    }

    /**
     * Obtener registro por ID
     */
    suspend fun obtenerRegistroPorId(
        registroId: Long,
        includeDetails: Boolean = true
    ): ApiResult<RegistroArriendoDTO> {
        Log.d(TAG, "Obteniendo registro: id=$registroId")

        return safeApiCall {
            api.obtenerRegistroPorId(registroId, includeDetails)
        }
    }

    /**
     * Obtener registros por solicitud
     */
    suspend fun obtenerRegistrosPorSolicitud(
        solicitudId: Long
    ): ApiResult<List<RegistroArriendoDTO>> {
        Log.d(TAG, "Obteniendo registros de solicitud: $solicitudId")

        return safeApiCall {
            api.obtenerRegistrosPorSolicitud(solicitudId)
        }
    }

    /**
     * Finalizar registro (marcar como inactivo)
     */
    suspend fun finalizarRegistro(
        registroId: Long
    ): ApiResult<RegistroArriendoDTO> {
        Log.d(TAG, "Finalizando registro: id=$registroId")

        return when (val result = safeApiCall { api.finalizarRegistro(registroId) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Registro finalizado: id=${result.data.id}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al finalizar registro: ${result.message}")
                result
            }
            else -> result
        }
    }

    /**
     * Listar todos los registros
     */
    suspend fun listarTodosRegistros(
        includeDetails: Boolean = false
    ): ApiResult<List<RegistroArriendoDTO>> {
        Log.d(TAG, "Listando todos los registros: includeDetails=$includeDetails")

        return safeApiCall {
            api.listarTodosRegistros(includeDetails)
        }
    }

    // ==================== HELPERS ====================

    /**
     * MEJORADO: Mapeo de nombres de estado a IDs con logging
     */
    private suspend fun mapEstadoNombreToId(nombre: String): Long {
        return try {
            val nombreUpper = nombre.uppercase()

            val estadoId = when (nombreUpper) {
                "PENDIENTE" -> catalogDao.getEstadoByNombre("Pendiente")?.id ?: 1L
                "ACEPTADA", "ACEPTADO", "APROBADO" -> catalogDao.getEstadoByNombre("Aprobado")?.id ?: 2L
                "RECHAZADA", "RECHAZADO" -> catalogDao.getEstadoByNombre("Rechazado")?.id ?: 3L
                else -> {
                    Log.w(TAG, "⚠Estado desconocido: $nombre, usando Pendiente")
                    1L
                }
            }

            Log.d(TAG, "Mapeado estado: $nombre -> $estadoId")
            estadoId
        } catch (e: Exception) {
            Log.e(TAG, "Error al mapear estado: ${e.message}")
            1L // Default: Pendiente
        }
    }
}