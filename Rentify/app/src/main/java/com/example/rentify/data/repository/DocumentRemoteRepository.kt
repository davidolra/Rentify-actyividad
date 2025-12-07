package com.example.rentify.data.repository

import android.util.Log
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.DocumentoRemoteDTO
import com.example.rentify.data.remote.dto.EstadoDocumentoDTO
import com.example.rentify.data.remote.dto.TipoDocumentoRemoteDTO
import com.example.rentify.data.remote.safeApiCall
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Repositorio para comunicación con Document Service (Puerto 8083).
 * Gestiona todas las operaciones relacionadas con documentos de usuarios.
 */
class DocumentRemoteRepository {

    private val api = RetrofitClient.documentServiceApi

    companion object {
        private const val TAG = "DocumentRemoteRepo"

        // IDs de estados (coinciden con backend)
        const val ESTADO_PENDIENTE = 1L
        const val ESTADO_ACEPTADO = 2L
        const val ESTADO_RECHAZADO = 3L
        const val ESTADO_EN_REVISION = 4L

        // IDs de tipos de documento (coinciden con backend)
        const val TIPO_DNI = 1L
        const val TIPO_PASAPORTE = 2L
        const val TIPO_LIQUIDACION_SUELDO = 3L
        const val TIPO_CERTIFICADO_ANTECEDENTES = 4L
        const val TIPO_CERTIFICADO_AFP = 5L
        const val TIPO_CONTRATO_TRABAJO = 6L
    }

    // ==================== DOCUMENTOS ====================

    /**
     * Crea/sube un nuevo documento al servidor.
     *
     * @param nombre Nombre descriptivo del documento (ej: "DNI_Juan_Perez.pdf")
     * @param usuarioId ID del usuario propietario
     * @param tipoDocId ID del tipo de documento (1=DNI, 2=PASAPORTE, etc.)
     * @param estadoId ID del estado inicial (por defecto PENDIENTE=1)
     * @return ApiResult con el documento creado o error
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
     * Sube múltiples documentos para un usuario.
     * Útil después del registro exitoso.
     *
     * @param usuarioId ID del usuario
     * @param documentos Lista de pares (tipoDocId, nombreArchivo)
     * @return Lista de resultados por cada documento
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
     * Obtiene todos los documentos del sistema.
     */
    suspend fun listarTodosDocumentos(includeDetails: Boolean = false): ApiResult<List<DocumentoRemoteDTO>> {
        Log.d(TAG, "Listando todos los documentos (includeDetails=$includeDetails)")
        return safeApiCall {
            api.listarTodosDocumentos(includeDetails)
        }
    }

    /**
     * Obtiene un documento por su ID.
     */
    suspend fun obtenerDocumentoPorId(id: Long, includeDetails: Boolean = true): ApiResult<DocumentoRemoteDTO> {
        Log.d(TAG, "Obteniendo documento con ID: $id")
        return safeApiCall {
            api.obtenerDocumentoPorId(id, includeDetails)
        }
    }

    /**
     * Obtiene todos los documentos de un usuario.
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
     * Verifica si un usuario tiene documentos aprobados.
     * Útil para validar antes de crear solicitudes de arriendo.
     */
    suspend fun verificarDocumentosAprobados(usuarioId: Long): ApiResult<Boolean> {
        Log.d(TAG, "Verificando documentos aprobados para usuario: $usuarioId")
        return safeApiCall {
            api.verificarDocumentosAprobados(usuarioId)
        }
    }

    /**
     * Actualiza el estado de un documento.
     * Solo administradores pueden usar esta función.
     *
     * @param documentoId ID del documento
     * @param nuevoEstadoId Nuevo estado (1=PENDIENTE, 2=ACEPTADO, 3=RECHAZADO, 4=EN_REVISION)
     */
    suspend fun actualizarEstadoDocumento(
        documentoId: Long,
        nuevoEstadoId: Long
    ): ApiResult<DocumentoRemoteDTO> {
        Log.d(TAG, "Actualizando estado de documento $documentoId a estado $nuevoEstadoId")
        return safeApiCall {
            api.actualizarEstadoDocumento(documentoId, nuevoEstadoId)
        }
    }

    /**
     * Elimina un documento.
     */
    suspend fun eliminarDocumento(id: Long): ApiResult<Unit> {
        Log.d(TAG, "Eliminando documento con ID: $id")
        return safeApiCall {
            val response = api.eliminarDocumento(id)
            if (response.isSuccessful) {
                retrofit2.Response.success(Unit)
            } else {
                val errorBody = response.errorBody() ?: "".toResponseBody(null)
                retrofit2.Response.error(response.code(), errorBody)
            }
        }
    }

    // ==================== ESTADOS ====================

    /**
     * Obtiene todos los estados disponibles.
     */
    suspend fun listarEstados(): ApiResult<List<EstadoDocumentoDTO>> {
        Log.d(TAG, "Listando estados de documentos")
        return safeApiCall {
            api.listarEstados()
        }
    }

    /**
     * Obtiene un estado por su ID.
     */
    suspend fun obtenerEstadoPorId(id: Long): ApiResult<EstadoDocumentoDTO> {
        Log.d(TAG, "Obteniendo estado con ID: $id")
        return safeApiCall {
            api.obtenerEstadoPorId(id)
        }
    }

    // ==================== TIPOS DE DOCUMENTOS ====================

    /**
     * Obtiene todos los tipos de documentos disponibles.
     */
    suspend fun listarTiposDocumentos(): ApiResult<List<TipoDocumentoRemoteDTO>> {
        Log.d(TAG, "Listando tipos de documentos")
        return safeApiCall {
            api.listarTiposDocumentos()
        }
    }

    /**
     * Obtiene un tipo de documento por su ID.
     */
    suspend fun obtenerTipoDocumentoPorId(id: Long): ApiResult<TipoDocumentoRemoteDTO> {
        Log.d(TAG, "Obteniendo tipo de documento con ID: $id")
        return safeApiCall {
            api.obtenerTipoDocumentoPorId(id)
        }
    }

    // ==================== HELPERS ====================

    /**
     * Genera un nombre de archivo estandarizado para un documento.
     * Formato: TIPO_NombreUsuario_Timestamp.extension
     */
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

    /**
     * Obtiene el nombre legible de un tipo de documento.
     */
    fun getNombreTipoDocumento(tipoDocId: Long): String {
        return when (tipoDocId) {
            TIPO_DNI -> "Cédula de Identidad"
            TIPO_PASAPORTE -> "Pasaporte"
            TIPO_LIQUIDACION_SUELDO -> "Liquidación de Sueldo"
            TIPO_CERTIFICADO_ANTECEDENTES -> "Certificado de Antecedentes"
            TIPO_CERTIFICADO_AFP -> "Certificado AFP"
            TIPO_CONTRATO_TRABAJO -> "Contrato de Trabajo"
            else -> "Documento"
        }
    }

    /**
     * Obtiene el nombre legible de un estado.
     */
    fun getNombreEstado(estadoId: Long): String {
        return when (estadoId) {
            ESTADO_PENDIENTE -> "Pendiente"
            ESTADO_ACEPTADO -> "Aceptado"
            ESTADO_RECHAZADO -> "Rechazado"
            ESTADO_EN_REVISION -> "En Revisión"
            else -> "Desconocido"
        }
    }
}
