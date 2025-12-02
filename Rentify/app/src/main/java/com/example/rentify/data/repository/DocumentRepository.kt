package com.example.rentify.data.repository

import com.example.rentify.data.remote.api.DocumentServiceApi
import com.example.rentify.data.remote.dto.DocumentoRemoteDTO
import com.example.rentify.data.remote.dto.TipoDocumentoRemoteDTO
import retrofit2.HttpException
import java.io.IOException

class DocumentRepository(
    private val documentServiceApi: DocumentServiceApi
) {
    suspend fun getDocumentTypes(): List<TipoDocumentoRemoteDTO> {
        return try {
            val response = documentServiceApi.listarTiposDocumentos()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: IOException) {
            emptyList()
        } catch (e: HttpException) {
            emptyList()
        }
    }

    suspend fun getDocumentsByUserId(userId: Long): List<DocumentoRemoteDTO> {
        return try {
            val response = documentServiceApi.obtenerDocumentosPorUsuario(userId)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: IOException) {
            emptyList()
        } catch (e: HttpException) {
            emptyList()
        }
    }

    suspend fun uploadDocument(document: DocumentoRemoteDTO): DocumentoRemoteDTO? {
        return try {
            val response = documentServiceApi.crearDocumento(document)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: IOException) {
            null
        } catch (e: HttpException) {
            null
        }
    }
}
