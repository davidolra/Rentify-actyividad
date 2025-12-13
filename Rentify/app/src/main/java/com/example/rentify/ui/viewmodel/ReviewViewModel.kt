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
 * ViewModel para gestion de resenas
 */
class ReviewViewModel(
    private val reviewRepository: ReviewRemoteRepository
) : ViewModel() {

    private val _resenas = MutableStateFlow<List<ResenaDTO>>(emptyList())
    val resenas: StateFlow<List<ResenaDTO>> = _resenas

    private val _tiposResena = MutableStateFlow<List<TipoResenaDTO>>(emptyList())
    val tiposResena: StateFlow<List<TipoResenaDTO>> = _tiposResena

    private val _promedioCalificacion = MutableStateFlow<Double?>(null)
    val promedioCalificacion: StateFlow<Double?> = _promedioCalificacion

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    init {
        cargarTiposResena()
    }

    // ==================== CREAR RESENA ====================

    fun crearResenaPropiedad(
        usuarioId: Long,
        propiedadId: Long,
        puntuacion: Int,
        comentario: String?,
        tipoResenaId: Long = 1L
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = reviewRepository.crearResena(
                usuarioId = usuarioId,
                propiedadId = propiedadId,
                usuarioResenadoId = null,
                puntuacion = puntuacion,
                comentario = comentario,
                tipoResenaId = tipoResenaId
            )) {
                is ApiResult.Success -> {
                    _successMessage.value = "Resena creada exitosamente"
                    cargarResenasPorPropiedad(propiedadId)
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {
                    _errorMessage.value = "Error al crear resena"
                }
            }

            _isLoading.value = false
        }
    }

    fun crearResenaUsuario(
        usuarioId: Long,
        usuarioResenadoId: Long,
        puntuacion: Int,
        comentario: String?,
        tipoResenaId: Long = 2L
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = reviewRepository.crearResena(
                usuarioId = usuarioId,
                propiedadId = null,
                usuarioResenadoId = usuarioResenadoId,
                puntuacion = puntuacion,
                comentario = comentario,
                tipoResenaId = tipoResenaId
            )) {
                is ApiResult.Success -> {
                    _successMessage.value = "Resena creada exitosamente"
                    cargarResenasSobreUsuario(usuarioResenadoId)
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {
                    _errorMessage.value = "Error al crear resena"
                }
            }

            _isLoading.value = false
        }
    }

    // ==================== CARGAR RESENAS ====================

    fun cargarResenasPorPropiedad(propiedadId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = reviewRepository.obtenerResenasPorPropiedad(propiedadId, true)) {
                is ApiResult.Success -> {
                    _resenas.value = result.data
                    cargarPromedioPorPropiedad(propiedadId)
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _resenas.value = emptyList()
                }
                else -> {
                    _errorMessage.value = "Error al cargar resenas"
                }
            }

            _isLoading.value = false
        }
    }

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
                    _errorMessage.value = "Error al cargar resenas"
                }
            }

            _isLoading.value = false
        }
    }

    fun cargarResenasSobreUsuario(usuarioResenadoId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = reviewRepository.obtenerResenasSobreUsuario(usuarioResenadoId, true)) {
                is ApiResult.Success -> {
                    _resenas.value = result.data
                    cargarPromedioPorUsuario(usuarioResenadoId)
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _resenas.value = emptyList()
                }
                else -> {
                    _errorMessage.value = "Error al cargar resenas"
                }
            }

            _isLoading.value = false
        }
    }

    // ==================== PROMEDIOS ====================

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

    // ==================== ADMINISTRACION ====================

    fun eliminarResena(resenaId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = reviewRepository.eliminarResena(resenaId)) {
                is ApiResult.Success -> {
                    _successMessage.value = "Resena eliminada exitosamente"
                    _resenas.update { resenas ->
                        resenas.filter { it.id != resenaId }
                    }
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {
                    _errorMessage.value = "Error al eliminar resena"
                }
            }

            _isLoading.value = false
        }
    }

    fun actualizarEstadoResena(resenaId: Long, nuevoEstado: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = reviewRepository.actualizarEstadoResena(resenaId, nuevoEstado)) {
                is ApiResult.Success -> {
                    _successMessage.value = "Estado actualizado a $nuevoEstado"
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

    // ==================== TIPOS DE RESENA ====================

    private fun cargarTiposResena() {
        viewModelScope.launch {
            when (val result = reviewRepository.listarTiposResena()) {
                is ApiResult.Success -> {
                    _tiposResena.value = result.data
                }
                is ApiResult.Error -> {
                    // Usar valores por defecto
                    _tiposResena.value = listOf(
                        TipoResenaDTO(1, "RESENA_PROPIEDAD"),
                        TipoResenaDTO(2, "RESENA_USUARIO")
                    )
                }
                else -> {}
            }
        }
    }

    // ==================== VALIDACIONES ====================

    fun validarPuntuacion(puntuacion: Int): Boolean {
        return puntuacion in 1..10
    }

    fun validarComentario(comentario: String?): Pair<Boolean, String?> {
        if (comentario.isNullOrBlank()) {
            return true to null
        }

        return when {
            comentario.length < 10 -> false to "El comentario debe tener al menos 10 caracteres"
            comentario.length > 500 -> false to "El comentario no puede exceder 500 caracteres"
            else -> true to null
        }
    }

    // ==================== HELPERS ====================

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun getTotalResenas(): Int {
        return _resenas.value.size
    }

    fun getResenasActivas(): List<ResenaDTO> {
        // Filtrar por estado si existe, sino devolver todas
        return _resenas.value
    }

    suspend fun puedeResenar(usuarioId: Long, propiedadId: Long): Boolean {
        return reviewRepository.puedeResenarPropiedad(usuarioId, propiedadId)
    }
}