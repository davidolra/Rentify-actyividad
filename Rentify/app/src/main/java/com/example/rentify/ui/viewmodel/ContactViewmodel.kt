package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.MensajeContactoDTO
import com.example.rentify.data.repository.ContactRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para gestión de mensajes de contacto
 * ✅ Integrado con Contact Service (Puerto 8085)
 */
class ContactViewModel(
    private val contactRepository: ContactRemoteRepository
) : ViewModel() {

    // Estado de mensajes
    private val _mensajes = MutableStateFlow<List<MensajeContactoDTO>>(emptyList())
    val mensajes: StateFlow<List<MensajeContactoDTO>> = _mensajes

    // Estado de mensaje seleccionado
    private val _mensajeSeleccionado = MutableStateFlow<MensajeContactoDTO?>(null)
    val mensajeSeleccionado: StateFlow<MensajeContactoDTO?> = _mensajeSeleccionado

    // Estadísticas
    private val _estadisticas = MutableStateFlow<Map<String, Long>>(emptyMap())
    val estadisticas: StateFlow<Map<String, Long>> = _estadisticas

    // Estados de UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    // ==================== CREAR MENSAJE ====================

    /**
     * Crear nuevo mensaje de contacto
     */
    fun crearMensaje(
        nombre: String,
        email: String,
        asunto: String,
        mensaje: String,
        numeroTelefono: String? = null,
        usuarioId: Long? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = contactRepository.crearMensaje(
                nombre = nombre,
                email = email,
                asunto = asunto,
                mensaje = mensaje,
                numeroTelefono = numeroTelefono,
                usuarioId = usuarioId
            )) {
                is ApiResult.Success -> {
                    _successMessage.value = "Mensaje enviado exitosamente. Le responderemos pronto."
                    // Si es usuario autenticado, recargar sus mensajes
                    usuarioId?.let { cargarMensajesPorUsuario(it) }
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {
                    _errorMessage.value = "Error al enviar mensaje"
                }
            }

            _isLoading.value = false
        }
    }

    // ==================== CARGAR MENSAJES ====================

    /**
     * Cargar todos los mensajes (admin)
     */
    fun cargarTodosMensajes() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = contactRepository.listarTodosMensajes(includeDetails = true)) {
                is ApiResult.Success -> {
                    _mensajes.value = result.data
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _mensajes.value = emptyList()
                }
                else -> {
                    _errorMessage.value = "Error al cargar mensajes"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Cargar mensajes por usuario
     */
    fun cargarMensajesPorUsuario(usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = contactRepository.listarMensajesPorUsuario(usuarioId)) {
                is ApiResult.Success -> {
                    _mensajes.value = result.data
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _mensajes.value = emptyList()
                }
                else -> {
                    _errorMessage.value = "Error al cargar mensajes"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Cargar mensajes por email
     */
    fun cargarMensajesPorEmail(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = contactRepository.listarMensajesPorEmail(email)) {
                is ApiResult.Success -> {
                    _mensajes.value = result.data
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _mensajes.value = emptyList()
                }
                else -> {
                    _errorMessage.value = "Error al cargar mensajes"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Cargar mensajes por estado
     */
    fun cargarMensajesPorEstado(estado: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = contactRepository.listarMensajesPorEstado(estado)) {
                is ApiResult.Success -> {
                    _mensajes.value = result.data
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _mensajes.value = emptyList()
                }
                else -> {
                    _errorMessage.value = "Error al cargar mensajes"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Cargar mensajes sin responder (admin)
     */
    fun cargarMensajesSinResponder() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = contactRepository.listarMensajesSinResponder()) {
                is ApiResult.Success -> {
                    _mensajes.value = result.data
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _mensajes.value = emptyList()
                }
                else -> {
                    _errorMessage.value = "Error al cargar mensajes"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Buscar mensajes por palabra clave
     */
    fun buscarMensajes(keyword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = contactRepository.buscarMensajes(keyword)) {
                is ApiResult.Success -> {
                    _mensajes.value = result.data
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _mensajes.value = emptyList()
                }
                else -> {
                    _errorMessage.value = "Error al buscar mensajes"
                }
            }

            _isLoading.value = false
        }
    }

    // ==================== DETALLE DE MENSAJE ====================

    /**
     * Cargar detalle de un mensaje
     */
    fun cargarMensajePorId(mensajeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = contactRepository.obtenerMensajePorId(mensajeId, true)) {
                is ApiResult.Success -> {
                    _mensajeSeleccionado.value = result.data
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _mensajeSeleccionado.value = null
                }
                else -> {
                    _errorMessage.value = "Error al cargar mensaje"
                }
            }

            _isLoading.value = false
        }
    }

    // ==================== ADMINISTRACIÓN ====================

    /**
     * Actualizar estado de mensaje (admin)
     */
    fun actualizarEstado(mensajeId: Long, nuevoEstado: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = contactRepository.actualizarEstado(mensajeId, nuevoEstado)) {
                is ApiResult.Success -> {
                    _successMessage.value = "Estado actualizado a $nuevoEstado"
                    // Actualizar en la lista
                    _mensajes.update { mensajes ->
                        mensajes.map { mensaje ->
                            if (mensaje.id == mensajeId) {
                                result.data
                            } else {
                                mensaje
                            }
                        }
                    }
                    // Actualizar seleccionado si aplica
                    if (_mensajeSeleccionado.value?.id == mensajeId) {
                        _mensajeSeleccionado.value = result.data
                    }
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {
                    _errorMessage.value = "Error al actualizar estado"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Responder mensaje (admin)
     */
    fun responderMensaje(
        mensajeId: Long,
        respuesta: String,
        respondidoPor: Long,
        nuevoEstado: String? = "RESUELTO"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = contactRepository.responderMensaje(
                mensajeId = mensajeId,
                respuesta = respuesta,
                respondidoPor = respondidoPor,
                nuevoEstado = nuevoEstado
            )) {
                is ApiResult.Success -> {
                    _successMessage.value = "Respuesta enviada exitosamente"
                    // Actualizar en la lista
                    _mensajes.update { mensajes ->
                        mensajes.map { mensaje ->
                            if (mensaje.id == mensajeId) {
                                result.data
                            } else {
                                mensaje
                            }
                        }
                    }
                    // Actualizar seleccionado
                    _mensajeSeleccionado.value = result.data
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {
                    _errorMessage.value = "Error al enviar respuesta"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Eliminar mensaje (admin)
     */
    fun eliminarMensaje(mensajeId: Long, adminId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = contactRepository.eliminarMensaje(mensajeId, adminId)) {
                is ApiResult.Success -> {
                    _successMessage.value = "Mensaje eliminado exitosamente"
                    // Remover de la lista
                    _mensajes.update { mensajes ->
                        mensajes.filter { it.id != mensajeId }
                    }
                    // Limpiar seleccionado si aplica
                    if (_mensajeSeleccionado.value?.id == mensajeId) {
                        _mensajeSeleccionado.value = null
                    }
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {
                    _errorMessage.value = "Error al eliminar mensaje"
                }
            }

            _isLoading.value = false
        }
    }

    // ==================== ESTADÍSTICAS ====================

    /**
     * Cargar estadísticas de mensajes
     */
    fun cargarEstadisticas() {
        viewModelScope.launch {
            when (val result = contactRepository.obtenerEstadisticas()) {
                is ApiResult.Success -> {
                    _estadisticas.value = result.data
                }
                is ApiResult.Error -> {
                    _estadisticas.value = emptyMap()
                }
                else -> {
                    _estadisticas.value = emptyMap()
                }
            }
        }
    }

    // ==================== VALIDACIONES ====================

    /**
     * Validar email
     */
    fun validarEmail(email: String): Pair<Boolean, String?> {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return when {
            email.isBlank() -> false to "El email es obligatorio"
            !email.matches(emailRegex) -> false to "El email no es válido"
            else -> true to null
        }
    }

    /**
     * Validar longitud de mensaje
     */
    fun validarMensaje(mensaje: String): Pair<Boolean, String?> {
        return when {
            mensaje.isBlank() -> false to "El mensaje es obligatorio"
            mensaje.length < 10 -> false to "El mensaje debe tener al menos 10 caracteres"
            mensaje.length > 5000 -> false to "El mensaje no puede exceder 5000 caracteres"
            else -> true to null
        }
    }

    /**
     * Validar longitud de asunto
     */
    fun validarAsunto(asunto: String): Pair<Boolean, String?> {
        return when {
            asunto.isBlank() -> false to "El asunto es obligatorio"
            asunto.length > 200 -> false to "El asunto no puede exceder 200 caracteres"
            else -> true to null
        }
    }

    // ==================== HELPERS ====================

    /**
     * Limpiar mensajes de error/éxito
     */
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    /**
     * Obtener cantidad de mensajes pendientes
     */
    fun getMensajesPendientes(): Int {
        return _mensajes.value.count { it.estado == "PENDIENTE" }
    }

    /**
     * Obtener cantidad de mensajes sin responder
     */
    fun getMensajesSinResponder(): Int {
        return _mensajes.value.count { it.respuesta == null }
    }

    /**
     * Limpiar selección de mensaje
     */
    fun limpiarMensajeSeleccionado() {
        _mensajeSeleccionado.value = null
    }
}