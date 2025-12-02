package com.example.rentify.ui.viewmodel

import android.util.Log
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
    val nombreEstado: String?,
    val precioMensual: Double? = null
)

/**
 * ✅ VIEWMODEL MEJORADO: Con validaciones del lado del cliente y mejor UX
 */
class SolicitudesViewModel(
    private val solicitudDao: SolicitudDao,
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val remoteRepository: ApplicationRemoteRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SolicitudesViewModel"
        private const val ROL_ARRIENDATARIO = 3
        private const val MAX_SOLICITUDES_ACTIVAS = 3
    }

    private val _solicitudes = MutableStateFlow<List<SolicitudConDatos>>(emptyList())
    val solicitudes: StateFlow<List<SolicitudConDatos>> = _solicitudes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg

    private val _solicitudCreada = MutableStateFlow(false)
    val solicitudCreada: StateFlow<Boolean> = _solicitudCreada

    /**
     * ✅ MEJORADO: Cargar solicitudes con mejor mapeo de datos
     */
    fun cargarSolicitudesUsuario(usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                Log.d(TAG, "📥 Cargando solicitudes del usuario: $usuarioId")

                when (val result = remoteRepository.obtenerSolicitudesUsuario(usuarioId)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "✅ Solicitudes cargadas: ${result.data.size}")

                        val solicitudesConDatos = result.data.map { dto ->
                            // Buscar datos locales de la propiedad si no vienen del backend
                            val propiedad = propiedadDao.getById(dto.propiedadId)

                            SolicitudConDatos(
                                solicitud = SolicitudEntity(
                                    id = dto.id ?: 0L,
                                    fsolicitud = dto.fechaSolicitud?.time ?: System.currentTimeMillis(),
                                    total = 0,
                                    usuarios_id = usuarioId,
                                    estado_id = mapEstadoNombreToId(dto.estado ?: "PENDIENTE"),
                                    propiedad_id = dto.propiedadId
                                ),
                                tituloPropiedad = dto.propiedad?.titulo ?: propiedad?.titulo,
                                codigoPropiedad = dto.propiedad?.codigo ?: propiedad?.codigo,
                                nombreEstado = dto.estado ?: "PENDIENTE",
                                precioMensual = dto.propiedad?.precioMensual ?: propiedad?.precio_mensual?.toDouble() // ✅ CORREGIDO
                            )
                        }

                        _solicitudes.value = solicitudesConDatos
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "❌ Error al cargar solicitudes: ${result.message}")
                        _errorMsg.value = result.message
                    }
                    is ApiResult.Loading -> { /* No hacer nada */ }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción al cargar solicitudes: ${e.message}", e)
                _errorMsg.value = "Error al cargar solicitudes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * ✅ NUEVO: Validar si el usuario puede crear una solicitud
     */
    private suspend fun validarPuedeCrearSolicitud(
        usuarioId: Long,
        propiedadId: Long,
        rolId: Int
    ): String? {
        // VALIDACIÓN 1: Verificar rol
        if (rolId != ROL_ARRIENDATARIO) {
            Log.w(TAG, "⚠️ Usuario $usuarioId no tiene rol arriendatario (rol=$rolId)")
            return "Solo usuarios arriendatarios pueden crear solicitudes"
        }

        // VALIDACIÓN 2: Verificar que la propiedad existe localmente
        val propiedad = propiedadDao.getById(propiedadId)
        if (propiedad == null) {
            Log.w(TAG, "⚠️ Propiedad $propiedadId no encontrada localmente")
            return "La propiedad seleccionada no está disponible"
        }

        // VALIDACIÓN 3: Verificar límite de solicitudes activas
        val solicitudesActivas = solicitudDao.countSolicitudesActivas(usuarioId, estadoActivo = 1L)
        if (solicitudesActivas >= MAX_SOLICITUDES_ACTIVAS) {
            Log.w(TAG, "⚠️ Usuario $usuarioId alcanzó el límite: $solicitudesActivas solicitudes")
            return "Ya tienes $MAX_SOLICITUDES_ACTIVAS solicitudes activas. " +
                    "Debes esperar a que se procesen antes de crear más."
        }

        // VALIDACIÓN 4: Verificar solicitud duplicada
        val existePendiente = solicitudDao.existeSolicitudPendiente(usuarioId, propiedadId, 1L)
        if (existePendiente > 0) {
            Log.w(TAG, "⚠️ Ya existe solicitud pendiente: usuario=$usuarioId, propiedad=$propiedadId")
            return "Ya tienes una solicitud pendiente para esta propiedad"
        }

        Log.d(TAG, "✅ Validaciones pasadas para usuario $usuarioId")
        return null // Todo OK
    }

    /**
     * ✅ MEJORADO: Crear solicitud con validaciones previas
     */
    fun crearSolicitud(
        usuarioId: Long,
        propiedadId: Long,
        rolId: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                Log.d(TAG, "🚀 Iniciando creación de solicitud")
                Log.d(TAG, "   Usuario: $usuarioId, Propiedad: $propiedadId, Rol: $rolId")

                // ✅ VALIDACIONES DEL LADO DEL CLIENTE
                val validationError = validarPuedeCrearSolicitud(usuarioId, propiedadId, rolId)
                if (validationError != null) {
                    _errorMsg.value = validationError
                    _isLoading.value = false
                    return@launch
                }

                // ✅ Si pasa todas las validaciones, crear en el backend
                Log.d(TAG, "✅ Validaciones pasadas, enviando al backend...")

                when (val result = remoteRepository.crearSolicitudRemota(usuarioId, propiedadId)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "✅ Solicitud creada exitosamente")
                        _solicitudCreada.value = true
                        // Recargar solicitudes para actualizar UI
                        cargarSolicitudesUsuario(usuarioId)
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "❌ Error del backend: ${result.message}")
                        _errorMsg.value = result.message
                    }
                    is ApiResult.Loading -> { /* No hacer nada */ }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción al crear solicitud: ${e.message}", e)
                _errorMsg.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * ✅ NUEVO: Actualizar estado de una solicitud
     */
    fun actualizarEstadoSolicitud(
        solicitudId: Long,
        nuevoEstado: String,
        usuarioId: Long
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                Log.d(TAG, "🔄 Actualizando estado de solicitud $solicitudId a $nuevoEstado")

                when (val result = remoteRepository.actualizarEstadoSolicitud(solicitudId, nuevoEstado)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "✅ Estado actualizado exitosamente")
                        // Recargar solicitudes
                        cargarSolicitudesUsuario(usuarioId)
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "❌ Error al actualizar estado: ${result.message}")
                        _errorMsg.value = result.message
                    }
                    is ApiResult.Loading -> { /* No hacer nada */ }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción al actualizar estado: ${e.message}", e)
                _errorMsg.value = "Error al actualizar: ${e.message}"
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

    /**
     * ✅ HELPER: Mapear nombre de estado a ID local
     */
    private suspend fun mapEstadoNombreToId(nombre: String): Long {
        return try {
            when (nombre.uppercase()) {
                "PENDIENTE" -> 1L
                "ACEPTADA", "ACEPTADO", "APROBADO" -> 2L
                "RECHAZADA", "RECHAZADO" -> 3L
                else -> 1L
            }
        } catch (e: Exception) {
            1L
        }
    }
}