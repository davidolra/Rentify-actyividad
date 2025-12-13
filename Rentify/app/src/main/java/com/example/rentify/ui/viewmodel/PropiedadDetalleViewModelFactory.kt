package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.repository.PropertyRemoteRepository
import com.example.rentify.data.repository.ApplicationRemoteRepository

/**
 * Factory para PropiedadDetalleViewModel
 */
class PropiedadDetalleViewModelFactory(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val propertyRepository: PropertyRemoteRepository,
    private val applicationRepository: ApplicationRemoteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropiedadDetalleViewModel::class.java)) {
            return PropiedadDetalleViewModel(
                propiedadDao,
                catalogDao,
                propertyRepository,
                applicationRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}