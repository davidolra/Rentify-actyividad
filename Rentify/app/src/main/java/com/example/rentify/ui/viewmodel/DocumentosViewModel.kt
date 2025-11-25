//package com.example.rentify.ui.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.rentify.data.local.dao.CatalogDao
//import com.example.rentify.data.local.dao.DocumentoDao
//import com.example.rentify.data.local.entities.DocumentoEntity
//import com.example.rentify.data.remote.ApiResult
//import com.example.rentify.data.remote.dto.DocumentoRemoteDTO
//import com.example.rentify.data.remote.dto.TipoDocumentoRemoteDTO
//import com.example.rentify.data.repository.DocumentRemoteRepository
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//
///**
// * Data class para documento con información enriquecida
// */
//data class DocumentoConInfo(
//    val documento: DocumentoEntity,
//    val nombreEstado: String?,
//    val nombreTipoDoc: String?
//)
//
///**
// * ViewModel para gestión de documentos de usuarios
// */
//class DocumentosViewModel(
//    private val documentoDao: DocumentoDao,
//    private val catalogDao: CatalogDao,
//    private val remoteRepository: DocumentRemoteRepository
//) : ViewModel() {
//
//    private val _documentos = MutableStateFlow<List<DocumentoConInfo>>(emptyList())
//    val documentos: StateFlow<List<DocumentoConInfo>> = _documentos.asStateFlow()
//
//    private val _tiposDocumentos = MutableStateFlow<List<TipoDocumentoRemoteDTO>>(emptyList())
//    val tiposDocumentos: StateFlow<List<TipoDocumentoRemoteDTO>> = _tiposDocumentos.asStateFlow()
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading
//
//    private val _errorMsg = MutableStateFlow<String?>(null)
//    val errorMsg: StateFlow<String?> = _errorMsg
//
//    private val _documentoCreado = MutableStateFlow(false)
//    val documentoCreado: StateFlow<Boolean> = _documentoCreado
//
//    private val _hasApprovedDocuments = MutableStateFlow(false)
//    val hasApprovedDocuments: StateFlow<Boolean> = _hasApprovedDocuments
//
//    /**
//     * Carga los documentos de un usuario desde el servidor
//     */
//    fun cargarDocumentosUsuario(usuarioId: Long) {
//        viewModelScope.launch {
//            _isLoading.value = true
//            _errorMsg.value = null
//
//            try {
//                when (val result = remoteRepository.obtenerDocumentosPorUsuario(usuarioId, true)) {
//                    is ApiResult.Success -> {
//                        // Mapear a entities locales con datos enriquecidos
//                        val documentosConInfo = result.data.map { dto ->
//                            DocumentoConInfo(
//                                documento = DocumentoEntity(
//                                    id = dto.id ?: 0L,
//                                    f_subido = dto.fechaSubido?.time ?: System.currentTimeMillis(),
//                                    nombre = dto.nombre,
//                                    usuarios_id = dto.usuarioId,
//                                    estado_id = dto.estadoId,
//                                    tipo_doc_id = dto.tipoDocId
//                                ),
//                                nombreEstado = dto.estadoNombre,
//                                nombreTipoDoc = dto.tipoDocNombre
//                            )
//                        }
//
//                        _documentos.value = documentosConInfo
//                    }
//                    is ApiResult.Error -> {
//                        _errorMsg.value = result.message
//                    }
//                    is ApiResult.Loading -> { /* No hacer nada */ }
//                }
//            } catch (e: Exception) {
//                _errorMsg.value = "Error al cargar documentos: ${e.message}"
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    /**
//     * Carga los tipos de documentos disponibles
//     */
//    fun cargarTiposDocumentos() {
//        viewModelScope.launch {
//            when (val result = remoteRepository.listarTiposDocumentos()) {
//                is ApiResult.Success -> {
//                    _tiposDocumentos.value = result.data
//                }
//                is ApiResult.Error -> {
//                    _errorMsg.value = result.message
//                }
//                is ApiResult.Loading -> { /* No hacer nada */ }
//            }
//        }
//    }
//
//    /**
//     * Crea/sube un nuevo documento
//     */
//    fun crearDocumento(
//        nombre: String,
//        usuarioId: Long,
//        tipoDocId: Long
//    ) {
//        viewModelScope.launch {
//            _isLoading.value = true
//            _errorMsg.value = null
//
//            try {
//                // Estado por defecto: PENDIENTE (ID = 3)
//                val estadoPendiente = catalogDao.getEstadoByNombre("Pendiente")?.id ?: 3L
//
//                when (val result = remoteRepository.crearDocumento(
//                    nombre = nombre,
//                    usuarioId = usuarioId,
//                    estadoId = estadoPendiente,
//                    tipoDocId = tipoDocId
//                )) {
//                    is ApiResult.Success -> {
//                        _documentoCreado.value = true
//                        // Recargar documentos para actualizar UI
//                        cargarDocumentosUsuario(usuarioId)
//                    }
//                    is ApiResult.Error -> {
//                        _errorMsg.value = result.message
//                    }
//                    is ApiResult.Loading -> { /* No hacer nada */ }
//                }
//            } catch (e: Exception) {
//                _errorMsg.value = "Error de conexión: ${e.message}"
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    /**
//     * Actualiza el estado de un documento
//     */
//    fun actualizarEstadoDocumento(
//        documentoId: Long,
//        nuevoEstadoId: Long,
//        usuarioId: Long
//    ) {
//        viewModelScope.launch {
//            _isLoading.value = true
//
//            when (val result = remoteRepository.actualizarEstadoDocumento(documentoId, nuevoEstadoId)) {
//                is ApiResult.Success -> {
//                    // Recargar documentos
//                    cargarDocumentosUsuario(usuarioId)
//                }
//                is ApiResult.Error -> {
//                    _errorMsg.value = result.message
//                }
//                is ApiResult.Loading -> { /* No hacer nada */ }
//            }
//
//            _isLoading.value = false
//        }
//    }
//
//    /**
//     * Elimina un documento
//     */
//    fun eliminarDocumento(
//        documentoId: Long,
//        usuarioId: Long
//    ) {
//        viewModelScope.launch {
//            _isLoading.value = true
//
//            when (val result = remoteRepository.eliminarDocumento(documentoId)) {
//                is ApiResult.Success -> {
//                    // Recargar documentos
//                    cargarDocumentosUsuario(usuarioId)
//                }
//                is ApiResult.Error -> {
//                    _errorMsg.value = result.message
//                }
//                is ApiResult.Loading -> { /* No hacer nada */ }
//            }
//
//            _isLoading.value = false
//        }
//    }
//
//    /**
//     * Verifica si el usuario tiene documentos aprobados
//     */
//    fun verificarDocumentosAprobados(usuarioId: Long) {
//        viewModelScope.launch {
//            when (val result = remoteRepository.verificarDocumentosAprobados(usuarioId)) {
//                is ApiResult.Success -> {
//                    _hasApprovedDocuments.value = result.data
//                }
//                is ApiResult.Error -> {
//                    _hasApprovedDocuments.value = false
//                }
//                is ApiResult.Loading -> { /* No hacer nada */ }
//            }
//        }
//    }
//
//    /**
//     * Limpia el flag de documento creado
//     */
//    fun clearDocumentoCreado() {
//        _documentoCreado.value = false
//    }
//
//    /**
//     * Limpia mensaje de error
//     */
//    fun clearError() {
//        _errorMsg.value = null
//    }
//}