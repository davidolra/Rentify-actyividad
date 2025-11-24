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
     * ✅ CREAR SOLICITUD - VERSIÓN CORREGIDA
     * Calcula correctamente el total según las reglas de negocio de Rentify
     */
    fun crearSolicitud(
        usuarioId: Long,
        propiedadId: Long
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                // 1. Obtener datos de la propiedad para calcular el total
                val propiedad = propiedadDao.getById(propiedadId)

                if (propiedad == null) {
                    _errorMsg.value = "Propiedad no encontrada"
                    _isLoading.value = false
                    return@launch
                }

                // 2. Calcular el total según reglas de Rentify:
                // Total = Canon mensual + Garantía (1 mes) + Comisión (50% de 1 mes)
                val canonMensual = propiedad.precio_mensual
                val garantia = canonMensual // 1 mes de garantía
                val comision = (canonMensual * 0.5).toInt() // 50% del canon como comisión
                val totalCalculado = canonMensual + garantia + comision

                // 3. Crear DTO para enviar al backend
                val solicitudDTO = SolicitudArriendoDTO(
                    usuarioId = usuarioId,
                    propiedadId = propiedadId
                )

                // 4. Llamar a la API
                val response = RetrofitClient.applicationServiceApi.crearSolicitud(solicitudDTO)

                if (response.isSuccessful && response.body() != null) {
                    val solicitudCreada = response.body()!!

                    // 5. Guardar en BD local con el total calculado correctamente
                    val entity = SolicitudEntity(
                        id = solicitudCreada.id ?: 0L,
                        fsolicitud = System.currentTimeMillis(),
                        total = totalCalculado, // ✅ Total correctamente calculado
                        usuarios_id = usuarioId,
                        estado_id = 1L, // PENDIENTE
                        propiedad_id = propiedadId
                    )

                    solicitudDao.insert(entity)
                    _solicitudCreada.value = true

                    // 6. Recargar solicitudes para actualizar UI
                    cargarSolicitudesUsuario(usuarioId)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMsg.value = "Error al crear solicitud: ${errorBody ?: response.message()}"
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
}