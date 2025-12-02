package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.remote.dto.UsuarioRemoteDTO
import com.example.rentify.data.repository.UserRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GestionUsuariosViewModel(private val userRemoteRepository: UserRemoteRepository) : ViewModel() {

    private val _users = MutableStateFlow<List<UsuarioRemoteDTO>>(emptyList())
    val users: StateFlow<List<UsuarioRemoteDTO>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = userRemoteRepository.obtenerTodosUsuarios(includeDetails = true)
            if (result is com.example.rentify.data.remote.ApiResult.Success) {
                _users.value = result.data
            }
            _isLoading.value = false
        }
    }

    fun updateUser(userId: Long, updatedUser: UsuarioRemoteDTO) {
        viewModelScope.launch {
            val result = userRemoteRepository.actualizarUsuario(userId, updatedUser)
            if (result is com.example.rentify.data.remote.ApiResult.Success) {
                loadUsers()
            }
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            userRemoteRepository.eliminarUsuario(userId)
            loadUsers()
        }
    }
}