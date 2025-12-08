package com.example.rentify.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentify.data.local.dao.*
import com.example.rentify.data.repository.*

class PropiedadViewModelFactory(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val remoteRepository: PropertyRemoteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropiedadViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PropiedadViewModel(propiedadDao, catalogDao, remoteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class PropiedadDetalleViewModelFactory(
    private val propiedadId: Long,
    private val propertyRepository: PropertyRemoteRepository,
    private val applicationRepository: ApplicationRemoteRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropiedadDetalleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PropiedadDetalleViewModel(propiedadId, propertyRepository, applicationRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SolicitudesViewModelFactory(
    private val solicitudDao: SolicitudDao,
    private val applicationRepository: ApplicationRemoteRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SolicitudesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SolicitudesViewModel(solicitudDao, applicationRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}