package com.example.rentify.data.repository

import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.api.DocumentServiceApi
import com.example.rentify.data.remote.dto.DocumentoRemoteDTO
import com.example.rentify.data.remote.dto.EstadoDocumentoDTO
import com.example.rentify.data.remote.dto.TipoDocumentoRemoteDTO
import com.example.rentify.data.remote.safeApiCall

/**
 * Repositorio remoto para gestión de documentos con DocumentService.
 * Implementa el patrón ApiResult para manejo estructurado de errores.
 */
class DocumentRemoteRepository(
    private val documentServiceApi: DocumentServiceApi
) {

    // ==================== DOCUMENTOS ====================

    /**
     * Crea/sube un nuevo documento
     */
    suspend fun crearDocumento(
        nombre: String,
        usuarioId: Long,
        estadoId: Long,
        tipoDocId: Long
    ): ApiResult<DocumentoRemoteDTO> {
        val documento = DocumentoRemoteDTO(
            nombre = nombre,
            usuarioId = usuarioId,
            estadoId = estadoId,
            tipoDocId = tipoDocId
        )

        return safeApiCall {
            documentServiceApi.crearDocumento(documento)
        }
    }

    /**
     * Lista todos los documentos
     */
    suspend fun listarTodosDocumentos(
        includeDetails: Boolean = false
    ): ApiResult<List<DocumentoRemoteDTO>> {
        return safeApiCall {
            documentServiceApi.listarTodosDocumentos(includeDetails)
        }
    }

    /**
     * Obtiene un documento por ID
     */
    suspend fun obtenerDocumentoPorId(
        id: Long,
        includeDetails: Boolean = true
    ): ApiResult<DocumentoRemoteDTO> {
        return safeApiCall {
            documentServiceApi.obtenerDocumentoPorId(id, includeDetails)
        }
    }

    /**
     * Obtiene todos los documentos de un usuario
     */
    suspend fun obtenerDocumentosPorUsuario(
        usuarioId: Long,
        includeDetails: Boolean = true
    ): ApiResult<List<DocumentoRemoteDTO>> {
        return safeApiCall {
            documentServiceApi.obtenerDocumentosPorUsuario(usuarioId, includeDetails)
        }
    }

    /**
     * Verifica si un usuario tiene documentos aprobados
     */
    suspend fun verificarDocumentosAprobados(
        usuarioId: Long
    ): ApiResult<Boolean> {
        return safeApiCall {
            documentServiceApi.verificarDocumentosAprobados(usuarioId)
        }
    }

    /**
     * Actualiza el estado de un documento
     */
    suspend fun actualizarEstadoDocumento(
        documentoId: Long,
        estadoId: Long
    ): ApiResult<DocumentoRemoteDTO> {
        return safeApiCall {
            documentServiceApi.actualizarEstadoDocumento(documentoId, estadoId)
        }
    }

    /**
     * Elimina un documento
     */
    suspend fun eliminarDocumento(id: Long): ApiResult<Unit> {
        return safeApiCall {
            documentServiceApi.eliminarDocumento(id)
            // Retrofit Response<Void> se convierte a Unit
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(Unit)
                is ApiResult.Error -> ApiResult.Error(result.message, result.code, result.errorResponse)
                is ApiResult.Loading -> ApiResult.Loading
            }
        }
    }

    // ==================== ESTADOS ====================

    /**
     * Lista todos los estados de documentos
     */
    suspend fun listarEstados(): ApiResult<List<EstadoDocumentoDTO>> {
        return safeApiCall {
            documentServiceApi.listarEstados()
        }
    }

    /**
     * Obtiene un estado por ID
     */
    suspend fun obtenerEstadoPorId(id: Long): ApiResult<EstadoDocumentoDTO> {
        return safeApiCall {
            documentServiceApi.obtenerEstadoPorId(id)
        }
    }

    /**
     * Crea un nuevo estado
     */
    suspend fun crearEstado(nombre: String): ApiResult<EstadoDocumentoDTO> {
        val estado = EstadoDocumentoDTO(nombre = nombre)
        return safeApiCall {
            documentServiceApi.crearEstado(estado)
        }
    }

    // ==================== TIPOS DE DOCUMENTOS ====================

    /**
     * Lista todos los tipos de documentos
     */
    suspend fun listarTiposDocumentos(): ApiResult<List<TipoDocumentoRemoteDTO>> {
        return safeApiCall {
            documentServiceApi.listarTiposDocumentos()
        }
    }

    /**
     * Obtiene un tipo de documento por ID
     */
    suspend fun obtenerTipoDocumentoPorId(id: Long): ApiResult<TipoDocumentoRemoteDTO> {
        return safeApiCall {
            documentServiceApi.obtenerTipoDocumentoPorId(id)
        }
    }

    /**
     * Crea un nuevo tipo de documento
     */
    suspend fun crearTipoDocumento(nombre: String): ApiResult<TipoDocumentoRemoteDTO> {
        val tipoDoc = TipoDocumentoRemoteDTO(nombre = nombre)
        return safeApiCall {
            documentServiceApi.crearTipoDocumento(tipoDoc)
        }
    }
}