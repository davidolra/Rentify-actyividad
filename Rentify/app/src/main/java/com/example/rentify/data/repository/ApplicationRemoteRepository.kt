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
 * Repositorio para comunicacion con Application Service (Puerto 8084)
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
                Log.d(TAG, "Solicitud creada: id=${result.data.id}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error: ${result.message}")
                result
            }
            else -> result
        }
    }

    suspend fun obtenerSolicitudesUsuario(
        usuarioId: Long
    ): ApiResult<List<SolicitudArriendoDTO>> {
        Log.d(TAG, "Obteniendo solicitudes del usuario: $usuarioId")
        return safeApiCall { api.obtenerSolicitudesPorUsuario(usuarioId) }
    }

    suspend fun obtenerSolicitudPorId(
        solicitudId: Long
    ): ApiResult<SolicitudArriendoDTO> {
        Log.d(TAG, "Obteniendo solicitud: id=$solicitudId")
        return safeApiCall { api.obtenerSolicitudPorId(solicitudId, true) }
    }

    suspend fun obtenerSolicitudesPorPropiedad(
        propiedadId: Long
    ): ApiResult<List<SolicitudArriendoDTO>> {
        Log.d(TAG, "Obteniendo solicitudes de propiedad: $propiedadId")
        return safeApiCall { api.obtenerSolicitudesPorPropiedad(propiedadId) }
    }

    suspend fun listarTodasSolicitudes(): ApiResult<List<SolicitudArriendoDTO>> {
        Log.d(TAG, "Listando todas las solicitudes")
        return safeApiCall { api.listarTodasSolicitudes(true) }
    }

    suspend fun actualizarEstadoSolicitud(
        solicitudId: Long,
        nuevoEstado: String
    ): ApiResult<SolicitudArriendoDTO> {
        Log.d(TAG, "Actualizando estado: solicitudId=$solicitudId, nuevoEstado=$nuevoEstado")
        return safeApiCall { api.actualizarEstadoSolicitud(solicitudId, nuevoEstado) }
    }

    // ==================== REGISTROS ====================

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

    suspend fun obtenerRegistroPorId(registroId: Long): ApiResult<RegistroArriendoDTO> {
        return safeApiCall { api.obtenerRegistroPorId(registroId, true) }
    }

    suspend fun obtenerRegistrosPorSolicitud(solicitudId: Long): ApiResult<List<RegistroArriendoDTO>> {
        return safeApiCall { api.obtenerRegistrosPorSolicitud(solicitudId) }
    }

    suspend fun finalizarRegistro(registroId: Long): ApiResult<RegistroArriendoDTO> {
        return safeApiCall { api.finalizarRegistro(registroId) }
    }

    suspend fun listarTodosRegistros(): ApiResult<List<RegistroArriendoDTO>> {
        return safeApiCall { api.listarTodosRegistros(false) }
    }
}