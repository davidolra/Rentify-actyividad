package com.example.rentify.data.repository

import android.util.Log
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.ActualizarEstadoRequest
import com.example.rentify.data.remote.dto.DocumentoRemoteDTO
import com.example.rentify.data.remote.dto.EstadoDocumentoDTO
import com.example.rentify.data.remote.dto.TipoDocumentoRemoteDTO
import com.example.rentify.data.remote.safeApiCall

/**
 * Repositorio para comunicacion con Document Service (Puerto 8083).
 */
class DocumentRemoteRepository {

    private val api = RetrofitClient.documentServiceApi

    companion object {
        private const val TAG = "DocumentRemoteRepo"

        // IDs de estados
        const val ESTADO_PENDIENTE = 1L
        const val ESTADO_ACEPTADO = 2L
        const val ESTADO_RECHAZADO = 3L
        const val ESTADO_EN_REVISION = 4L

        // IDs de tipos de documento
        const val TIPO_DNI = 1L
        const val TIPO_PASAPORTE = 2L
        const val TIPO_LIQUIDACION_SUELDO = 3L
        const val TIPO_CERTIFICADO_ANTECEDENTES = 4L
        const val TIPO_CERTIFICADO_AFP = 5L
        const val TIPO_CONTRATO_TRABAJO = 6L
    }

    // ==================== DOCUMENTOS ====================

    /**
     * Crea un nuevo documento
     */
    suspend fun crearDocumento(
        nombre: String,
        usuarioId: Long,
        tipoDocId: Long,
        estadoId: Long = ESTADO_PENDIENTE
    ): ApiResult<DocumentoRemoteDTO> {
        Log.d(TAG, "Creando documento: $nombre para usuario $usuarioId, tipo $tipoDocId")

        val documentoDTO = DocumentoRemoteDTO(
            nombre = nombre,
            usuarioId = usuarioId,
            estadoId = estadoId,
            tipoDocId = tipoDocId
        )

        return safeApiCall {
            api.crearDocumento(documentoDTO)
        }
    }

    /**
     * Sube multiples documentos para un usuario
     */
    suspend fun subirMultiplesDocumentos(
        usuarioId: Long,
        documentos: List<Pair<Long, String>>
    ): List<ApiResult<DocumentoRemoteDTO>> {
        Log.d(TAG, "Subiendo ${documentos.size} documentos para usuario $usuarioId")

        return documentos.map { (tipoDocId, nombreArchivo) ->
            crearDocumento(
                nombre = nombreArchivo,
                usuarioId = usuarioId,
                tipoDocId = tipoDocId,
                estadoId = ESTADO_PENDIENTE
            )
        }
    }

    /**
     * Obtiene todos los documentos
     */
    suspend fun listarTodosDocumentos(includeDetails: Boolean = false): ApiResult<List<DocumentoRemoteDTO>> {
        Log.d(TAG, "Listando todos los documentos")
        return safeApiCall {
            api.listarTodosDocumentos(includeDetails)
        }
    }

    /**
     * Obtiene un documento por ID
     */
    suspend fun obtenerDocumentoPorId(id: Long, includeDetails: Boolean = true): ApiResult<DocumentoRemoteDTO> {
        Log.d(TAG, "Obteniendo documento con ID: $id")
        return safeApiCall {
            api.obtenerDocumentoPorId(id, includeDetails)
        }
    }

    /**
     * Obtiene documentos de un usuario
     */
    suspend fun obtenerDocumentosPorUsuario(
        usuarioId: Long,
        includeDetails: Boolean = true
    ): ApiResult<List<DocumentoRemoteDTO>> {
        Log.d(TAG, "Obteniendo documentos del usuario: $usuarioId")
        return safeApiCall {
            api.obtenerDocumentosPorUsuario(usuarioId, includeDetails)
        }
    }

    /**
     * Verifica si usuario tiene documentos aprobados
     */
    suspend fun verificarDocumentosAprobados(usuarioId: Long): ApiResult<Boolean> {
        Log.d(TAG, "Verificando documentos aprobados para usuario: $usuarioId")
        return safeApiCall {
            api.verificarDocumentosAprobados(usuarioId)
        }
    }

