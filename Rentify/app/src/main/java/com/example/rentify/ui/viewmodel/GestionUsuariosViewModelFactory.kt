package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentify.data.repository.UserRemoteRepository

class GestionUsuariosViewModelFactory(private val userRemoteRepository: UserRemoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionUsuariosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GestionUsuariosViewModel(userRemoteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
