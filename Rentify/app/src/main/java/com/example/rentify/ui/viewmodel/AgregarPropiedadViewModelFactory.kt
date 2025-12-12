package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentify.data.repository.PropertyRemoteRepository

/**
 * Factory para AgregarPropiedadViewModel
 */
class AgregarPropiedadViewModelFactory(
    private val propertyRepository: PropertyRemoteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgregarPropiedadViewModel::class.java)) {
            return AgregarPropiedadViewModel(propertyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}