    /**
     * Actualiza estado de documento (sin observaciones)
     */
    suspend fun actualizarEstadoDocumento(
        documentoId: Long,
        nuevoEstadoId: Long
    ): ApiResult<DocumentoRemoteDTO> {
        Log.d(TAG, "Actualizando estado de documento $documentoId a $nuevoEstadoId")
        return safeApiCall {
            api.actualizarEstadoDocumento(documentoId, nuevoEstadoId)
        }
    }

    /**
     * Actualiza estado de documento CON observaciones (para rechazos)
     */
    suspend fun actualizarEstadoConObservaciones(
        documentoId: Long,
        nuevoEstadoId: Long,
        observaciones: String?,
        revisadoPor: Long? = null
    ): ApiResult<DocumentoRemoteDTO> {
        Log.d(TAG, "Actualizando estado de documento $documentoId a $nuevoEstadoId con observaciones")

        val request = ActualizarEstadoRequest(
            estadoId = nuevoEstadoId,
            observaciones = observaciones,
            revisadoPor = revisadoPor
        )

        return safeApiCall {
            api.actualizarEstadoConObservaciones(documentoId, request)
        }
    }

    /**
     * Elimina un documento
     */
    suspend fun eliminarDocumento(id: Long): ApiResult<Unit> {
        Log.d(TAG, "Eliminando documento con ID: $id")
        return try {
            val response = api.eliminarDocumento(id)
            if (response.isSuccessful) {
                Log.d(TAG, "Documento $id eliminado exitosamente")
                ApiResult.Success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error ${response.code()}"
                Log.e(TAG, "Error al eliminar documento: $errorMsg")
                ApiResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepcion al eliminar documento: ${e.message}")
            ApiResult.Error(e.message ?: "Error de conexion")
        }
    }

    // ==================== ESTADOS ====================

    suspend fun listarEstados(): ApiResult<List<EstadoDocumentoDTO>> {
        return safeApiCall { api.listarEstados() }
    }

    suspend fun obtenerEstadoPorId(id: Long): ApiResult<EstadoDocumentoDTO> {
        return safeApiCall { api.obtenerEstadoPorId(id) }
    }

    // ==================== TIPOS DE DOCUMENTOS ====================

    suspend fun listarTiposDocumentos(): ApiResult<List<TipoDocumentoRemoteDTO>> {
        return safeApiCall { api.listarTiposDocumentos() }
    }

    suspend fun obtenerTipoDocumentoPorId(id: Long): ApiResult<TipoDocumentoRemoteDTO> {
        return safeApiCall { api.obtenerTipoDocumentoPorId(id) }
    }

    // ==================== HELPERS ====================

    fun generarNombreDocumento(
        tipoDocId: Long,
        nombreUsuario: String,
        extension: String = "pdf"
    ): String {
        val tipoNombre = when (tipoDocId) {
            TIPO_DNI -> "DNI"
            TIPO_PASAPORTE -> "PASAPORTE"
            TIPO_LIQUIDACION_SUELDO -> "LIQUIDACION"
            TIPO_CERTIFICADO_ANTECEDENTES -> "ANTECEDENTES"
            TIPO_CERTIFICADO_AFP -> "AFP"
            TIPO_CONTRATO_TRABAJO -> "CONTRATO"
            else -> "DOC"
        }

        val nombreLimpio = nombreUsuario
            .replace(" ", "_")
            .replace(Regex("[^A-Za-z0-9_]"), "")
            .take(20)

        val timestamp = System.currentTimeMillis()

        return "${tipoNombre}_${nombreLimpio}_$timestamp.$extension"
    }

    fun getNombreTipoDocumento(tipoDocId: Long): String {
        return when (tipoDocId) {
            TIPO_DNI -> "Cedula de Identidad"
            TIPO_PASAPORTE -> "Pasaporte"
            TIPO_LIQUIDACION_SUELDO -> "Liquidacion de Sueldo"
            TIPO_CERTIFICADO_ANTECEDENTES -> "Certificado de Antecedentes"
            TIPO_CERTIFICADO_AFP -> "Certificado AFP"
            TIPO_CONTRATO_TRABAJO -> "Contrato de Trabajo"
            else -> "Documento"
        }
    }

    fun getNombreEstado(estadoId: Long): String {
        return when (estadoId) {
            ESTADO_PENDIENTE -> "Pendiente"
            ESTADO_ACEPTADO -> "Aceptado"
            ESTADO_RECHAZADO -> "Rechazado"
            ESTADO_EN_REVISION -> "En Revision"
            else -> "Desconocido"
        }
    }
}