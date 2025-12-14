package com.example.rentify.data.repository

import android.util.Log
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.*
import com.example.rentify.data.remote.safeApiCall
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import android.webkit.MimeTypeMap

/**
 * Repositorio para comunicacion con Property Service (Puerto 8082)
 * Incluye manejo de errores y logging completo
 */
class PropertyRemoteRepository {

    private val api = RetrofitClient.propertyServiceApi

    companion object {
        private const val TAG = "PropertyRemoteRepo"
    }

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
        comunaId: Long,
        propietarioId: Long? = null
    ): ApiResult<PropertyRemoteDTO> {
        Log.d(TAG, "Creando propiedad: codigo=$codigo, titulo=$titulo")

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
            comunaId = comunaId,
            propietarioId = propietarioId
        )

        return when (val result = safeApiCall { api.crearPropiedad(propertyDTO) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Propiedad creada: id=${result.data.id}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al crear propiedad: ${result.message}")
                val friendlyMessage = parsePropertyError(result.message, result.code)
                ApiResult.Error(friendlyMessage, result.code)
            }
            else -> result
        }
    }

    /**
     * Listar todas las propiedades
     */
    suspend fun listarTodasPropiedades(
        includeDetails: Boolean = false
    ): ApiResult<List<PropertyRemoteDTO>> {
        Log.d(TAG, "Listando propiedades: includeDetails=$includeDetails")

        return when (val result = safeApiCall { api.listarTodasPropiedades(includeDetails) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Propiedades obtenidas: ${result.data.size}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al listar propiedades: ${result.message}")
                result
            }
            else -> result
        }
    }

    /**
     * Obtener propiedad por ID
     */
    suspend fun obtenerPropiedadPorId(
        propiedadId: Long,
        includeDetails: Boolean = true
    ): ApiResult<PropertyRemoteDTO> {
        Log.d(TAG, "Obteniendo propiedad: id=$propiedadId")

        return when (val result = safeApiCall { api.obtenerPropiedadPorId(propiedadId, includeDetails) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Propiedad obtenida: ${result.data.titulo}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al obtener propiedad: ${result.message}")
                result
            }
            else -> result
        }
    }

    /**
     * Obtener propiedad por codigo
     */
    suspend fun obtenerPropiedadPorCodigo(
        codigo: String,
        includeDetails: Boolean = true
    ): ApiResult<PropertyRemoteDTO> {
        Log.d(TAG, "Obteniendo propiedad por codigo: $codigo")

        return safeApiCall { api.obtenerPropiedadPorCodigo(codigo, includeDetails) }
    }

    /**
     * Actualizar propiedad
     */
    suspend fun actualizarPropiedad(
        propiedadId: Long,
        propertyDTO: PropertyRemoteDTO
    ): ApiResult<PropertyRemoteDTO> {
        Log.d(TAG, "Actualizando propiedad: id=$propiedadId")

        return when (val result = safeApiCall { api.actualizarPropiedad(propiedadId, propertyDTO) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Propiedad actualizada: ${result.data.titulo}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al actualizar: ${result.message}")
                val friendlyMessage = parsePropertyError(result.message, result.code)
                ApiResult.Error(friendlyMessage, result.code)
            }
            else -> result
        }
    }

    /**
     * Eliminar propiedad
     */
    suspend fun eliminarPropiedad(propiedadId: Long): ApiResult<Unit> {
        Log.d(TAG, "Eliminando propiedad: id=$propiedadId")

        return when (val result = safeApiCall { api.eliminarPropiedad(propiedadId) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Propiedad eliminada exitosamente")
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al eliminar: ${result.message}")
                result
            }
            else -> ApiResult.Success(Unit)
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
        Log.d(TAG, "Buscando con filtros: comunaId=$comunaId, tipoId=$tipoId")

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
        return safeApiCall { api.existePropiedad(propiedadId) }
    }

    // ==================== FOTOS ====================

    /**
     * Subir foto a propiedad
     */
    suspend fun subirFoto(propiedadId: Long, file: File): ApiResult<FotoRemoteDTO> {
        Log.d(TAG, "Subiendo foto: propiedad=$propiedadId, archivo=${file.name}")

        val extension = MimeTypeMap.getFileExtensionFromUrl(file.path)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "image/jpeg"

        // Usar el tipo MIME real (ej: image/jpeg, image/png)
        val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())


        val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestBody)

        return when (val result = safeApiCall { api.subirFoto(propiedadId, multipartBody) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Foto subida: id=${result.data.id}")
                result
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al subir foto: ${result.message}")
                val friendlyMessage = parsePhotoError(result.message, result.code)
                ApiResult.Error(friendlyMessage, result.code)
            }
            else -> result
        }
    }

    /**
     * Listar fotos de propiedad
     */
    suspend fun listarFotos(propiedadId: Long): ApiResult<List<FotoRemoteDTO>> {
        Log.d(TAG, "Listando fotos: propiedad=$propiedadId")
        return safeApiCall { api.listarFotos(propiedadId) }
    }

    /**
     * Obtener foto por ID
     */
    suspend fun obtenerFoto(fotoId: Long): ApiResult<FotoRemoteDTO> {
        return safeApiCall { api.obtenerFoto(fotoId) }
    }

    /**
     * Eliminar foto
     */
    suspend fun eliminarFoto(fotoId: Long): ApiResult<Unit> {
        Log.d(TAG, "Eliminando foto: id=$fotoId")

        return when (val result = safeApiCall { api.eliminarFoto(fotoId) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "Foto eliminada exitosamente")
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Error al eliminar foto: ${result.message}")
                result
            }
            else -> ApiResult.Success(Unit)
        }
    }

    /**
     * Reordenar fotos de propiedad
     */
    suspend fun reordenarFotos(propiedadId: Long, fotosIds: List<Long>): ApiResult<Unit> {
        Log.d(TAG, "Reordenando fotos: propiedad=$propiedadId")

        return when (val result = safeApiCall { api.reordenarFotos(propiedadId, fotosIds) }) {
            is ApiResult.Success -> ApiResult.Success(Unit)
            is ApiResult.Error -> result
            else -> ApiResult.Success(Unit)
        }
    }

    // ==================== CATALOGOS ====================

    /**
     * Listar todos los tipos
     */
    suspend fun listarTipos(): ApiResult<List<TipoRemoteDTO>> {
        Log.d(TAG, "Listando tipos")
        return safeApiCall { api.listarTipos() }
    }

    /**
     * Obtener tipo por ID
     */
    suspend fun obtenerTipoPorId(id: Long): ApiResult<TipoRemoteDTO> {
        return safeApiCall { api.obtenerTipoPorId(id) }
    }

    /**
     * Listar todas las comunas
     */
    suspend fun listarComunas(): ApiResult<List<ComunaRemoteDTO>> {
        Log.d(TAG, "Listando comunas")
        return safeApiCall { api.listarComunas() }
    }

    /**
     * Obtener comuna por ID
     */
    suspend fun obtenerComunaPorId(id: Long): ApiResult<ComunaRemoteDTO> {
        return safeApiCall { api.obtenerComunaPorId(id) }
    }

    /**
     * Obtener comunas por region
     */
    suspend fun obtenerComunasPorRegion(regionId: Long): ApiResult<List<ComunaRemoteDTO>> {
        Log.d(TAG, "Obteniendo comunas de region: $regionId")
        return safeApiCall { api.obtenerComunasPorRegion(regionId) }
    }

    /**
     * Listar todas las regiones
     */
    suspend fun listarRegiones(): ApiResult<List<RegionRemoteDTO>> {
        Log.d(TAG, "Listando regiones")
        return safeApiCall { api.listarRegiones() }
    }

    /**
     * Obtener region por ID
     */
    suspend fun obtenerRegionPorId(id: Long): ApiResult<RegionRemoteDTO> {
        return safeApiCall { api.obtenerRegionPorId(id) }
    }

    /**
     * Listar todas las categorias
     */
    suspend fun listarCategorias(): ApiResult<List<CategoriaRemoteDTO>> {
        Log.d(TAG, "Listando categorias")
        return safeApiCall { api.listarCategorias() }
    }

    /**
     * Obtener categoria por ID
     */
    suspend fun obtenerCategoriaPorId(id: Long): ApiResult<CategoriaRemoteDTO> {
        return safeApiCall { api.obtenerCategoriaPorId(id) }
    }

    // ==================== PARSING DE ERRORES ====================

    /**
     * Parsear errores del backend para propiedades
     */
    private fun parsePropertyError(rawMessage: String, code: Int?): String {
        Log.d(TAG, "Parseando error propiedad: code=$code, message=$rawMessage")

        return when (code) {
            400 -> {
                when {
                    rawMessage.contains("codigo", ignoreCase = true) &&
                            (rawMessage.contains("duplicado", ignoreCase = true) ||
                                    rawMessage.contains("existe", ignoreCase = true)) ->
                        "El codigo de propiedad ya existe"

                    rawMessage.contains("divisa", ignoreCase = true) &&
                            rawMessage.contains("invalida", ignoreCase = true) ->
                        "Divisa no valida. Usa CLP, UF o USD"

                    rawMessage.contains("precio", ignoreCase = true) &&
                            rawMessage.contains("invalido", ignoreCase = true) ->
                        "El precio debe ser mayor a 0"

                    rawMessage.contains("m2", ignoreCase = true) &&
                            rawMessage.contains("invalido", ignoreCase = true) ->
                        "Los metros cuadrados deben estar entre 10 y 10000"

                    rawMessage.contains("habitaciones", ignoreCase = true) ->
                        "El numero de habitaciones debe estar entre 0 y 20"

                    rawMessage.contains("banos", ignoreCase = true) ->
                        "El numero de banos debe estar entre 1 y 10"

                    rawMessage.contains("tipo", ignoreCase = true) &&
                            rawMessage.contains("no encontrado", ignoreCase = true) ->
                        "El tipo de propiedad seleccionado no existe"

                    rawMessage.contains("comuna", ignoreCase = true) &&
                            rawMessage.contains("no encontrada", ignoreCase = true) ->
                        "La comuna seleccionada no existe"

                    rawMessage.contains("obligatorio", ignoreCase = true) ->
                        "Faltan campos obligatorios"

                    else -> "Error de validacion: $rawMessage"
                }
            }
            404 -> "Propiedad no encontrada"
            503 -> "Servicio no disponible. Intenta nuevamente."
            500 -> "Error interno del servidor"
            else -> rawMessage
        }
    }

    /**
     * Parsear errores del backend para fotos
     */
    private fun parsePhotoError(rawMessage: String, code: Int?): String {
        Log.d(TAG, "Parseando error foto: code=$code, message=$rawMessage")

        return when (code) {
            400 -> {
                when {
                    rawMessage.contains("vacio", ignoreCase = true) ||
                            rawMessage.contains("empty", ignoreCase = true) ->
                        "Debes seleccionar un archivo"

                    rawMessage.contains("formato", ignoreCase = true) ||
                            rawMessage.contains("tipo de archivo", ignoreCase = true) ->
                        "Formato no soportado. Usa JPG, PNG o GIF"

                    rawMessage.contains("tamano", ignoreCase = true) ||
                            rawMessage.contains("size", ignoreCase = true) ||
                            rawMessage.contains("grande", ignoreCase = true) ->
                        "El archivo es muy grande. Maximo 10MB"

                    rawMessage.contains("maximo", ignoreCase = true) ||
                            rawMessage.contains("limite", ignoreCase = true) ||
                            rawMessage.contains("20", ignoreCase = true) ->
                        "Has alcanzado el limite de 20 fotos por propiedad"

                    else -> "Error al subir foto: $rawMessage"
                }
            }
            404 -> "Foto o propiedad no encontrada"
            else -> rawMessage
        }
    }
}