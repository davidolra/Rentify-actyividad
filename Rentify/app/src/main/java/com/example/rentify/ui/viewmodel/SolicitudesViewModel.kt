package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.entities.SolicitudEntity
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.SolicitudArriendoDTO
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

class SolicitudesViewModel(
    private val solicitudDao: SolicitudDao,
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao
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
     * Cargar solicitudes del usuario
     */
    fun cargarSolicitudesUsuario(usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                solicitudDao.getSolicitudesByUsuario(usuarioId).collect { lista ->

                    val solicitudesConDatos = lista.map { solicitud ->

                        val propiedad = propiedadDao.getById(solicitud.propiedad_id)
                        val estado = catalogDao.getEstadoById(solicitud.estado_id)

                        SolicitudConDatos(
                            solicitud = solicitud,
                            tituloPropiedad = propiedad?.titulo,
                            codigoPropiedad = propiedad?.codigo,
                            nombreEstado = estado?.nombre
                        )
                    }

                    _solicitudes.value = solicitudesConDatos
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMsg.value = "Error al cargar solicitudes: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Crear una nueva solicitud
     */
    // En SolicitudesViewModel.kt - REEMPLAZAR el método crearSolicitud

    fun crearSolicitudRemota(
        usuarioId: Long,
        propiedadId: Long
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                // Llamada directa a la API
                val solicitudDTO = SolicitudArriendoDTO(
                    usuarioId = usuarioId,
                    propiedadId = propiedadId
                )

                val response = RetrofitClient.applicationServiceApi.crearSolicitud(solicitudDTO)

                if (response.isSuccessful && response.body() != null) {
                    val solicitudCreada = response.body()!!

                    // Guardar en BD local
                    val entity = SolicitudEntity(
                        id = solicitudCreada.id ?: 0L,
                        fsolicitud = System.currentTimeMillis(),
                        total = 0, // Calcular según tu lógica
                        usuarios_id = usuarioId,
                        estado_id = 1L, // PENDIENTE
                        propiedad_id = propiedadId
                    )

                    solicitudDao.insert(entity)
                    _solicitudCreada.value = true

                    // Recargar solicitudes
                    cargarSolicitudesUsuario(usuarioId)
                } else {
                    _errorMsg.value = "Error al crear solicitud: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMsg.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSolicitudCreada() {
        _solicitudCreada.value = false
    }
}
