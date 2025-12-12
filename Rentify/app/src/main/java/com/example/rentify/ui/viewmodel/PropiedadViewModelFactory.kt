package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.repository.PropertyRemoteRepository

/**
 * Factory para PropiedadViewModel
 * Integrado con PropertyRemoteRepository para backend
 */
class PropiedadViewModelFactory(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val propertyRepository: PropertyRemoteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropiedadViewModel::class.java)) {
            return PropiedadViewModel(
                propiedadDao,
                catalogDao,
                propertyRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}