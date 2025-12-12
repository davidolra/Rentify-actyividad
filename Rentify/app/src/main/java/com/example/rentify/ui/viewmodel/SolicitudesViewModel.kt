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
    val tituloPropiedad: String?,
    val codigoPropiedad: String?,
    val nombreEstado: String?,
    val precioMensual: Double? = null,
    val nombreSolicitante: String? = null,
    val emailSolicitante: String? = null,
    val telefonoSolicitante: String? = null,
    val direccionPropiedad: String? = null,
    val solicitudDTO: SolicitudArriendoDTO? = null
)

/**
 * ViewModel para gestion de solicitudes multi-rol
 */
class SolicitudesViewModel(
    private val solicitudDao: SolicitudDao,
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val remoteRepository: ApplicationRemoteRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SolicitudesViewModel"
        const val ROL_ADMIN = 1
        const val ROL_PROPIETARIO = 2
        const val ROL_ARRIENDATARIO = 3
        private const val MAX_SOLICITUDES_ACTIVAS = 3
    }

    private val _solicitudes = MutableStateFlow<List<SolicitudConDatos>>(emptyList())
    val solicitudes: StateFlow<List<SolicitudConDatos>> = _solicitudes.asStateFlow()

    private val _solicitudSeleccionada = MutableStateFlow<SolicitudConDatos?>(null)
    val solicitudSeleccionada: StateFlow<SolicitudConDatos?> = _solicitudSeleccionada.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    private val _successMsg = MutableStateFlow<String?>(null)
    val successMsg: StateFlow<String?> = _successMsg.asStateFlow()

    private val _solicitudCreada = MutableStateFlow(false)
    val solicitudCreada: StateFlow<Boolean> = _solicitudCreada.asStateFlow()

    private val _rolActual = MutableStateFlow<Int?>(null)
    val rolActual: StateFlow<Int?> = _rolActual.asStateFlow()

    /**
     * Cargar solicitudes segun el rol del usuario
     */
    fun cargarSolicitudes(usuarioId: Long, rolId: Int) {
        _rolActual.value = rolId

        when (rolId) {
            ROL_ADMIN -> cargarTodasSolicitudes()
            ROL_PROPIETARIO -> cargarSolicitudesPropietario(usuarioId)
            ROL_ARRIENDATARIO -> cargarSolicitudesUsuario(usuarioId)
            else -> cargarSolicitudesUsuario(usuarioId)
        }
    }

    /**
     * Cargar solicitudes del usuario (inquilino)
     */
    fun cargarSolicitudesUsuario(usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                Log.d(TAG, "Cargando solicitudes del usuario: $usuarioId")

                when (val result = remoteRepository.obtenerSolicitudesUsuario(usuarioId)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Solicitudes cargadas: ${result.data.size}")
                        val solicitudesConDatos = mapearSolicitudes(result.data, usuarioId)
                        _solicitudes.value = solicitudesConDatos
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al cargar solicitudes: ${result.message}")
                        _errorMsg.value = result.message
                    }
                    is ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion: ${e.message}", e)
                _errorMsg.value = "Error al cargar solicitudes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cargar solicitudes de propiedades del propietario
     */
    fun cargarSolicitudesPropietario(propietarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                Log.d(TAG, "Cargando solicitudes del propietario: $propietarioId")

                when (val resultSolicitudes = remoteRepository.listarTodasSolicitudes(true)) {
                    is ApiResult.Success -> {
                        val solicitudesFiltradas = resultSolicitudes.data.filter { solicitud ->
                            solicitud.propiedad?.propietarioId == propietarioId
                        }
                        Log.d(TAG, "Solicitudes del propietario: ${solicitudesFiltradas.size}")
                        val solicitudesConDatos = mapearSolicitudes(solicitudesFiltradas, propietarioId)
                        _solicitudes.value = solicitudesConDatos
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error: ${resultSolicitudes.message}")
                        _errorMsg.value = resultSolicitudes.message
                    }
                    else -> {}
                }

            } catch (e: Exception) {
                Log.e(TAG, "Excepcion: ${e.message}", e)
                _errorMsg.value = "Error: ${e.message}"
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
                Log.d(TAG, "Cargando todas las solicitudes (admin)")

                when (val result = remoteRepository.listarTodasSolicitudes(true)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Todas las solicitudes: ${result.data.size}")
                        val solicitudesConDatos = mapearSolicitudes(result.data, 0L)
                        _solicitudes.value = solicitudesConDatos
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error: ${result.message}")
                        _errorMsg.value = result.message
                    }
                    is ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion: ${e.message}", e)
                _errorMsg.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Mapear DTOs a SolicitudConDatos
     */
    private suspend fun mapearSolicitudes(
        dtos: List<SolicitudArriendoDTO>,
        usuarioId: Long
    ): List<SolicitudConDatos> {
        return withContext(Dispatchers.IO) {
            dtos.map { dto ->
                val propiedad = propiedadDao.getById(dto.propiedadId)

                // Obtener nombre del usuario
                val nombreUsuario = dto.usuario?.let { u ->
                    listOfNotNull(u.pnombre, u.snombre, u.papellido)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                        .ifEmpty { "Usuario" }
                }

                SolicitudConDatos(
                    solicitud = SolicitudEntity(
                        id = dto.id ?: 0L,
                        fsolicitud = dto.fechaSolicitud?.time ?: System.currentTimeMillis(),
                        total = 0,
                        usuarios_id = dto.usuarioId,
                        estado_id = mapEstadoNombreToId(dto.estado ?: "PENDIENTE"),
                        propiedad_id = dto.propiedadId
                    ),
                    tituloPropiedad = dto.propiedad?.titulo ?: propiedad?.titulo,
                    codigoPropiedad = dto.propiedad?.codigo ?: propiedad?.codigo,
                    nombreEstado = dto.estado ?: "PENDIENTE",
                    precioMensual = dto.propiedad?.precioMensual ?: propiedad?.precio_mensual?.toDouble(),
                    nombreSolicitante = nombreUsuario,
                    emailSolicitante = dto.usuario?.email,
                    telefonoSolicitante = dto.usuario?.ntelefono,
                    direccionPropiedad = dto.propiedad?.direccion ?: propiedad?.direccion,
                    solicitudDTO = dto
                )
            }
        }
    }

    /**
     * Seleccionar solicitud para ver detalle
     */
    fun seleccionarSolicitud(solicitudId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "Seleccionando solicitud: $solicitudId")

            when (val result = remoteRepository.obtenerSolicitudPorId(solicitudId, true)) {
                is ApiResult.Success -> {
                    val dto = result.data
                    val propiedad = propiedadDao.getById(dto.propiedadId)

                    val nombreUsuario = dto.usuario?.let { u ->
                        listOfNotNull(u.pnombre, u.snombre, u.papellido)
                            .filter { it.isNotBlank() }
                            .joinToString(" ")
                            .ifEmpty { "Usuario" }
                    }

                    _solicitudSeleccionada.value = SolicitudConDatos(
                        solicitud = SolicitudEntity(
                            id = dto.id ?: 0L,
                            fsolicitud = dto.fechaSolicitud?.time ?: System.currentTimeMillis(),
                            total = 0,
                            usuarios_id = dto.usuarioId,
                            estado_id = mapEstadoNombreToId(dto.estado ?: "PENDIENTE"),
                            propiedad_id = dto.propiedadId
                        ),
                        tituloPropiedad = dto.propiedad?.titulo ?: propiedad?.titulo,
                        codigoPropiedad = dto.propiedad?.codigo ?: propiedad?.codigo,
                        nombreEstado = dto.estado ?: "PENDIENTE",
                        precioMensual = dto.propiedad?.precioMensual ?: propiedad?.precio_mensual?.toDouble(),
                        nombreSolicitante = nombreUsuario,
                        emailSolicitante = dto.usuario?.email,
                        telefonoSolicitante = dto.usuario?.ntelefono,
                        direccionPropiedad = dto.propiedad?.direccion ?: propiedad?.direccion,
                        solicitudDTO = dto
                    )
                }
                is ApiResult.Error -> {
                    _errorMsg.value = result.message
                }
                else -> {}
            }
        }
    }

    fun limpiarSeleccion() {
        _solicitudSeleccionada.value = null
    }

    /**
     * Validar si puede crear solicitud
     */
    private suspend fun validarPuedeCrearSolicitud(
        usuarioId: Long,
        propiedadId: Long,
        rolId: Int
    ): String? {
        if (rolId != ROL_ARRIENDATARIO) {
            return "Solo usuarios arriendatarios pueden crear solicitudes"
        }

        val propiedad = propiedadDao.getById(propiedadId)
        if (propiedad == null) {
            return "La propiedad seleccionada no esta disponible"
        }

        val solicitudesActivas = solicitudDao.countSolicitudesActivas(usuarioId, estadoActivo = 1L)
        if (solicitudesActivas >= MAX_SOLICITUDES_ACTIVAS) {
            return "Ya tienes $MAX_SOLICITUDES_ACTIVAS solicitudes activas"
        }

        val existePendiente = solicitudDao.existeSolicitudPendiente(usuarioId, propiedadId, 1L)
        if (existePendiente > 0) {
            return "Ya tienes una solicitud pendiente para esta propiedad"
        }

        return null
    }

    /**
     * Crear solicitud de arriendo
     */
    fun crearSolicitud(usuarioId: Long, propiedadId: Long, rolId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                Log.d(TAG, "Creando solicitud: usuario=$usuarioId, propiedad=$propiedadId")

                val validationError = validarPuedeCrearSolicitud(usuarioId, propiedadId, rolId)
                if (validationError != null) {
                    _errorMsg.value = validationError
                    _isLoading.value = false
                    return@launch
                }

                when (val result = remoteRepository.crearSolicitudRemota(usuarioId, propiedadId)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Solicitud creada: ${result.data.id}")
                        _solicitudCreada.value = true
                        _successMsg.value = "Solicitud enviada exitosamente"
                        cargarSolicitudesUsuario(usuarioId)
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error: ${result.message}")
                        _errorMsg.value = result.message
                    }
                    is ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion: ${e.message}", e)
                _errorMsg.value = "Error de conexion: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Aprobar solicitud (propietario/admin)
     */
    fun aprobarSolicitud(solicitudId: Long, usuarioId: Long, rolId: Int) {
        actualizarEstadoSolicitud(solicitudId, "ACEPTADA", usuarioId, rolId)
    }

    /**
     * Rechazar solicitud con motivo (propietario/admin)
     */
    fun rechazarSolicitud(solicitudId: Long, motivo: String, usuarioId: Long, rolId: Int) {
        actualizarEstadoSolicitud(solicitudId, "RECHAZADA", usuarioId, rolId)
    }

    /**
     * Actualizar estado de solicitud
     */
    fun actualizarEstadoSolicitud(
        solicitudId: Long,
        nuevoEstado: String,
        usuarioId: Long,
        rolId: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                Log.d(TAG, "Actualizando estado: $solicitudId -> $nuevoEstado")

                when (val result = remoteRepository.actualizarEstadoSolicitud(solicitudId, nuevoEstado)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Estado actualizado")
                        _successMsg.value = "Solicitud ${nuevoEstado.lowercase()}"
                        cargarSolicitudes(usuarioId, rolId)
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error: ${result.message}")
                        _errorMsg.value = result.message
                    }
                    is ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion: ${e.message}", e)
                _errorMsg.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Eliminar solicitud (solo admin)
     */
    fun eliminarSolicitud(solicitudId: Long, usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                Log.d(TAG, "Eliminando solicitud: $solicitudId")

                when (val result = remoteRepository.actualizarEstadoSolicitud(solicitudId, "RECHAZADA")) {
                    is ApiResult.Success -> {
                        _successMsg.value = "Solicitud eliminada"
                        cargarTodasSolicitudes()
                    }
                    is ApiResult.Error -> {
                        _errorMsg.value = result.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _errorMsg.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSolicitudCreada() {
        _solicitudCreada.value = false
    }

    fun clearError() {
        _errorMsg.value = null
    }

    fun clearSuccess() {
        _successMsg.value = null
    }

    private fun mapEstadoNombreToId(nombre: String): Long {
        return when (nombre.uppercase()) {
            "PENDIENTE" -> 1L
            "ACEPTADA", "ACEPTADO", "APROBADO", "APROBADA" -> 2L
            "RECHAZADA", "RECHAZADO" -> 3L
            else -> 1L
        }
    }
}