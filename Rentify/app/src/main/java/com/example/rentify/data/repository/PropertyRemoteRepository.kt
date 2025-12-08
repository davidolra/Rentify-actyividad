package com.example.rentify.data.repository

import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.*
import com.example.rentify.data.remote.safeApiCall
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class PropertyRemoteRepository {

    private val api = RetrofitClient.propertyServiceApi

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

    suspend fun listarTodasPropiedades(
        includeDetails: Boolean = false
    ): ApiResult<List<PropertyRemoteDTO>> {
        return safeApiCall {
            api.listarTodasPropiedades(includeDetails)
        }
    }

    suspend fun obtenerPropiedadPorId(
        propiedadId: Long,
        includeDetails: Boolean = true
    ): ApiResult<PropertyRemoteDTO> {
        return safeApiCall {
            api.obtenerPropiedadPorId(propiedadId, includeDetails)
        }
    }

    suspend fun obtenerPropiedadPorCodigo(
        codigo: String,
        includeDetails: Boolean = true
    ): ApiResult<PropertyRemoteDTO> {
        return safeApiCall {
            api.obtenerPropiedadPorCodigo(codigo, includeDetails)
        }
    }

    suspend fun actualizarPropiedad(
        propiedadId: Long,
        propertyDTO: PropertyRemoteDTO
    ): ApiResult<PropertyRemoteDTO> {
        return safeApiCall {
            api.actualizarPropiedad(propiedadId, propertyDTO)
        }
    }

    suspend fun eliminarPropiedad(
        propiedadId: Long
    ): ApiResult<Void> {
        return safeApiCall {
            api.eliminarPropiedad(propiedadId)
        }
    }

    suspend fun buscarPropiedadesConFiltros(
        tipoId: Long? = null,
        comunaId: Long? = null,
        minPrecio: Double? = null,
        maxPrecio: Double? = null,
        nHabit: Int? = null,
        nBanos: Int? = null,
        petFriendly: Boolean? = null,
        includeDetails: Boolean = false
    ): ApiResult<List<PropertyRemoteDTO>> {
        return safeApiCall {
            api.buscarPropiedadesConFiltros(
                tipoId, comunaId, minPrecio, maxPrecio,
                nHabit, nBanos, petFriendly, includeDetails
            )
        }
    }

    suspend fun existePropiedad(propiedadId: Long): ApiResult<Boolean> {
        return safeApiCall {
            api.existePropiedad(propiedadId)
        }
    }

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

    suspend fun listarFotos(propiedadId: Long): ApiResult<List<FotoRemoteDTO>> {
        return safeApiCall {
            api.listarFotos(propiedadId)
        }
    }

    suspend fun obtenerFoto(fotoId: Long): ApiResult<FotoRemoteDTO> {
        return safeApiCall {
            api.obtenerFoto(fotoId)
        }
    }

    suspend fun eliminarFoto(fotoId: Long): ApiResult<Void> {
        return safeApiCall {
            api.eliminarFoto(fotoId)
        }
    }

    suspend fun reordenarFotos(
        propiedadId: Long,
        fotosIds: List<Long>
    ): ApiResult<Void> {
        return safeApiCall {
            api.reordenarFotos(propiedadId, fotosIds)
        }
    }

    suspend fun crearTipo(nombre: String): ApiResult<TipoRemoteDTO> {
        val tipoDTO = TipoRemoteDTO(nombre = nombre)
        return safeApiCall {
            api.crearTipo(tipoDTO)
        }
    }

    suspend fun listarTipos(): ApiResult<List<TipoRemoteDTO>> {
        return safeApiCall {
            api.listarTipos()
        }
    }

    suspend fun obtenerTipoPorId(tipoId: Long): ApiResult<TipoRemoteDTO> {
        return safeApiCall {
            api.obtenerTipoPorId(tipoId)
        }
    }

    suspend fun actualizarTipo(
        tipoId: Long,
        nombre: String
    ): ApiResult<TipoRemoteDTO> {
        val tipoDTO = TipoRemoteDTO(id = tipoId, nombre = nombre)
        return safeApiCall {
            api.actualizarTipo(tipoId, tipoDTO)
        }
    }

    suspend fun eliminarTipo(tipoId: Long): ApiResult<Void> {
        return safeApiCall {
            api.eliminarTipo(tipoId)
        }
    }

    suspend fun crearComuna(
        nombre: String,
        regionId: Long
    ): ApiResult<ComunaRemoteDTO> {
        val comunaDTO = ComunaRemoteDTO(nombre = nombre, regionId = regionId)
        return safeApiCall {
            api.crearComuna(comunaDTO)
        }
    }

    suspend fun listarComunas(): ApiResult<List<ComunaRemoteDTO>> {
        return safeApiCall {
            api.listarComunas()
        }
    }

    suspend fun obtenerComunaPorId(comunaId: Long): ApiResult<ComunaRemoteDTO> {
        return safeApiCall {
            api.obtenerComunaPorId(comunaId)
        }
    }

    suspend fun obtenerComunasPorRegion(regionId: Long): ApiResult<List<ComunaRemoteDTO>> {
        return safeApiCall {
            api.obtenerComunasPorRegion(regionId)
        }
    }

    suspend fun actualizarComuna(
        comunaId: Long,
        nombre: String,
        regionId: Long
    ): ApiResult<ComunaRemoteDTO> {
        val comunaDTO = ComunaRemoteDTO(id = comunaId, nombre = nombre, regionId = regionId)
        return safeApiCall {
            api.actualizarComuna(comunaId, comunaDTO)
        }
    }

    suspend fun eliminarComuna(comunaId: Long): ApiResult<Void> {
        return safeApiCall {
            api.eliminarComuna(comunaId)
        }
    }

    suspend fun crearRegion(nombre: String): ApiResult<RegionRemoteDTO> {
        val regionDTO = RegionRemoteDTO(nombre = nombre)
        return safeApiCall {
            api.crearRegion(regionDTO)
        }
    }

    suspend fun listarRegiones(): ApiResult<List<RegionRemoteDTO>> {
        return safeApiCall {
            api.listarRegiones()
        }
    }

    suspend fun obtenerRegionPorId(regionId: Long): ApiResult<RegionRemoteDTO> {
        return safeApiCall {
            api.obtenerRegionPorId(regionId)
        }
    }

    suspend fun actualizarRegion(
        regionId: Long,
        nombre: String
    ): ApiResult<RegionRemoteDTO> {
        val regionDTO = RegionRemoteDTO(id = regionId, nombre = nombre)
        return safeApiCall {
            api.actualizarRegion(regionId, regionDTO)
        }
    }

    suspend fun eliminarRegion(regionId: Long): ApiResult<Void> {
        return safeApiCall {
            api.eliminarRegion(regionId)
        }
    }

    suspend fun crearCategoria(nombre: String): ApiResult<CategoriaRemoteDTO> {
        val categoriaDTO = CategoriaRemoteDTO(nombre = nombre)
        return safeApiCall {
            api.crearCategoria(categoriaDTO)
        }
    }

    suspend fun listarCategorias(): ApiResult<List<CategoriaRemoteDTO>> {
        return safeApiCall {
            api.listarCategorias()
        }
    }

    suspend fun obtenerCategoriaPorId(categoriaId: Long): ApiResult<CategoriaRemoteDTO> {
        return safeApiCall {
            api.obtenerCategoriaPorId(categoriaId)
        }
    }

    suspend fun actualizarCategoria(
        categoriaId: Long,
        nombre: String
    ): ApiResult<CategoriaRemoteDTO> {
        val categoriaDTO = CategoriaRemoteDTO(id = categoriaId, nombre = nombre)
        return safeApiCall {
            api.actualizarCategoria(categoriaId, categoriaDTO)
        }
    }

    suspend fun eliminarCategoria(categoriaId: Long): ApiResult<Void> {
        return safeApiCall {
            api.eliminarCategoria(categoriaId)
        }
    }
}