package com.example.rentify.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.entities.SolicitudEntity
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.repository.ApplicationRemoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

data class SolicitudConDetalles(
    val solicitud: SolicitudEntity,
    val nombreUsuario: String?,
    val direccionPropiedad: String?,
    val estadoNombre: String?
)

class SolicitudesViewModel(
    private val solicitudDao: SolicitudDao,
    private val applicationRepository: ApplicationRemoteRepository,
    private val context: Context
) : ViewModel() {

    private val userPrefs = UserPreferences(context)

    private val _solicitudes = MutableStateFlow<List<SolicitudConDetalles>>(emptyList())
    val solicitudes: StateFlow<List<SolicitudConDetalles>> = _solicitudes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _filtroEstado = MutableStateFlow<Long?>(null)
    val filtroEstado: StateFlow<Long?> = _filtroEstado.asStateFlow()

    fun cargarSolicitudesArrendatario() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            userPrefs.userId.firstOrNull()?.let { userId ->
                when (val result = applicationRepository.obtenerSolicitudesUsuario(userId)) {
                    is ApiResult.Success -> {
                        result.data.forEach { dto ->
                            val entity = SolicitudEntity(
                                id = dto.id ?: 0L,
                                fsolicitud = dto.fechaSolicitud?.time ?: System.currentTimeMillis(),
                                total = 0,
                                usuarios_id = dto.usuarioId,
                                estado_id = mapEstadoToId(dto.estado),
                                propiedad_id = dto.propiedadId
                            )
                            solicitudDao.insert(entity)
                        }
                        cargarSolicitudesLocales(userId, true)
                    }
                    is ApiResult.Error -> {
                        _error.value = result.message
                        cargarSolicitudesLocales(userId, true)
                    }
                    is ApiResult.Loading -> {}
                }
            } ?: run {
                _error.value = "Usuario no autenticado"
            }
            _isLoading.value = false
        }
    }

    fun cargarSolicitudesPropietario() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            userPrefs.userId.firstOrNull()?.let { userId ->
                when (val result = applicationRepository.obtenerSolicitudesPorPropiedad(userId)) {
                    is ApiResult.Success -> {
                        result.data.forEach { dto ->
                            val entity = SolicitudEntity(
                                id = dto.id ?: 0L,
                                fsolicitud = dto.fechaSolicitud?.time ?: System.currentTimeMillis(),
                                total = 0,
                                usuarios_id = dto.usuarioId,
                                estado_id = mapEstadoToId(dto.estado),
                                propiedad_id = dto.propiedadId
                            )
                            solicitudDao.insert(entity)
                        }
                        cargarSolicitudesLocales(userId, false)
                    }
                    is ApiResult.Error -> {
                        _error.value = result.message
                        cargarSolicitudesLocales(userId, false)
                    }
                    is ApiResult.Loading -> {}
                }
            } ?: run {
                _error.value = "Usuario no autenticado"
            }
            _isLoading.value = false
        }
    }

    private suspend fun cargarSolicitudesLocales(usuarioId: Long, esArrendatario: Boolean) {
        solicitudDao.getSolicitudesByUsuario(usuarioId).firstOrNull()?.let { lista ->
            val filtro = _filtroEstado.value
            val filtradas = if (filtro != null) lista.filter { it.estado_id == filtro } else lista

            _solicitudes.value = filtradas.map { solicitud ->
                SolicitudConDetalles(
                    solicitud = solicitud,
                    nombreUsuario = "Usuario ${solicitud.usuarios_id}",
                    direccionPropiedad = "Propiedad ${solicitud.propiedad_id}",
                    estadoNombre = mapEstadoToNombre(solicitud.estado_id)
                )
            }
        }
    }

    fun setFiltroEstado(estadoId: Long?) {
        _filtroEstado.value = estadoId
        viewModelScope.launch {
            userPrefs.userId.firstOrNull()?.let { userId ->
                cargarSolicitudesLocales(userId, true)
            }
        }
    }

    fun aprobarSolicitud(solicitudId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = applicationRepository.actualizarEstadoSolicitud(solicitudId, "ACEPTADA")) {
                is ApiResult.Success -> {
                    solicitudDao.cambiarEstado(solicitudId, 2L)
                    userPrefs.userId.firstOrNull()?.let { cargarSolicitudesLocales(it, false) }
                }
                is ApiResult.Error -> _error.value = result.message
                is ApiResult.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    fun rechazarSolicitud(solicitudId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = applicationRepository.actualizarEstadoSolicitud(solicitudId, "RECHAZADA")) {
                is ApiResult.Success -> {
                    solicitudDao.cambiarEstado(solicitudId, 3L)
                    userPrefs.userId.firstOrNull()?.let { cargarSolicitudesLocales(it, false) }
                }
                is ApiResult.Error -> _error.value = result.message
                is ApiResult.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    private fun mapEstadoToId(estado: String?): Long = when(estado) {
        "PENDIENTE" -> 1L
        "ACEPTADA" -> 2L
        "RECHAZADA" -> 3L
        else -> 1L
    }

    private fun mapEstadoToNombre(estadoId: Long): String = when(estadoId) {
        1L -> "Pendiente"
        2L -> "Aprobada"
        3L -> "Rechazada"
        else -> "Desconocido"
    }
}