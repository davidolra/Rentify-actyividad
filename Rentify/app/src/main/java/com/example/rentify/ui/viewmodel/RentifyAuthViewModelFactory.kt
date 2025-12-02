package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentify.data.repository.UserRemoteRepository
import com.example.rentify.data.repository.RentifyUserRepository

/**
 * ✅ Factory actualizado para crear RentifyAuthViewModel con ambos repositorios
 */
class RentifyAuthViewModelFactory(
    private val userRemoteRepository: UserRemoteRepository,
    private val rentifyUserRepository: RentifyUserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RentifyAuthViewModel::class.java)) {
            return RentifyAuthViewModel(userRemoteRepository, rentifyUserRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}