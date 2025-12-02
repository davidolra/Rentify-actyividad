package com.example.rentify.data.repository

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
 * ✅ REPOSITORIO CORREGIDO: Simplificado y compatible con backend
 */
class ApplicationRemoteRepository(
    private val solicitudDao: SolicitudDao,
    private val catalogDao: CatalogDao
) {

    private val api = RetrofitClient.applicationServiceApi

    // ==================== SOLICITUDES ====================

    /**
     * ✅ CORREGIDO: Crear solicitud sin calcular total ni mapear estados complejos
     * El backend maneja automáticamente estado y fecha
     */
    suspend fun crearSolicitudRemota(
        usuarioId: Long,
        propiedadId: Long
    ): ApiResult<SolicitudArriendoDTO> {
        val solicitudDTO = SolicitudArriendoDTO(
            usuarioId = usuarioId,
            propiedadId = propiedadId
            // ✅ estado y fechaSolicitud los genera el backend
        )

        return when (val result = safeApiCall { api.crearSolicitud(solicitudDTO) }) {
            is ApiResult.Success -> {
                // ✅ Guardar en BD local con el ID que devuelve el backend
                val entity = SolicitudEntity(
                    id = result.data.id ?: 0L,
                    fsolicitud = result.data.fechaSolicitud?.time ?: System.currentTimeMillis(),
                    total = 0, // ✅ El backend NO usa este campo
                    usuarios_id = usuarioId,
                    estado_id = 1L, // PENDIENTE (estado por defecto)
                    propiedad_id = propiedadId
                )
                solicitudDao.insert(entity)
                result
            }
            else -> result
        }
    }

    /**
     * Obtener solicitudes del usuario desde el servidor
     */
    suspend fun obtenerSolicitudesUsuario(
        usuarioId: Long
    ): ApiResult<List<SolicitudArriendoDTO>> {
        return safeApiCall {
            api.obtenerSolicitudesPorUsuario(usuarioId)
        }
    }

    /**
     * Obtener solicitud por ID con detalles
     */
    suspend fun obtenerSolicitudPorId(
        solicitudId: Long,
        includeDetails: Boolean = true
    ): ApiResult<SolicitudArriendoDTO> {
        return safeApiCall {
            api.obtenerSolicitudPorId(solicitudId, includeDetails)
        }
    }

    /**
     * ✅ CORREGIDO: Actualizar estado de solicitud
     */
    suspend fun actualizarEstadoSolicitud(
        solicitudId: Long,
        nuevoEstado: String  // "PENDIENTE", "ACEPTADA", "RECHAZADA"
    ): ApiResult<SolicitudArriendoDTO> {
        return when (val result = safeApiCall {
            api.actualizarEstadoSolicitud(solicitudId, nuevoEstado)
        }) {
            is ApiResult.Success -> {
                // ✅ Actualizar en BD local
                val estadoId = mapEstadoNombreToId(nuevoEstado)
                solicitudDao.cambiarEstado(solicitudId, estadoId)
                result
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
        return safeApiCall {
            api.listarTodasSolicitudes(includeDetails)
        }
    }

    // ==================== REGISTROS ====================

    /**
     * ✅ CORREGIDO: Crear registro de arriendo con fechas Date
     */
    suspend fun crearRegistro(
        solicitudId: Long,
        fechaInicio: Date,
        montoMensual: Double,
        fechaFin: Date? = null
    ): ApiResult<RegistroArriendoDTO> {
        val registroDTO = RegistroArriendoDTO(
            solicitudId = solicitudId,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            montoMensual = montoMensual
        )

        return safeApiCall { api.crearRegistro(registroDTO) }
    }

    /**
     * Obtener registro por ID
     */
    suspend fun obtenerRegistroPorId(
        registroId: Long,
        includeDetails: Boolean = true
    ): ApiResult<RegistroArriendoDTO> {
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
        return safeApiCall {
            api.finalizarRegistro(registroId)
        }
    }

    /**
     * Listar todos los registros
     */
    suspend fun listarTodosRegistros(
        includeDetails: Boolean = false
    ): ApiResult<List<RegistroArriendoDTO>> {
        return safeApiCall {
            api.listarTodosRegistros(includeDetails)
        }
    }

    // ==================== HELPERS ====================

    /**
     * ✅ CORREGIDO: Mapeo simplificado de nombres de estado a IDs
     */
    private suspend fun mapEstadoNombreToId(nombre: String): Long {
        return try {
            val nombreUpper = nombre.uppercase()

            // Mapeo directo basado en los estados del backend
            when (nombreUpper) {
                "PENDIENTE" -> catalogDao.getEstadoByNombre("Pendiente")?.id ?: 1L
                "ACEPTADA", "ACEPTADO", "APROBADO" -> catalogDao.getEstadoByNombre("Aprobado")?.id ?: 2L
                "RECHAZADA", "RECHAZADO" -> catalogDao.getEstadoByNombre("Rechazado")?.id ?: 3L
                else -> 1L // Default: Pendiente
            }
        } catch (e: Exception) {
            1L // Si hay error, usar Pendiente por defecto
        }
    }
}