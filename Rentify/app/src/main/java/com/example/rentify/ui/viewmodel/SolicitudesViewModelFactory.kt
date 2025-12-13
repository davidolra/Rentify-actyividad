package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.repository.ApplicationRemoteRepository
import com.example.rentify.data.repository.PropertyRemoteRepository

/**
 * Factory para SolicitudesViewModel
 */
class SolicitudesViewModelFactory(
    private val solicitudDao: SolicitudDao,
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val remoteRepository: ApplicationRemoteRepository,
    private val propertyRepository: PropertyRemoteRepository? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SolicitudesViewModel::class.java)) {
            return SolicitudesViewModel(
                solicitudDao = solicitudDao,
                propiedadDao = propiedadDao,
                catalogDao = catalogDao,
                remoteRepository = remoteRepository,
                propertyRepository = propertyRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}