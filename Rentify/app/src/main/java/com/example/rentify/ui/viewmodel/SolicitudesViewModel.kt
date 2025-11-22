package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.entities.SolicitudEntity
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
    fun crearSolicitud(
        usuarioId: Long,
        propiedadId: Long,
        mesesArriendo: Int = 1
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null
            _solicitudCreada.value = false

            try {

                // Validar límite de 3 solicitudes activas
                val estadoPendiente = catalogDao.getEstadoByNombre("Pendiente")
                    ?: throw IllegalStateException("Estado 'Pendiente' no encontrado")

                val activas = solicitudDao.countSolicitudesActivas(
                    usuarioId,
                    estadoPendiente.id
                )

                if (activas >= 3) {
                    _errorMsg.value = "Ya tienes el máximo de 3 solicitudes activas"
                    return@launch
                }

                // Obtener datos de la propiedad
                val propiedad = propiedadDao.getById(propiedadId)
                    ?: throw IllegalStateException("Propiedad no encontrada")

                // Cálculo del total
                val canon = propiedad.precio_mensual * mesesArriendo
                val garantia = propiedad.precio_mensual
                val comision = (propiedad.precio_mensual * 0.10).toInt()
                val total = canon + garantia + comision

                // Crear nueva solicitud
                val solicitud = SolicitudEntity(
                    fsolicitud = System.currentTimeMillis(),
                    total = total,
                    usuarios_id = usuarioId,
                    estado_id = estadoPendiente.id,
                    propiedad_id = propiedadId
                )

                solicitudDao.insert(solicitud)
                _solicitudCreada.value = true

                cargarSolicitudesUsuario(usuarioId)

            } catch (e: Exception) {
                _errorMsg.value = "Error al crear solicitud: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSolicitudCreada() {
        _solicitudCreada.value = false
    }
}
