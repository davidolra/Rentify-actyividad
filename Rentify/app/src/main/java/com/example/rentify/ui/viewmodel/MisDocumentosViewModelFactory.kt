package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentify.data.repository.DocumentRemoteRepository

class MisDocumentosViewModelFactory(
    private val documentRepository: DocumentRemoteRepository = DocumentRemoteRepository()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MisDocumentosViewModel::class.java)) {
            return MisDocumentosViewModel(documentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}