package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.UsuarioDTO
import com.example.rentify.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de gestión de usuarios.
 * ✅ CORREGIDO: Maneja ApiResult para mostrar errores detallados.
 */
class UserManagementViewModel(private val repository: UserRepository) : ViewModel() {

    private val _users = MutableStateFlow<List<UsuarioDTO>>(emptyList())
    val users: StateFlow<List<UsuarioDTO>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.getUsers()) {
                is ApiResult.Success -> {
                    _users.value = result.data
                    _error.value = null
                }
                is ApiResult.Error -> {
                    _error.value = "Error al cargar usuarios: ${result.message}"
                }
                is ApiResult.Loading -> {
                    // Opcional: ya se maneja con _isLoading
                }
            }
            _isLoading.value = false
        }
    }

    fun updateUser(userId: Long, updatedUser: UsuarioDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.updateUser(userId, updatedUser)) {
                is ApiResult.Success -> {
                    // Actualizar la lista local con el usuario modificado
                    _users.value = _users.value.map {
                        if (it.id == userId) result.data else it
                    }
                    _error.value = null // Limpiar error si la operación fue exitosa
                }
                is ApiResult.Error -> {
                    _error.value = "Error al actualizar el usuario: ${result.message}"
                }
                is ApiResult.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.deleteUser(userId)) {
                is ApiResult.Success -> {
                    // Eliminar el usuario de la lista local
                    _users.value = _users.value.filter { it.id != userId }
                    _error.value = null
                }
                is ApiResult.Error -> {
                    _error.value = "Error al eliminar el usuario: ${result.message}"
                }
                is ApiResult.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}

class UserManagementViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserManagementViewModel::class.java)) {
            return UserManagementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
