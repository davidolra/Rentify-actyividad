package com.example.rentify.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.PropertyRemoteDTO
import com.example.rentify.data.repository.ApplicationRemoteRepository
import com.example.rentify.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PropiedadDetalleViewModel(
    private val propiedadId: Long,
    private val propertyRepository: PropertyRemoteRepository,
    private val applicationRepository: ApplicationRemoteRepository,
    private val context: Context
) : ViewModel() {

    private val userPrefs = UserPreferences(context)

    private val _propiedad = MutableStateFlow<PropertyRemoteDTO?>(null)
    val propiedad: StateFlow<PropertyRemoteDTO?> = _propiedad.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _solicitudCreada = MutableStateFlow(false)
    val solicitudCreada: StateFlow<Boolean> = _solicitudCreada.asStateFlow()

    fun cargarPropiedad() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = propertyRepository.obtenerPropiedadPorId(propiedadId, includeDetails = true)) {
                is ApiResult.Success -> _propiedad.value = result.data
                is ApiResult.Error -> _error.value = result.message
                is ApiResult.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    fun crearSolicitud() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _solicitudCreada.value = false

            userPrefs.userId.firstOrNull()?.let { userId ->
                when (val result = applicationRepository.crearSolicitudRemota(userId, propiedadId)) {
                    is ApiResult.Success -> _solicitudCreada.value = true
                    is ApiResult.Error -> _error.value = result.message
                    is ApiResult.Loading -> {}
                }
            } ?: run {
                _error.value = "Usuario no autenticado"
            }
            _isLoading.value = false
        }
    }
}