package com.example.rentify.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.dao.CatalogDao

/**
 * Factory para crear PropiedadDetalleViewModel
 */
class PropiedadDetalleViewModelFactory(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropiedadDetalleViewModel::class.java)) {
            return PropiedadDetalleViewModel(propiedadDao, catalogDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}