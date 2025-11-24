package com.example.rentify.data.repository

import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.entities.SolicitudEntity
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.RegistroArriendoDTO
import com.example.rentify.data.remote.dto.SolicitudArriendoDTO
import com.example.rentify.data.remote.safeApiCall
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repositorio para sincronización entre Application Service (remoto)
 * y base de datos local de solicitudes
 *
 * ✅ VERSIÓN MEJORADA: Consulta dinámica de estados desde la BD
 */
class ApplicationRemoteRepository(
    private val solicitudDao: SolicitudDao,
    private val catalogDao: CatalogDao // ✅ AGREGADO para consultas dinámicas
) {

    private val api = RetrofitClient.applicationServiceApi

    // ==================== SOLICITUDES ====================

    /**
     * Crear solicitud en el servidor y guardar localmente
     */
    suspend fun crearSolicitudRemota(
        usuarioId: Long,
        propiedadId: Long
    ): ApiResult<SolicitudArriendoDTO> {
        val solicitudDTO = SolicitudArriendoDTO(
            usuarioId = usuarioId,
            propiedadId = propiedadId
        )

        return when (val result = safeApiCall { api.crearSolicitud(solicitudDTO) }) {
            is ApiResult.Success -> {
                // Guardar en BD local
                val entity = convertSolicitudDtoToEntity(result.data)
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
     * Actualizar estado de solicitud
     */
    suspend fun actualizarEstadoSolicitud(
        solicitudId: Long,
        nuevoEstado: String  // "PENDIENTE", "ACEPTADA", "RECHAZADA"
    ): ApiResult<SolicitudArriendoDTO> {
        return when (val result = safeApiCall {
            api.actualizarEstadoSolicitud(solicitudId, nuevoEstado)
        }) {
            is ApiResult.Success -> {
                // Actualizar en BD local con consulta dinámica
                val estadoId = getEstadoIdByNombre(nuevoEstado)
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
     * Crear registro de arriendo
     */
    suspend fun crearRegistro(
        solicitudId: Long,
        fechaInicio: String,  // Formato: "yyyy-MM-dd"
        montoMensual: Double,
        fechaFin: String? = null
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
     * Convierte DTO de solicitud a Entity para BD local
     */
    private suspend fun convertSolicitudDtoToEntity(dto: SolicitudArriendoDTO): SolicitudEntity {
        return SolicitudEntity(
            id = dto.id ?: 0L,
            fsolicitud = parseDateStringToTimestamp(dto.fechaSolicitud),
            total = dto.propiedad?.precioMensual?.toInt() ?: 0,
            usuarios_id = dto.usuarioId,
            estado_id = getEstadoIdByNombre(dto.estado ?: "PENDIENTE"),
            propiedad_id = dto.propiedadId
        )
    }

    /**
     * Convierte string de fecha a timestamp
     */
    private fun parseDateStringToTimestamp(dateString: String?): Long {
        return try {
            if (dateString.isNullOrBlank()) return System.currentTimeMillis()

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    /**
     * ✅ MEJORADO: Mapea nombre de estado a ID consultando la BD
     * Ya no usa valores hardcodeados
     */
    private suspend fun getEstadoIdByNombre(nombre: String): Long {
        return try {
            // Consultar desde la base de datos
            val estado = catalogDao.getEstadoByNombre(nombre.uppercase())
            estado?.id ?: run {
                // Fallback: intentar variantes comunes
                when (nombre.uppercase()) {
                    "PENDIENTE" -> catalogDao.getEstadoByNombre("Pendiente")?.id ?: 1L
                    "ACEPTADA", "ACEPTADO" -> catalogDao.getEstadoByNombre("Aprobado")?.id ?: 2L
                    "RECHAZADA", "RECHAZADO" -> catalogDao.getEstadoByNombre("Rechazado")?.id ?: 3L
                    else -> 1L // Default: Pendiente
                }
            }
        } catch (e: Exception) {
            // Si hay error, usar valores por defecto seguros
            1L // Pendiente
        }
    }
}