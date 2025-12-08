package com.example.rentify.data.repository

import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.SolicitudRemoteDTO
import com.example.rentify.data.remote.safeApiCall

class ApplicationRemoteRepository(
    private val solicitudDao: SolicitudDao,
    private val catalogDao: CatalogDao
) {

    private val api = RetrofitClient.applicationServiceApi

    suspend fun crearSolicitud(
        usuarioId: Long,
        propiedadId: Long,
        fechaSolicitud: String,
        mensaje: String? = null
    ): ApiResult<SolicitudRemoteDTO> {
        val solicitudDTO = SolicitudRemoteDTO(
            usuarioId = usuarioId,
            propiedadId = propiedadId,
            fechaSolicitud = fechaSolicitud,
            estadoId = 1L,
            mensaje = mensaje
        )

        return safeApiCall {
            api.crearSolicitud(solicitudDTO)
        }
    }

    suspend fun obtenerSolicitudesPorUsuario(
        usuarioId: Long
    ): ApiResult<List<SolicitudRemoteDTO>> {
        return safeApiCall {
            api.obtenerSolicitudesPorUsuario(usuarioId)
        }
    }

    suspend fun obtenerSolicitudesPorPropietario(
        propietarioId: Long
    ): ApiResult<List<SolicitudRemoteDTO>> {
        return safeApiCall {
            api.obtenerSolicitudesPorPropietario(propietarioId)
        }
    }

    suspend fun obtenerSolicitudPorId(
        solicitudId: Long
    ): ApiResult<SolicitudRemoteDTO> {
        return safeApiCall {
            api.obtenerSolicitudPorId(solicitudId)
        }
    }

    suspend fun actualizarEstadoSolicitud(
        solicitudId: Long,
        nuevoEstadoId: Long
    ): ApiResult<SolicitudRemoteDTO> {
        return safeApiCall {
            api.actualizarEstadoSolicitud(solicitudId, nuevoEstadoId)
        }
    }

    suspend fun eliminarSolicitud(
        solicitudId: Long
    ): ApiResult<Void> {
        return safeApiCall {
            api.eliminarSolicitud(solicitudId)
        }
    }

    suspend fun obtenerSolicitudesPorEstado(
        estadoId: Long
    ): ApiResult<List<SolicitudRemoteDTO>> {
        return safeApiCall {
            api.obtenerSolicitudesPorEstado(estadoId)
        }
    }

    suspend fun obtenerSolicitudesPorPropiedad(
        propiedadId: Long
    ): ApiResult<List<SolicitudRemoteDTO>> {
        return safeApiCall {
            api.obtenerSolicitudesPorPropiedad(propiedadId)
        }
    }
}