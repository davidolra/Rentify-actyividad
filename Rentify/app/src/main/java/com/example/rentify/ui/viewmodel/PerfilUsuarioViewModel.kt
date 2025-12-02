package com.example.rentify.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.UsuarioDTO
import com.example.rentify.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el perfil del usuario
 * Usa UserRepository para obtener datos del backend
 */
class PerfilUsuarioViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    companion object {
        private const val TAG = "PerfilUsuarioViewModel"
    }

    private val _usuario = MutableStateFlow<UsuarioDTO?>(null)
    val usuario: StateFlow<UsuarioDTO?> = _usuario.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    /**
     * Carga los datos del usuario desde el backend
     */
    fun cargarDatosUsuario(usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            Log.d(TAG, "Cargando datos del usuario ID: $usuarioId")

            when (val result = userRepository.getUserById(usuarioId)) {
                is ApiResult.Success -> {
                    _usuario.value = result.data
                    Log.d(TAG, "Usuario cargado: ${result.data.email}")
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                    Log.e(TAG, "Error al cargar usuario: ${result.message}")
                }
                is ApiResult.Loading -> {
                    // No hacer nada
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Actualiza los datos del perfil del usuario en el backend
     */
    fun actualizarPerfil(
        pnombre: String,
        snombre: String,
        papellido: String,
        telefono: String
    ) {
        viewModelScope.launch {
            val currentUser = _usuario.value ?: return@launch

            _isLoading.value = true
            _error.value = null
            _updateSuccess.value = false

            Log.d(TAG, "Actualizando perfil del usuario ID: ${currentUser.id}")

            val updatedUser = currentUser.copy(
                pnombre = pnombre,
                snombre = snombre,
                papellido = papellido,
                ntelefono = telefono
            )

            when (val result = userRepository.updateUser(currentUser.id!!, updatedUser)) {
                is ApiResult.Success -> {
                    _usuario.value = result.data
                    _updateSuccess.value = true
                    Log.d(TAG, "Perfil actualizado exitosamente")
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                    Log.e(TAG, "Error al actualizar perfil: ${result.message}")
                }
                is ApiResult.Loading -> {
                    // No hacer nada
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Limpia el estado de exito de actualizacion
     */
    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }

    /**
     * Limpia el estado de error
     */
    fun clearError() {
        _error.value = null
    }
}