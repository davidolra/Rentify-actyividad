package com.example.rentify.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==================== PROPERTY SERVICE DTOs ====================

/**
 * DTO para Propiedad (Property)
 * Compatible con PropertyDTO.java del backend
 */
data class PropertyRemoteDTO(
    val id: Long? = null,

    val codigo: String,
    val titulo: String,

    @SerializedName("precioMensual")
    val precioMensual: Double,

    val divisa: String = "CLP",

    val m2: Double,

    @SerializedName("nHabit")
    val nHabit: Int,

    @SerializedName("nBanos")
    val nBanos: Int,

    @SerializedName("petFriendly")
    val petFriendly: Boolean = false,

    val direccion: String,

    val fcreacion: String? = null,

    @SerializedName("tipoId")
    val tipoId: Long,

    @SerializedName("comunaId")
    val comunaId: Long,

    @SerializedName("propietarioId")
    val propietarioId: Long? = null,

    // Campos opcionales cuando includeDetails=true
    val tipo: TipoRemoteDTO? = null,
    val comuna: ComunaRemoteDTO? = null,
    val fotos: List<FotoRemoteDTO>? = null,
    val categorias: List<CategoriaRemoteDTO>? = null
)

/**
 * DTO para crear propiedad
 */
data class PropertyCreateDTO(
    val codigo: String,
    val titulo: String,

    @SerializedName("precioMensual")
    val precioMensual: Double,

    val divisa: String = "CLP",

    val m2: Double,

    @SerializedName("nHabit")
    val nHabit: Int,

    @SerializedName("nBanos")
    val nBanos: Int,

    @SerializedName("petFriendly")
    val petFriendly: Boolean = false,

    val direccion: String,

    @SerializedName("tipoId")
    val tipoId: Long,

    @SerializedName("comunaId")
    val comunaId: Long,

    @SerializedName("propietarioId")
    val propietarioId: Long? = null,

    @SerializedName("categoriaIds")
    val categoriaIds: List<Long>? = null
)

/**
 * DTO para Foto
 */
data class FotoRemoteDTO(
    val id: Long? = null,
    val nombre: String,
    val url: String,
    val sortOrder: Int? = null,

    @SerializedName("propiedadId")
    val propiedadId: Long
)

/**
 * DTO para Tipo de Propiedad
 */
data class TipoRemoteDTO(
    val id: Long? = null,
    val nombre: String
)

/**
 * DTO para Comuna
 */
data class ComunaRemoteDTO(
    val id: Long? = null,
    val nombre: String,

    @SerializedName("regionId")
    val regionId: Long,

    val region: RegionRemoteDTO? = null
)

/**
 * DTO para Region
 */
data class RegionRemoteDTO(
    val id: Long? = null,
    val nombre: String
)

/**
 * DTO para Categoria
 */
data class CategoriaRemoteDTO(
    val id: Long? = null,
    val nombre: String
)

/**
 * Respuesta de error del backend Property Service
 */
data class PropertyServiceErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val validationErrors: Map<String, String>? = null
)