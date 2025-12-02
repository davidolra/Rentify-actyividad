package com.example.rentify.data.remote.dto

import com.example.rentify.data.local.entities.UsuarioEntity
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale

data class UsuarioRemoteDTO(
    val id: Long? = null,
    val pnombre: String,
    val snombre: String,
    val papellido: String,
    @SerializedName("fnacimiento")
    val fnacimiento: String,
    val email: String,
    val rut: String,
    val ntelefono: String,
    @SerializedName(value = "clave", alternate = ["password"]) // La clave puede ser nula al actualizar
    val clave: String? = null,
    val direccion: String? = null,
    val comuna: String? = null,
    @SerializedName("duocVip")
    val duocVip: Boolean? = null,
    val puntos: Int? = null,
    @SerializedName("codigoRef")
    val codigoRef: String? = null,
    @SerializedName("fcreacion")
    val fcreacion: String? = null,
    @SerializedName("factualizacion")
    val factualizacion: String? = null,
    @SerializedName("estadoId")
    val estadoId: Long? = null,
    @SerializedName("rolId")
    val rolId: Long? = null,
    val rol: RolRemoteDTO? = null,
    val estado: EstadoRemoteDTO? = null
)

fun UsuarioRemoteDTO.toEntity(): UsuarioEntity {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val fnacimientoTimestamp = try {
        sdf.parse(this.fnacimiento)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }

    val fcreacionTimestamp = try {
        this.fcreacion?.let { sdf.parse(it)?.time } ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    val factualizacionTimestamp = try {
        this.factualizacion?.let { sdf.parse(it)?.time } ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    return UsuarioEntity(
        id = this.id ?: 0L,
        pnombre = this.pnombre,
        snombre = this.snombre,
        papellido = this.papellido,
        fnacimiento = fnacimientoTimestamp,
        email = this.email,
        rut = this.rut,
        ntelefono = this.ntelefono,
        direccion = this.direccion,
        comuna = this.comuna,
        fotoPerfil = null, // El DTO no tiene foto de perfil, se maneja localmente
        clave = this.clave ?: "", // La entidad requiere una clave no nula
        duoc_vip = this.duocVip ?: false,
        puntos = this.puntos ?: 0,
        codigo_ref = this.codigoRef ?: "", // La entidad requiere un código de referencia no nulo
        fcreacion = fcreacionTimestamp,
        factualizacion = factualizacionTimestamp,
        estado_id = this.estadoId ?: 1L, // Asumiendo 1 como estado por defecto
        rol_id = this.rolId
    )
}

data class LoginRemoteDTO(
    val email: String,
    val clave: String
)

data class LoginResponseRemoteDTO(
    val mensaje: String,
    val usuario: UsuarioRemoteDTO
)

data class RolRemoteDTO(
    val id: Long,
    val nombre: String
)

data class EstadoRemoteDTO(
    val id: Long,
    val nombre: String
)

data class UserServiceErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val validationErrors: Map<String, String>? = null
) {
    fun getUserFriendlyMessage(): String {
        if (!validationErrors.isNullOrEmpty()) {
            return validationErrors.values.joinToString("\n")
        }
        return when (status) {
            400 -> "Error de validacion: $message"
            401 -> "Error de autenticacion: $message"
            404 -> "Recurso no encontrado: $message"
            409 -> "Conflicto: $message"
            500 -> "Error del servidor. Por favor intente nuevamente."
            503 -> "Servicio no disponible. Por favor intente mas tarde."
            else -> message
        }
    }

    fun getFirstValidationError(): String? {
        return validationErrors?.values?.firstOrNull()
    }

    fun hasFieldError(field: String): Boolean {
        return validationErrors?.containsKey(field) == true
    }

    fun getFieldError(field: String): String? {
        return validationErrors?.get(field)
    }
}