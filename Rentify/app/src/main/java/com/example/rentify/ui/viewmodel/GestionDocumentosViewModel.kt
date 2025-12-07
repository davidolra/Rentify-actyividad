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

/**
 * Filtros disponibles para documentos.
 */
enum class FiltroEstadoDocumento(val displayName: String, val estadoId: Long?) {
    TODOS("Todos", null),
    PENDIENTE("Pendientes", 1L),
    EN_REVISION("En Revisión", 4L),
    ACEPTADO("Aprobados", 2L),
    RECHAZADO("Rechazados", 3L)
}

/**
 * Estado de la UI para gestión de documentos.
 */
data class GestionDocumentosUiState(
    val documentos: List<DocumentoRemoteDTO> = emptyList(),
    val filtroEstado: FiltroEstadoDocumento = FiltroEstadoDocumento.PENDIENTE,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
) {
    /**
     * Documentos filtrados según el estado seleccionado.
     */
    val documentosFiltrados: List<DocumentoRemoteDTO>
        get() = when (filtroEstado) {
            FiltroEstadoDocumento.TODOS -> documentos
            else -> documentos.filter { it.estadoId == filtroEstado.estadoId }
        }

    /**
     * Contadores por estado para los badges.
     */
    val contadores: Map<FiltroEstadoDocumento, Int>
        get() = mapOf(
            FiltroEstadoDocumento.TODOS to documentos.size,
            FiltroEstadoDocumento.PENDIENTE to documentos.count { it.estadoId == 1L },
            FiltroEstadoDocumento.EN_REVISION to documentos.count { it.estadoId == 4L },
            FiltroEstadoDocumento.ACEPTADO to documentos.count { it.estadoId == 2L },
            FiltroEstadoDocumento.RECHAZADO to documentos.count { it.estadoId == 3L }
        )
}

/**
 * Registro de acciones realizadas sobre documentos.
 * Se puede usar para auditoría o historial.
 */
data class AccionDocumentoLog(
    val documentoId: Long,
    val accion: String,
    val motivo: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val adminId: Long? = null
)

/**
 * ViewModel para la gestión de documentos por parte del administrador.
 */
