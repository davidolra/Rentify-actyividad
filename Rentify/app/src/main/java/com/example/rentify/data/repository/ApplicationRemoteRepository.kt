package com.example.rentify.data.repository

import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.SolicitudArriendoDTO
import com.example.rentify.data.remote.safeApiCall

class ApplicationRemoteRepository(
    private val solicitudDao: SolicitudDao,
    private val catalogDao: CatalogDao
) {
    private val api = RetrofitClient.applicationServiceApi

    suspend fun crearSolicitudRemota(
        usuarioId: Long,
        propiedadId: Long
    ): ApiResult<SolicitudArriendoDTO> {
        val solicitudDTO = SolicitudArriendoDTO(
            usuarioId = usuarioId,
            propiedadId = propiedadId
        )
        return safeApiCall { api.crearSolicitud(solicitudDTO) }
    }

    suspend fun obtenerSolicitudesUsuario(
        usuarioId: Long
    ): ApiResult<List<SolicitudArriendoDTO>> {
        return safeApiCall { api.obtenerSolicitudesPorUsuario(usuarioId) }
    }

    suspend fun obtenerSolicitudesPorPropiedad(
        propiedadId: Long
    ): ApiResult<List<SolicitudArriendoDTO>> {
        return safeApiCall { api.obtenerSolicitudesPorPropiedad(propiedadId) }
    }

    suspend fun actualizarEstadoSolicitud(
        solicitudId: Long,
        nuevoEstado: String
    ): ApiResult<SolicitudArriendoDTO> {
        return safeApiCall { api.actualizarEstadoSolicitud(solicitudId, nuevoEstado) }
    }

    suspend fun listarTodasSolicitudes(): ApiResult<List<SolicitudArriendoDTO>> {
        return safeApiCall { api.listarTodasSolicitudes() }
    }

    suspend fun obtenerSolicitudPorId(
        solicitudId: Long
    ): ApiResult<SolicitudArriendoDTO> {
        return safeApiCall { api.obtenerSolicitudPorId(solicitudId) }
    }
}