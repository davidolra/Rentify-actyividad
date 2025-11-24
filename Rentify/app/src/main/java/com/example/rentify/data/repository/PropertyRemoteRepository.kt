package com.example.rentify.data.repository

import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.*
import com.example.rentify.data.remote.safeApiCall
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Repositorio para sincronización con Property Service (remoto)
 */
class PropertyRemoteRepository {

    private val api = RetrofitClient.propertyServiceApi

    // ==================== PROPIEDADES ====================

    /**
     * Crear nueva propiedad
     */
    suspend fun crearPropiedad(
        codigo: String,
        titulo: String,
        precioMensual: Double,
        divisa: String = "CLP",
        m2: Double,
        nHabit: Int,
        nBanos: Int,
        petFriendly: Boolean,
        direccion: String,
        tipoId: Long,
        comunaId: Long
    ): ApiResult<PropertyRemoteDTO> {
        val propertyDTO = PropertyRemoteDTO(
            codigo = codigo,
            titulo = titulo,
            precioMensual = precioMensual,
            divisa = divisa,
            m2 = m2,
            nHabit = nHabit,
            nBanos = nBanos,
            petFriendly = petFriendly,
            direccion = direccion,
            tipoId = tipoId,
            comunaId = comunaId
        )

        return safeApiCall {
            api.crearPropiedad(propertyDTO)
        }
    }

    /**
     * Listar todas las propiedades
     */
    suspend fun listarTodasPropiedades(
        includeDetails: Boolean = false
    ): ApiResult<List<PropertyRemoteDTO>> {
        return safeApiCall {
            api.listarTodasPropiedades(includeDetails)
        }
    }

    /**
     * Obtener propiedad por ID
     */
    suspend fun obtenerPropiedadPorId(
        propiedadId: Long,
        includeDetails: Boolean = true
    ): ApiResult<PropertyRemoteDTO> {
        return safeApiCall {
            api.obtenerPropiedadPorId(propiedadId, includeDetails)
        }
    }

    /**
     * Obtener propiedad por código
     */
    suspend fun obtenerPropiedadPorCodigo(
        codigo: String,
        includeDetails: Boolean = true
    ): ApiResult<PropertyRemoteDTO> {
        return safeApiCall {
            api.obtenerPropiedadPorCodigo(codigo, includeDetails)
        }
    }

    /**
     * Actualizar propiedad
     */
    suspend fun actualizarPropiedad(
        propiedadId: Long,
        propertyDTO: PropertyRemoteDTO
    ): ApiResult<PropertyRemoteDTO> {
        return safeApiCall {
            api.actualizarPropiedad(propiedadId, propertyDTO)
        }
    }

    /**
     * Eliminar propiedad
     */
    suspend fun eliminarPropiedad(
        propiedadId: Long
    ): ApiResult<Void> {
        return safeApiCall {
            api.eliminarPropiedad(propiedadId)
        }
    }

    /**
     * Buscar propiedades con filtros
     */
    suspend fun buscarPropiedadesConFiltros(
        comunaId: Long? = null,
        tipoId: Long? = null,
        minPrecio: Double? = null,
        maxPrecio: Double? = null,
        nHabit: Int? = null,
        nBanos: Int? = null,
        petFriendly: Boolean? = null,
        includeDetails: Boolean = false
    ): ApiResult<List<PropertyRemoteDTO>> {
        return safeApiCall {
            api.buscarPropiedadesConFiltros(
                comunaId, tipoId, minPrecio, maxPrecio,
                nHabit, nBanos, petFriendly, includeDetails
            )
        }
    }

    /**
     * Verificar existencia de propiedad
     */
    suspend fun existePropiedad(propiedadId: Long): ApiResult<Boolean> {
        return safeApiCall {
            api.existePropiedad(propiedadId)
        }
    }

    // ==================== FOTOS ====================

    /**
     * Subir foto a propiedad
     */
    suspend fun subirFoto(
        propiedadId: Long,
        file: File
    ): ApiResult<FotoRemoteDTO> {
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestBody)

        return safeApiCall {
            api.subirFoto(propiedadId, multipartBody)
        }
    }

    /**
     * Listar fotos de propiedad
     */
    suspend fun listarFotos(propiedadId: Long): ApiResult<List<FotoRemoteDTO>> {
        return safeApiCall {
            api.listarFotos(propiedadId)
        }
    }

    /**
     * Eliminar foto
     */
    suspend fun eliminarFoto(fotoId: Long): ApiResult<Void> {
        return safeApiCall {
            api.eliminarFoto(fotoId)
        }
    }

    // ==================== CATÁLOGOS ====================

    /**
     * Listar todos los tipos
     */
    suspend fun listarTipos(): ApiResult<List<TipoRemoteDTO>> {
        return safeApiCall {
            api.listarTipos()
        }
    }

    /**
     * Listar todas las comunas
     */
    suspend fun listarComunas(): ApiResult<List<ComunaRemoteDTO>> {
        return safeApiCall {
            api.listarComunas()
        }
    }

    /**
     * Obtener comunas por región
     */
    suspend fun obtenerComunasPorRegion(regionId: Long): ApiResult<List<ComunaRemoteDTO>> {
        return safeApiCall {
            api.obtenerComunasPorRegion(regionId)
        }
    }

    /**
     * Listar todas las regiones
     */
    suspend fun listarRegiones(): ApiResult<List<RegionRemoteDTO>> {
        return safeApiCall {
            api.listarRegiones()
        }
    }

    /**
     * Listar todas las categorías
     */
    suspend fun listarCategorias(): ApiResult<List<CategoriaRemoteDTO>> {
        return safeApiCall {
            api.listarCategorias()
        }
    }
}