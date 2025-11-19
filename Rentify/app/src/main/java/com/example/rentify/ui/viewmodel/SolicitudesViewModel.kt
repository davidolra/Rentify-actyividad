package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.entities.SolicitudEntity
import com.example.rentify.data.local.entities.EstadoSolicitud
import com.example.rentify.data.local.entities.PropiedadEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Data class para combinar solicitud con información de la propiedad
 */
data class SolicitudConPropiedad(
    val solicitud: SolicitudEntity,
    val propiedad: PropiedadEntity?,
    val nombreComuna: String?
)

/**
 * ViewModel para gestionar solicitudes de arriendo
 */
class SolicitudesViewModel(
    private val solicitudDao: SolicitudDao,
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao
) : ViewModel() {

    private val _solicitudes = MutableStateFlow<List<SolicitudConPropiedad>>(emptyList())
    val solicitudes: StateFlow<List<SolicitudConPropiedad>> = _solicitudes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    // Solo cantidad de solicitudes pendientes
    private val _cantidadPendientes = MutableStateFlow(0)
    val cantidadPendientes: StateFlow<Int> = _cantidadPendientes.asStateFlow()

    /**
     * Cargar solicitudes de un usuario
     */
    fun cargarSolicitudes(usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                // Obtener solicitudes del usuario
                solicitudDao.getSolicitudesByUsuario(usuarioId).collect { lista ->
                    val solicitudesConPropiedad = lista.map { solicitud ->
                        val propiedad = propiedadDao.getById(solicitud.propiedad_id)
                        val nombreComuna = propiedad?.let {
                            catalogDao.getComunaById(it.comuna_id)?.nombre
                        }

                        SolicitudConPropiedad(
                            solicitud = solicitud,
                            propiedad = propiedad,
                            nombreComuna = nombreComuna
                        )
                    }

                    _solicitudes.value = solicitudesConPropiedad
                    actualizarEstadisticas(usuarioId)
                }
            } catch (e: Exception) {
                _errorMsg.value = "Error al cargar solicitudes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Crear nueva solicitud
     */
    suspend fun crearSolicitud(
        usuarioId: Long,
        propiedadId: Long,
        mensaje: String? = null
    ): Result<Long> {
        return try {
            val existe = solicitudDao.existeSolicitudPendiente(usuarioId, propiedadId) > 0

            if (existe) {
                Result.failure(Exception("Ya tienes una solicitud pendiente para esta propiedad"))
            } else {
                val solicitud = SolicitudEntity(
                    propiedad_id = propiedadId,
                    usuario_id = usuarioId,
                    estado = EstadoSolicitud.PENDIENTE,
                    mensaje = mensaje,
                    fecha_solicitud = System.currentTimeMillis()
                )

                val id = solicitudDao.insert(solicitud)
                cargarSolicitudes(usuarioId)
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cancelar/eliminar solicitud
     */
    fun cancelarSolicitud(solicitud: SolicitudEntity, usuarioId: Long) {
        viewModelScope.launch {
            try {
                solicitudDao.delete(solicitud)
                cargarSolicitudes(usuarioId)
            } catch (e: Exception) {
                _errorMsg.value = "Error al cancelar solicitud: ${e.message}"
            }
        }
    }

    /**
     * Actualizar estadísticas de solicitudes (solo pendientes)
     */
    private suspend fun actualizarEstadisticas(usuarioId: Long) {
        _cantidadPendientes.value = solicitudDao.contarPorEstado(usuarioId, EstadoSolicitud.PENDIENTE)
    }

    /**
     * Filtrar solicitudes por estado
     */
    fun filtrarPorEstado(estado: String?) {
        val todasLasSolicitudes = _solicitudes.value

        _solicitudes.value = if (estado == null) {
            todasLasSolicitudes
        } else {
            todasLasSolicitudes.filter { it.solicitud.estado == estado }
        }
    }
}
