package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.entities.SolicitudEntity
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.repository.ApplicationRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data class para solicitud con datos enriquecidos
 */
data class SolicitudConDatos(
    val solicitud: SolicitudEntity,
    val tituloPropiedad: String?,
    val codigoPropiedad: String?,
    val nombreEstado: String?
)

/**
 * ✅ VIEWMODEL CORREGIDO: Integrado con backend real
 */
class SolicitudesViewModel(
    private val solicitudDao: SolicitudDao,
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val remoteRepository: ApplicationRemoteRepository  // ✅ AÑADIDO
) : ViewModel() {

    private val _solicitudes = MutableStateFlow<List<SolicitudConDatos>>(emptyList())
    val solicitudes: StateFlow<List<SolicitudConDatos>> = _solicitudes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg

    private val _solicitudCreada = MutableStateFlow(false)
    val solicitudCreada: StateFlow<Boolean> = _solicitudCreada

    /**
     * ✅ CORREGIDO: Cargar solicitudes desde el servidor
     */
    fun cargarSolicitudesUsuario(usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                // ✅ Llamar al backend
                when (val result = remoteRepository.obtenerSolicitudesUsuario(usuarioId)) {
                    is ApiResult.Success -> {
                        // Mapear a entities locales con datos enriquecidos
                        val solicitudesConDatos = result.data.map { dto ->
                            // Buscar datos locales de la propiedad
                            val propiedad = propiedadDao.getById(dto.propiedadId)

                            // Mapear estado (el backend devuelve "PENDIENTE", "ACEPTADA", etc.)
                            val estadoNombre = dto.estado ?: "PENDIENTE"

                            SolicitudConDatos(
                                solicitud = SolicitudEntity(
                                    id = dto.id ?: 0L,
                                    fsolicitud = dto.fechaSolicitud?.time ?: System.currentTimeMillis(),
                                    total = 0, // No usado
                                    usuarios_id = usuarioId,
                                    estado_id = 1L, // Pendiente
                                    propiedad_id = dto.propiedadId
                                ),
                                tituloPropiedad = dto.propiedad?.titulo ?: propiedad?.titulo,
                                codigoPropiedad = dto.propiedad?.codigo ?: propiedad?.codigo,
                                nombreEstado = estadoNombre
                            )
                        }

                        _solicitudes.value = solicitudesConDatos
                    }
                    is ApiResult.Error -> {
                        _errorMsg.value = result.message
                    }
                    is ApiResult.Loading -> { /* No hacer nada */ }
                }
            } catch (e: Exception) {
                _errorMsg.value = "Error al cargar solicitudes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * ✅ CORREGIDO: Crear solicitud en el servidor
     */
    fun crearSolicitud(
        usuarioId: Long,
        propiedadId: Long
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                when (val result = remoteRepository.crearSolicitudRemota(usuarioId, propiedadId)) {
                    is ApiResult.Success -> {
                        _solicitudCreada.value = true
                        // Recargar solicitudes para actualizar UI
                        cargarSolicitudesUsuario(usuarioId)
                    }
                    is ApiResult.Error -> {
                        _errorMsg.value = result.message
                    }
                    is ApiResult.Loading -> { /* No hacer nada */ }
                }
            } catch (e: Exception) {
                _errorMsg.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Limpiar flag de solicitud creada
     */
    fun clearSolicitudCreada() {
        _solicitudCreada.value = false
    }

    /**
     * Limpiar mensaje de error
     */
    fun clearError() {
        _errorMsg.value = null
    }
}