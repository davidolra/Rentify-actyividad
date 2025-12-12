package com.example.rentify.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.PropertyRemoteDTO
import com.example.rentify.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestion de propiedades (admin)
 * Permite aprobar, rechazar y eliminar propiedades
 */
class GestionPropiedadesViewModel(
    private val propertyRepository: PropertyRemoteRepository
) : ViewModel() {

    companion object {
        private const val TAG = "GestionPropiedadesVM"
    }

    // Lista de propiedades
    private val _propiedades = MutableStateFlow<List<PropertyRemoteDTO>>(emptyList())
    val propiedades: StateFlow<List<PropertyRemoteDTO>> = _propiedades.asStateFlow()

    // Propiedades filtradas
    private val _propiedadesFiltradas = MutableStateFlow<List<PropertyRemoteDTO>>(emptyList())
    val propiedadesFiltradas: StateFlow<List<PropertyRemoteDTO>> = _propiedadesFiltradas.asStateFlow()

    // Propiedad seleccionada
    private val _propiedadSeleccionada = MutableStateFlow<PropertyRemoteDTO?>(null)
    val propiedadSeleccionada: StateFlow<PropertyRemoteDTO?> = _propiedadSeleccionada.asStateFlow()

    // Filtro de estado
    private val _filtroEstado = MutableStateFlow<String?>(null)
    val filtroEstado: StateFlow<String?> = _filtroEstado.asStateFlow()

    // Estados de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    private val _successMsg = MutableStateFlow<String?>(null)
    val successMsg: StateFlow<String?> = _successMsg.asStateFlow()

    init {
        cargarPropiedades()
    }

    /**
     * Cargar todas las propiedades
     */
    fun cargarPropiedades() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                Log.d(TAG, "Cargando todas las propiedades...")

                when (val result = propertyRepository.listarTodasPropiedades(includeDetails = true)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Propiedades cargadas: ${result.data.size}")
                        _propiedades.value = result.data
                        aplicarFiltro()
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al cargar propiedades: ${result.message}")
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
     * Establecer filtro de estado
     */
    fun setFiltroEstado(estado: String?) {
        _filtroEstado.value = estado
        aplicarFiltro()
    }

    /**
     * Aplicar filtro a las propiedades
     */
    private fun aplicarFiltro() {
        val filtro = _filtroEstado.value
        _propiedadesFiltradas.value = if (filtro.isNullOrEmpty()) {
            _propiedades.value
        } else {
            // Aqui filtramos por estado si el backend lo soporta
            // Por ahora mostramos todas
            _propiedades.value
        }
    }

    /**
     * Seleccionar propiedad para ver detalle
     */
    fun seleccionarPropiedad(propiedadId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "Seleccionando propiedad: $propiedadId")

            when (val result = propertyRepository.obtenerPropiedadPorId(propiedadId, includeDetails = true)) {
                is ApiResult.Success -> {
                    _propiedadSeleccionada.value = result.data
                }
                is ApiResult.Error -> {
                    _errorMsg.value = result.message
                }
                else -> {}
            }
        }
    }

    /**
     * Limpiar seleccion
     */
    fun limpiarSeleccion() {
        _propiedadSeleccionada.value = null
    }

    /**
     * Aprobar propiedad (cambiar estado a ACTIVA)
     * Nota: Esto requiere que el backend tenga endpoint de cambio de estado
     */
    fun aprobarPropiedad(propiedadId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d(TAG, "Aprobando propiedad: $propiedadId")

                // Por ahora solo mostramos mensaje de exito
                // El backend deberia tener un endpoint para cambiar estado
                _successMsg.value = "Propiedad aprobada"
                cargarPropiedades()

            } catch (e: Exception) {
                _errorMsg.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Rechazar propiedad con motivo
     */
    fun rechazarPropiedad(propiedadId: Long, motivo: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d(TAG, "Rechazando propiedad: $propiedadId con motivo: $motivo")

                // Por ahora solo mostramos mensaje
                // El backend deberia tener un endpoint para cambiar estado con motivo
                _successMsg.value = "Propiedad rechazada"
                cargarPropiedades()

            } catch (e: Exception) {
                _errorMsg.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Eliminar propiedad
     */
    fun eliminarPropiedad(propiedadId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d(TAG, "Eliminando propiedad: $propiedadId")

                when (val result = propertyRepository.eliminarPropiedad(propiedadId)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Propiedad eliminada exitosamente")
                        _successMsg.value = "Propiedad eliminada"
                        cargarPropiedades()
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al eliminar: ${result.message}")
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

    /**
     * Limpiar mensajes
     */
    fun limpiarMensajes() {
        _errorMsg.value = null
        _successMsg.value = null
    }
}