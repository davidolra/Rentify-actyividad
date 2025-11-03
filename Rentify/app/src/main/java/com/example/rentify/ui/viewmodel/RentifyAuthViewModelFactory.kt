package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentify.data.repository.RentifyUserRepository

/**
 * Factory para crear RentifyAuthViewModel con su repositorio
 */
class RentifyAuthViewModelFactory(
    private val repository: RentifyUserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RentifyAuthViewModel::class.java)) {
            return RentifyAuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}