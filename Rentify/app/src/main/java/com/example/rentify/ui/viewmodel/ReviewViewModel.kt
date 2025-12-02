package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.ResenaDTO
import com.example.rentify.data.remote.dto.TipoResenaDTO
import com.example.rentify.data.repository.ReviewRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para gestión de reseñas
 * ✅ Integrado con Review Service (Puerto 8086)
 */
class ReviewViewModel(
    private val reviewRepository: ReviewRemoteRepository
) : ViewModel() {

    // Estado de reseñas
    private val _resenas = MutableStateFlow<List<ResenaDTO>>(emptyList())
    val resenas: StateFlow<List<ResenaDTO>> = _resenas

    // Estado de tipos de reseña
    private val _tiposResena = MutableStateFlow<List<TipoResenaDTO>>(emptyList())
    val tiposResena: StateFlow<List<TipoResenaDTO>> = _tiposResena

    // Estado de promedio de calificación
    private val _promedioCalificacion = MutableStateFlow<Double?>(null)
    val promedioCalificacion: StateFlow<Double?> = _promedioCalificacion

    // Estados de UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    init {
        cargarTiposResena()
    }

    // ==================== CREAR RESEÑA ====================

    /**
     * Crear reseña de propiedad
     */
    fun crearResenaPropiedad(
        usuarioId: Long,
        propiedadId: Long,
        puntaje: Int,
        comentario: String?,
        tipoResenaId: Long = 1L  // Tipo por defecto: RESENA_PROPIEDAD
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = reviewRepository.crearResena(
                usuarioId = usuarioId,
                propiedadId = propiedadId,
                usuarioResenadoId = null,
                puntaje = puntaje,
                comentario = comentario,
                tipoResenaId = tipoResenaId
            )) {
                is ApiResult.Success -> {
                    _successMessage.value = "Reseña creada exitosamente"
                    // Recargar reseñas de la propiedad
                    cargarResenasPorPropiedad(propiedadId)
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {
                    _errorMessage.value = "Error al crear reseña"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Crear reseña de usuario
     */
    fun crearResenaUsuario(
        usuarioId: Long,
        usuarioResenadoId: Long,
        puntaje: Int,
        comentario: String?,
        tipoResenaId: Long = 2L  // Tipo: RESENA_USUARIO
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = reviewRepository.crearResena(
                usuarioId = usuarioId,
                propiedadId = null,
                usuarioResenadoId = usuarioResenadoId,
                puntaje = puntaje,
                comentario = comentario,
                tipoResenaId = tipoResenaId
            )) {
                is ApiResult.Success -> {
                    _successMessage.value = "Reseña creada exitosamente"
                    // Recargar reseñas del usuario
                    cargarResenasSobreUsuario(usuarioResenadoId)
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {
                    _errorMessage.value = "Error al crear reseña"
                }
            }

            _isLoading.value = false
        }
    }

    // ==================== CARGAR RESEÑAS ====================

    /**
     * Cargar reseñas de una propiedad
     */
    fun cargarResenasPorPropiedad(propiedadId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = reviewRepository.obtenerResenasPorPropiedad(propiedadId, true)) {
                is ApiResult.Success -> {
                    _resenas.value = result.data
                    // También cargar el promedio
                    cargarPromedioPorPropiedad(propiedadId)
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _resenas.value = emptyList()
                }
                else -> {
                    _errorMessage.value = "Error al cargar reseñas"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Cargar reseñas creadas por un usuario
     */
    fun cargarResenasPorUsuario(usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = reviewRepository.obtenerResenasPorUsuario(usuarioId, true)) {
                is ApiResult.Success -> {
                    _resenas.value = result.data
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _resenas.value = emptyList()
                }
                else -> {
                    _errorMessage.value = "Error al cargar reseñas"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Cargar reseñas sobre un usuario
     */
    fun cargarResenasSobreUsuario(usuarioResenadoId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = reviewRepository.obtenerResenasSobreUsuario(usuarioResenadoId, true)) {
                is ApiResult.Success -> {
                    _resenas.value = result.data
                    // También cargar el promedio
                    cargarPromedioPorUsuario(usuarioResenadoId)
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _resenas.value = emptyList()
                }
                else -> {
                    _errorMessage.value = "Error al cargar reseñas"
                }
            }

            _isLoading.value = false
        }
    }

    // ==================== PROMEDIOS ====================

    /**
     * Cargar promedio de calificación de propiedad
     */
    fun cargarPromedioPorPropiedad(propiedadId: Long) {
        viewModelScope.launch {
            when (val result = reviewRepository.calcularPromedioPorPropiedad(propiedadId)) {
                is ApiResult.Success -> {
                    _promedioCalificacion.value = result.data
                }
                is ApiResult.Error -> {
                    _promedioCalificacion.value = 0.0
                }
                else -> {
                    _promedioCalificacion.value = 0.0
                }
            }
        }
    }

    /**
     * Cargar promedio de calificación de usuario
     */
    fun cargarPromedioPorUsuario(usuarioResenadoId: Long) {
        viewModelScope.launch {
            when (val result = reviewRepository.calcularPromedioPorUsuario(usuarioResenadoId)) {
                is ApiResult.Success -> {
                    _promedioCalificacion.value = result.data
                }
                is ApiResult.Error -> {
                    _promedioCalificacion.value = 0.0
                }
                else -> {
                    _promedioCalificacion.value = 0.0
                }
            }
        }
    }

    // ==================== ADMINISTRACIÓN ====================

    /**
     * Eliminar reseña (solo admin)
     */
    fun eliminarResena(resenaId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = reviewRepository.eliminarResena(resenaId)) {
                is ApiResult.Success -> {
                    _successMessage.value = "Reseña eliminada exitosamente"
                    // Remover de la lista local
                    _resenas.update { resenas ->
                        resenas.filter { it.id != resenaId }
                    }
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {
                    _errorMessage.value = "Error al eliminar reseña"
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Actualizar estado de reseña (ACTIVA, BANEADA, OCULTA)
     */
    fun actualizarEstadoResena(resenaId: Long, nuevoEstado: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = reviewRepository.actualizarEstadoResena(resenaId, nuevoEstado)) {
                is ApiResult.Success -> {
                    _successMessage.value = "Estado actualizado a $nuevoEstado"
                    // Actualizar en la lista local
                    _resenas.update { resenas ->
                        resenas.map { resena ->
                            if (resena.id == resenaId) {
                                result.data
                            } else {
                                resena
                            }
                        }
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

    // ==================== TIPOS DE RESEÑA ====================

    /**
     * Cargar tipos de reseña disponibles
     */
    private fun cargarTiposResena() {
        viewModelScope.launch {
            when (val result = reviewRepository.listarTiposResena()) {
                is ApiResult.Success -> {
                    _tiposResena.value = result.data
                }
                is ApiResult.Error -> {
                    // Si falla, usar valores por defecto
                    _tiposResena.value = listOf(
                        TipoResenaDTO(1L, "RESENA_PROPIEDAD"),
                        TipoResenaDTO(2L, "RESENA_USUARIO")
                    )
                }
                else -> {}
            }
        }
    }

    // ==================== VALIDACIONES ====================

    /**
     * Validar que un puntaje sea válido (1-10)
     */
    fun validarPuntaje(puntaje: Int): Boolean {
        return puntaje in 1..10
    }

    /**
     * Validar que un comentario tenga longitud válida (10-500 caracteres)
     */
    fun validarComentario(comentario: String?): Pair<Boolean, String?> {
        if (comentario.isNullOrBlank()) {
            return true to null  // Comentario opcional
        }

        return when {
            comentario.length < 10 -> false to "El comentario debe tener al menos 10 caracteres"
            comentario.length > 500 -> false to "El comentario no puede exceder 500 caracteres"
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
     * Obtener cantidad total de reseñas
     */
    fun getTotalResenas(): Int {
        return _resenas.value.size
    }

    /**
     * Obtener reseñas activas
     */
    fun getResenasActivas(): List<ResenaDTO> {
        return _resenas.value.filter { it.estado == "ACTIVA" }
    }

    /**
     * Verificar si el usuario puede reseñar
     */
    suspend fun puedeResenar(usuarioId: Long, propiedadId: Long): Boolean {
        return reviewRepository.puedeResenarPropiedad(usuarioId, propiedadId)
    }
}