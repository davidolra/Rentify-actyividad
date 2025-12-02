package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.dao.UsuarioDao
import com.example.rentify.data.repository.DocumentRepository
import com.example.rentify.data.repository.UserRemoteRepository

class PerfilUsuarioViewModelFactory(
    private val usuarioDao: UsuarioDao,
    private val catalogDao: CatalogDao,
    private val solicitudDao: SolicitudDao,
    private val documentRepository: DocumentRepository,
    private val userRemoteRepository: UserRemoteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerfilUsuarioViewModel::class.java)) {
            return PerfilUsuarioViewModel(
                usuarioDao,
                catalogDao,
                solicitudDao,
                documentRepository,
                userRemoteRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
