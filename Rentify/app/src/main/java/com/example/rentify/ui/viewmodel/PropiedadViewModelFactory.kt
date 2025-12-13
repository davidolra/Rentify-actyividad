package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.repository.PropertyRemoteRepository

/**
 * Factory para PropiedadViewModel
 */
class PropiedadViewModelFactory(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val remoteRepository: PropertyRemoteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropiedadViewModel::class.java)) {
            return PropiedadViewModel(
                propiedadDao = propiedadDao,
                catalogDao = catalogDao,
                remoteRepository = remoteRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}