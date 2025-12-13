package com.example.rentify.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.entities.SolicitudEntity
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.SolicitudArriendoDTO
import com.example.rentify.data.repository.ApplicationRemoteRepository
import com.example.rentify.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Data class para solicitud con datos enriquecidos
 */
data class SolicitudConDatos(
    val solicitud: SolicitudEntity,
    val tituloPropiedad: String? = null,
    val codigoPropiedad: String? = null,
    val nombreEstado: String? = null,
    val precioMensual: Double? = null,
    val nombreSolicitante: String? = null,
    val emailSolicitante: String? = null,
    val telefonoSolicitante: String? = null,
    val direccionPropiedad: String? = null,
    val solicitudDTO: SolicitudArriendoDTO? = null
)

/**
 * ViewModel para gestion de solicitudes
 */
class SolicitudesViewModel(
    private val solicitudDao: SolicitudDao,
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val remoteRepository: ApplicationRemoteRepository,
    private val propertyRepository: PropertyRemoteRepository? = null
) : ViewModel() {

    companion object {
        private const val TAG = "SolicitudesViewModel"
    }

    private val _solicitudes = MutableStateFlow<List<SolicitudConDatos>>(emptyList())
    val solicitudes: StateFlow<List<SolicitudConDatos>> = _solicitudes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    private val _successMsg = MutableStateFlow<String?>(null)
    val successMsg: StateFlow<String?> = _successMsg.asStateFlow()

    private val _filtroEstado = MutableStateFlow<String?>(null)
    val filtroEstado: StateFlow<String?> = _filtroEstado.asStateFlow()

    /**
     * Cargar solicitudes del arrendatario (usuario logueado)
     */
    fun cargarSolicitudesArrendatario(usuarioId: Long = 1L) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                when (val result = remoteRepository.obtenerSolicitudesUsuario(usuarioId)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Solicitudes cargadas: ${result.data.size}")
                        _solicitudes.value = mapearSolicitudes(result.data)
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error: ${result.message}")
                        _errorMsg.value = result.message
                        cargarSolicitudesLocales()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion: ${e.message}", e)
                _errorMsg.value = e.message
                cargarSolicitudesLocales()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cargar solicitudes del propietario
     */
    fun cargarSolicitudesPropietario(propietarioId: Long = 1L) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                when (val result = remoteRepository.listarTodasSolicitudes()) {
                    is ApiResult.Success -> {
                        // Filtrar por propietario si es necesario
                        val solicitudesFiltradas = result.data.filter { solicitud ->
                            solicitud.propiedad?.propietarioId == propietarioId
                        }
                        _solicitudes.value = mapearSolicitudes(
                            if (solicitudesFiltradas.isEmpty()) result.data else solicitudesFiltradas
                        )
                    }
                    is ApiResult.Error -> {
                        _errorMsg.value = result.message
                        cargarSolicitudesLocales()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _errorMsg.value = e.message
                cargarSolicitudesLocales()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cargar todas las solicitudes (admin)
     */
    fun cargarTodasSolicitudes() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                when (val result = remoteRepository.listarTodasSolicitudes()) {
                    is ApiResult.Success -> {
                        _solicitudes.value = mapearSolicitudes(result.data)
                    }
                    is ApiResult.Error -> {
                        _errorMsg.value = result.message
                        cargarSolicitudesLocales()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _errorMsg.value = e.message
                cargarSolicitudesLocales()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Crear nueva solicitud
     */
    fun crearSolicitud(usuarioId: Long, propiedadId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                when (val result = remoteRepository.crearSolicitudRemota(usuarioId, propiedadId)) {
                    is ApiResult.Success -> {
                        _successMsg.value = "Solicitud creada exitosamente"
                        cargarSolicitudesArrendatario(usuarioId)
                    }
                    is ApiResult.Error -> {
                        _errorMsg.value = result.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _errorMsg.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Aprobar solicitud
     */
    fun aprobarSolicitud(solicitudId: Long) {
        actualizarEstado(solicitudId, "ACEPTADA")
    }

    /**
     * Rechazar solicitud
     */
    fun rechazarSolicitud(solicitudId: Long) {
        actualizarEstado(solicitudId, "RECHAZADA")
    }

    private fun actualizarEstado(solicitudId: Long, nuevoEstado: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                when (val result = remoteRepository.actualizarEstadoSolicitud(solicitudId, nuevoEstado)) {
                    is ApiResult.Success -> {
                        _successMsg.value = "Solicitud ${nuevoEstado.lowercase()}"
                        cargarTodasSolicitudes()
                    }
                    is ApiResult.Error -> {
                        _errorMsg.value = result.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _errorMsg.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Seleccionar solicitud por ID (para detalle)
     */
    fun seleccionarSolicitud(solicitudId: Long) {
        viewModelScope.launch {
            try {
                when (val result = remoteRepository.obtenerSolicitudPorId(solicitudId)) {
                    is ApiResult.Success -> {
                        val solicitudConDatos = mapearSolicitud(result.data)
                        // Actualizar la lista si no existe
                        val listaActual = _solicitudes.value.toMutableList()
                        val index = listaActual.indexOfFirst { it.solicitud.id == solicitudId }
                        if (index >= 0) {
                            listaActual[index] = solicitudConDatos
                        } else {
                            listaActual.add(solicitudConDatos)
                        }
                        _solicitudes.value = listaActual
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al cargar solicitud: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion: ${e.message}")
            }
        }
    }

    fun setFiltroEstado(estado: String?) {
        _filtroEstado.value = estado
    }

    fun clearError() {
        _errorMsg.value = null
    }

    fun clearSuccess() {
        _successMsg.value = null
    }

    private suspend fun cargarSolicitudesLocales() {
        val solicitudesLocales = solicitudDao.getAll()
        _solicitudes.value = solicitudesLocales.map { entity ->
            SolicitudConDatos(
                solicitud = entity,
                nombreEstado = mapEstadoIdToNombre(entity.estado_id)
            )
        }
    }

    private fun mapearSolicitudes(dtos: List<SolicitudArriendoDTO>): List<SolicitudConDatos> {
        return dtos.map { dto -> mapearSolicitud(dto) }
    }

    private fun mapearSolicitud(dto: SolicitudArriendoDTO): SolicitudConDatos {
        val nombreUsuario = dto.usuario?.let { u ->
            listOfNotNull(u.pnombre, u.snombre, u.papellido)
                .filter { it?.isNotBlank() == true }
                .joinToString(" ")
                .ifEmpty { "Usuario" }
        }

        return SolicitudConDatos(
            solicitud = SolicitudEntity(
                id = dto.id ?: 0L,
                fsolicitud = dto.fechaSolicitud?.time ?: System.currentTimeMillis(),
                total = 0,
                usuarios_id = dto.usuarioId,
                estado_id = mapEstadoNombreToId(dto.estado ?: "PENDIENTE"),
                propiedad_id = dto.propiedadId
            ),
            tituloPropiedad = dto.propiedad?.titulo,
            codigoPropiedad = dto.propiedad?.codigo,
            nombreEstado = dto.estado ?: "PENDIENTE",
            precioMensual = dto.propiedad?.precioMensual,
            nombreSolicitante = nombreUsuario,
            emailSolicitante = dto.usuario?.email,
            telefonoSolicitante = dto.usuario?.ntelefono,
            direccionPropiedad = dto.propiedad?.direccion,
            solicitudDTO = dto
        )
    }

    private fun mapEstadoNombreToId(nombre: String): Long {
        return when (nombre.uppercase()) {
            "PENDIENTE" -> 1L
            "ACEPTADA", "APROBADA" -> 2L
            "RECHAZADA" -> 3L
            else -> 1L
        }
    }

    private fun mapEstadoIdToNombre(id: Long): String {
        return when (id) {
            1L -> "PENDIENTE"
            2L -> "ACEPTADA"
            3L -> "RECHAZADA"
            else -> "PENDIENTE"
        }
    }
}