class GestionDocumentosViewModel(
    private val documentRepository: DocumentRemoteRepository = DocumentRemoteRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "GestionDocumentosVM"
    }

    private val _uiState = MutableStateFlow(GestionDocumentosUiState())
    val uiState: StateFlow<GestionDocumentosUiState> = _uiState

    // Historial de acciones (para auditoría local)
    private val _historialAcciones = mutableListOf<AccionDocumentoLog>()
    val historialAcciones: List<AccionDocumentoLog> get() = _historialAcciones.toList()

    /**
     * Carga todos los documentos del sistema.
     */
    fun cargarDocumentos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = documentRepository.listarTodosDocumentos(includeDetails = true)) {
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
                is ApiResult.Loading -> { /* No usado */ }
            }
        }
    }

    /**
     * Cambia el filtro de estado actual.
     */
    fun cambiarFiltro(filtro: FiltroEstadoDocumento) {
        _uiState.update { it.copy(filtroEstado = filtro) }
    }

    /**
     * Aprueba un documento (cambia estado a ACEPTADO).
     */
    fun aprobarDocumento(documentoId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            when (val result = documentRepository.actualizarEstadoDocumento(
                documentoId = documentoId,
                nuevoEstadoId = DocumentRemoteRepository.ESTADO_ACEPTADO
            )) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Documento $documentoId aprobado")

                    // Registrar acción
                    registrarAccion(documentoId, "APROBAR", null)

                    // Actualizar lista local
                    actualizarDocumentoEnLista(result.data)

                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            mensaje = "Documento aprobado correctamente"
                        )
                    }
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Error al aprobar documento: ${result.message}")
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            error = "Error al aprobar: ${result.message}"
                        )
                    }
                }
                is ApiResult.Loading -> { /* No usado */ }
            }
        }
    }

    /**
     * Rechaza un documento con un motivo.
     */
    fun rechazarDocumento(documentoId: Long, motivo: String) {
        if (motivo.isBlank()) {
            _uiState.update { it.copy(error = "El motivo es obligatorio para rechazar") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            when (val result = documentRepository.actualizarEstadoDocumento(
                documentoId = documentoId,
                nuevoEstadoId = DocumentRemoteRepository.ESTADO_RECHAZADO
            )) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Documento $documentoId rechazado. Motivo: $motivo")

                    // Registrar acción con motivo
                    registrarAccion(documentoId, "RECHAZAR", motivo)

                    // Actualizar lista local
                    actualizarDocumentoEnLista(result.data)

                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            mensaje = "Documento rechazado. Motivo registrado."
                        )
                    }

                    // TODO: Enviar notificación al usuario con el motivo
                    // notificarUsuario(result.data.usuarioId, "rechazo", motivo)
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Error al rechazar documento: ${result.message}")
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            error = "Error al rechazar: ${result.message}"
                        )
                    }
                }
                is ApiResult.Loading -> { /* No usado */ }
            }
        }
    }

    /**
     * Marca un documento como "En Revisión".
     */
    fun marcarEnRevision(documentoId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            when (val result = documentRepository.actualizarEstadoDocumento(
                documentoId = documentoId,
                nuevoEstadoId = DocumentRemoteRepository.ESTADO_EN_REVISION
            )) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Documento $documentoId marcado en revisión")

                    // Registrar acción
                    registrarAccion(documentoId, "EN_REVISION", null)

                    // Actualizar lista local
                    actualizarDocumentoEnLista(result.data)

                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            mensaje = "Documento marcado en revisión"
                        )
                    }
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Error al marcar en revisión: ${result.message}")
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            error = "Error: ${result.message}"
                        )
                    }
                }
                is ApiResult.Loading -> { /* No usado */ }
            }
        }
    }

    /**
     * Elimina un documento con un motivo.
     */
    fun eliminarDocumento(documentoId: Long, motivo: String) {
        if (motivo.isBlank()) {
            _uiState.update { it.copy(error = "El motivo es obligatorio para eliminar") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            // Registrar acción ANTES de eliminar (para tener el registro)
            registrarAccion(documentoId, "ELIMINAR", motivo)

            when (val result = documentRepository.eliminarDocumento(documentoId)) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Documento $documentoId eliminado. Motivo: $motivo")

                    // Remover de la lista local
                    _uiState.update { state ->
                        state.copy(
                            documentos = state.documentos.filter { it.id != documentoId },
                            isProcessing = false,
                            mensaje = "Documento eliminado correctamente"
                        )
                    }

                    // TODO: Enviar notificación al usuario con el motivo
                    // notificarUsuario(usuarioId, "eliminacion", motivo)
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Error al eliminar documento: ${result.message}")
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            error = "Error al eliminar: ${result.message}"
                        )
                    }
                }
                is ApiResult.Loading -> { /* No usado */ }
            }
        }
    }

    /**
     * Limpia el mensaje de feedback.
     */
    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    /**
     * Limpia el error.
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Actualiza un documento en la lista local.
     */
    private fun actualizarDocumentoEnLista(documentoActualizado: DocumentoRemoteDTO) {
        _uiState.update { state ->
            val nuevaLista = state.documentos.map { doc ->
                if (doc.id == documentoActualizado.id) documentoActualizado else doc
            }
            state.copy(documentos = nuevaLista)
        }
    }

    /**
     * Registra una acción para auditoría.
     */
    private fun registrarAccion(documentoId: Long, accion: String, motivo: String?) {
        val log = AccionDocumentoLog(
            documentoId = documentoId,
            accion = accion,
            motivo = motivo
        )
        _historialAcciones.add(log)
        Log.d(TAG, "Acción registrada: $log")

        // TODO: Persistir en BD local o enviar a servidor para auditoría
        // auditRepository.guardarAccion(log)
    }

    /**
     * Obtiene estadísticas de documentos.
     */
    fun obtenerEstadisticas(): DocumentosEstadisticas {
        val docs = _uiState.value.documentos
        return DocumentosEstadisticas(
            total = docs.size,
            pendientes = docs.count { it.estadoId == 1L },
            enRevision = docs.count { it.estadoId == 4L },
            aprobados = docs.count { it.estadoId == 2L },
            rechazados = docs.count { it.estadoId == 3L }
        )
    }
}

/**
 * Estadísticas de documentos.
 */
data class DocumentosEstadisticas(
    val total: Int,
    val pendientes: Int,
    val enRevision: Int,
    val aprobados: Int,
    val rechazados: Int
) {
    val porcentajeAprobados: Float
        get() = if (total > 0) (aprobados.toFloat() / total) * 100 else 0f

    val porcentajeRechazados: Float
        get() = if (total > 0) (rechazados.toFloat() / total) * 100 else 0f
}