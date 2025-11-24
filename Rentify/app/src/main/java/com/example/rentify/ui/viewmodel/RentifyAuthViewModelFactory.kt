package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentify.data.repository.UserRemoteRepository

/**
 * Factory para crear RentifyAuthViewModel con UserRemoteRepository
 */
class RentifyAuthViewModelFactory(
    private val remoteRepository: UserRemoteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RentifyAuthViewModel::class.java)) {
            return RentifyAuthViewModel(remoteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}