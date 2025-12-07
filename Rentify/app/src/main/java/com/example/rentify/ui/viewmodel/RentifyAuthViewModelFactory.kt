package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentify.data.repository.DocumentRemoteRepository
import com.example.rentify.data.repository.RentifyUserRepository
import com.example.rentify.data.repository.UserRemoteRepository

/**
 * Factory para crear RentifyAuthViewModel con las dependencias necesarias.
 */
class RentifyAuthViewModelFactory(
    private val remoteRepository: UserRemoteRepository,
    private val localRepository: RentifyUserRepository,
    private val documentRepository: DocumentRemoteRepository = DocumentRemoteRepository()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RentifyAuthViewModel::class.java)) {
            return RentifyAuthViewModel(
                remoteRepository = remoteRepository,
                localRepository = localRepository,
                documentRepository = documentRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}