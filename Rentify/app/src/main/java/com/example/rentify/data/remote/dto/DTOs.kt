package com.example.rentify.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.util.Date

// ==================== USER SERVICE DTOs ====================

data class UsuarioDTO(
    val id: Long? = null,
    val pnombre: String? = null,
    val snombre: String? = null,
    val papellido: String? = null,
    val email: String? = null,
    val ntelefono: String? = null,

    @SerializedName("rolId")
    val rolId: Int? = null,

    @SerializedName("estadoId")
    val estadoId: Int? = null,

    val rol: RolInfo? = null,
    val estado: EstadoInfo? = null,

    @SerializedName("duocVip")
    val duocVip: Boolean? = null
) {
    data class RolInfo(val id: Int, val nombre: String)
    data class EstadoInfo(val id: Int, val nombre: String)

    fun getNombreCompleto(): String {
        return listOfNotNull(pnombre, snombre, papellido)
            .filter { it?.isNotBlank() == true }
            .joinToString(" ")
            .ifEmpty { "Usuario" }
    }
}

data class RegistroDTO(
    val pnombre: String,
    val snombre: String? = null,
    val papellido: String,
    val email: String,
    val password: String,
    val ntelefono: String? = null,
    @SerializedName("rolId")
    val rolId: Int = 3,
    @SerializedName("estadoId")
    val estadoId: Int = 1,
    @SerializedName("duocVip")
    val duocVip: Boolean = false
)

data class LoginDTO(
    val email: String,
    val password: String
)

data class LoginResponseDTO(
    val mensaje: String? = null,
    val usuario: UsuarioDTO? = null,
    val token: String? = null
)

// ==================== APPLICATION SERVICE DTOs ====================

data class SolicitudArriendoDTO(
    val id: Long? = null,

    @SerializedName("usuarioId")
    val usuarioId: Long,

    @SerializedName("propiedadId")
    val propiedadId: Long,

    val estado: String? = null,

    @SerializedName("fechaSolicitud")
    val fechaSolicitud: Date? = null,

    val usuario: UsuarioDTO? = null,
    val propiedad: PropiedadDTO? = null
)

data class RegistroArriendoDTO(
    val id: Long? = null,

    @SerializedName("solicitudId")
    val solicitudId: Long,

    @SerializedName("fechaInicio")
    val fechaInicio: Date,

    @SerializedName("fechaFin")
    val fechaFin: Date? = null,

    @SerializedName("montoMensual")
    val montoMensual: Double,

    val activo: Boolean? = null,
    val solicitud: SolicitudArriendoDTO? = null
)

// ==================== PROPERTY SERVICE DTOs ====================

data class PropiedadDTO(
    val id: Long? = null,
    val codigo: String? = null,
    val titulo: String? = null,
    val direccion: String? = null,

    @SerializedName("precioMensual")
    val precioMensual: Double? = null,

    val divisa: String? = "CLP",
    val m2: Double? = null,

    @SerializedName("nHabit")
    val nHabit: Int? = null,

    @SerializedName("nBanos")
    val nBanos: Int? = null,

    @SerializedName("petFriendly")
    val petFriendly: Boolean? = null,

    @SerializedName("tipoId")
    val tipoId: Long? = null,

    @SerializedName("comunaId")
    val comunaId: Long? = null,

    val fcreacion: String? = null,
    val tipo: TipoDTO? = null,
    val comuna: ComunaDTO? = null,

    @SerializedName("propietarioId")
    val propietarioId: Long? = null
)

data class TipoDTO(
    val id: Long? = null,
    val nombre: String? = null
)

data class ComunaDTO(
    val id: Long? = null,
    val nombre: String? = null,
    val regionId: Long? = null
)

// ==================== CATALOG DTOs ====================

data class RegionDTO(val id: Long, val nombre: String)
data class CategoriaDTO(val id: Long, val nombre: String)
data class EstadoDTO(val id: Long, val nombre: String)
data class RolDTO(val id: Long, val nombre: String)

// ==================== REVIEW SERVICE DTOs ====================

/**
 * DTO para Resena
 */
data class ResenaDTO(
    val id: Long? = null,

    @SerializedName("propiedadId")
    val propiedadId: Long? = null,

    @SerializedName("usuarioId")
    val usuarioId: Long,

    @SerializedName("usuarioResenadoId")
    val usuarioResenadoId: Long? = null,

    @SerializedName("tipoResenaId")
    val tipoResenaId: Long,

    val puntuacion: Int,
    val comentario: String? = null,

    @SerializedName("fechaCreacion")
    val fechaCreacion: Date? = null,

    val usuario: UsuarioDTO? = null,
    val tipoResena: TipoResenaDTO? = null
)

data class CrearResenaDTO(
    @SerializedName("propiedadId")
    val propiedadId: Long? = null,

    @SerializedName("usuarioId")
    val usuarioId: Long,

    @SerializedName("usuarioResenadoId")
    val usuarioResenadoId: Long? = null,

    @SerializedName("tipoResenaId")
    val tipoResenaId: Long,

    val puntuacion: Int,
    val comentario: String? = null
)

/**
 * DTO para tipo de resena - id es Int para compatibilidad
 */
data class TipoResenaDTO(
    val id: Int,
    val nombre: String
)

data class ResenaEstadisticasDTO(
    val propiedadId: Long,
    val promedioGeneral: Double,
    val totalResenas: Int,
    val promedioPorTipo: Map<String, Double>? = null
)

// ==================== CONSTANTES ====================

object EstadoSolicitud {
    const val PENDIENTE = "PENDIENTE"
    const val ACEPTADA = "ACEPTADA"
    const val RECHAZADA = "RECHAZADA"
}

object RolesRentify {
    const val ADMIN = 1
    const val PROPIETARIO = 2
    const val ARRIENDATARIO = 3
}

object LimitesRentify {
    const val MAX_SOLICITUDES_ACTIVAS = 3
}