package com.example.rentify.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.DocumentoRemoteDTO
import com.example.rentify.data.repository.DocumentRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MisDocumentosUiState(
    val documentos: List<DocumentoRemoteDTO> = emptyList(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class MisDocumentosViewModel(
    private val documentRepository: DocumentRemoteRepository = DocumentRemoteRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "MisDocumentosVM"
    }

    private val _uiState = MutableStateFlow(MisDocumentosUiState())
    val uiState: StateFlow<MisDocumentosUiState> = _uiState

    fun cargarMisDocumentos(usuarioId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = documentRepository.obtenerDocumentosPorUsuario(usuarioId, includeDetails = true)) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Documentos cargados: ${result.data.size}")
                    _uiState.update {
                        it.copy(
                            documentos = result.data.sortedByDescending { doc -> doc.fechaSubido },
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Error al cargar documentos: ${result.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    /**
     * Resube un documento rechazado:
     * 1. Elimina el documento rechazado
     * 2. Crea un nuevo documento con estado PENDIENTE
     */
    fun resubirDocumentoRechazado(
        documentoRechazadoId: Long,
        usuarioId: Long,
        tipoDocId: Long,
        nombreArchivo: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }

            // Paso 1: Eliminar documento rechazado
            val deleteResult = documentRepository.eliminarDocumento(documentoRechazadoId)

            when (deleteResult) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Documento rechazado eliminado: $documentoRechazadoId")

                    // Paso 2: Crear nuevo documento
                    val nombreDoc = documentRepository.generarNombreDocumento(
                        tipoDocId = tipoDocId,
                        nombreUsuario = "user_$usuarioId",
                        extension = "pdf"
                    )

                    when (val createResult = documentRepository.crearDocumento(
                        nombre = nombreDoc,
                        usuarioId = usuarioId,
                        tipoDocId = tipoDocId,
                        estadoId = DocumentRemoteRepository.ESTADO_PENDIENTE
                    )) {
                        is ApiResult.Success -> {
                            Log.d(TAG, "Nuevo documento creado exitosamente")
                            _uiState.update {
                                it.copy(
                                    isUploading = false,
                                    mensaje = "Documento subido correctamente. Pendiente de revision."
                                )
                            }
                            // Recargar lista
                            cargarMisDocumentos(usuarioId)
                        }
                        is ApiResult.Error -> {
                            Log.e(TAG, "Error al crear nuevo documento: ${createResult.message}")
                            _uiState.update {
                                it.copy(
                                    isUploading = false,
                                    error = "Error al subir documento: ${createResult.message}"
                                )
                            }
                        }
                        is ApiResult.Loading -> {}
                    }
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Error al eliminar documento rechazado: ${deleteResult.message}")
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            error = "Error al procesar: ${deleteResult.message}"
                        )
                    }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    /**
     * Sube un nuevo documento (sin eliminar anterior)
     */
    fun subirNuevoDocumento(
        usuarioId: Long,
        tipoDocId: Long,
        nombreArchivo: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }

            val nombreDoc = documentRepository.generarNombreDocumento(
                tipoDocId = tipoDocId,
                nombreUsuario = "user_$usuarioId",
                extension = "pdf"
            )

            when (val result = documentRepository.crearDocumento(
                nombre = nombreDoc,
                usuarioId = usuarioId,
                tipoDocId = tipoDocId,
                estadoId = DocumentRemoteRepository.ESTADO_PENDIENTE
            )) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Documento subido exitosamente")
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            mensaje = "Documento subido correctamente. Pendiente de revision."
                        )
                    }
                    cargarMisDocumentos(usuarioId)
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Error al subir documento: ${result.message}")
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            error = "Error al subir documento: ${result.message}"
                        )
                    }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
}