package com.example.rentify.data.remote.constants

/**
 * Constantes que coinciden con el backend (ApplicationConstants.java)
 * Mantener sincronizado con el microservicio
 */
object ApplicationConstants {

    /**
     * Estados de Solicitud
     * Coinciden con ApplicationConstants.EstadoSolicitud del backend
     */
    object EstadoSolicitud {
        const val PENDIENTE = "PENDIENTE"
        const val ACEPTADA = "ACEPTADA"
        const val RECHAZADA = "RECHAZADA"

        fun esValido(estado: String): Boolean {
            return estado in listOf(PENDIENTE, ACEPTADA, RECHAZADA)
        }

        fun getColor(estado: String): androidx.compose.ui.graphics.Color {
            return when (estado.uppercase()) {
                PENDIENTE -> androidx.compose.ui.graphics.Color(0xFFFFA000) // Naranja
                ACEPTADA -> androidx.compose.ui.graphics.Color(0xFF4CAF50)  // Verde
                RECHAZADA -> androidx.compose.ui.graphics.Color(0xFFD32F2F) // Rojo
                else -> androidx.compose.ui.graphics.Color.Gray
            }
        }
    }

    /**
     * IDs de Roles del Sistema
     * Coinciden con ApplicationConstants.Roles del backend
     */
    object Roles {
        const val ADMIN = 1
        const val PROPIETARIO = 2
        const val ARRIENDATARIO = 3

        fun puedeCrearSolicitud(rolId: Int?): Boolean {
            return rolId == ARRIENDATARIO || rolId == ADMIN
        }

        fun puedeAceptarSolicitud(rolId: Int?): Boolean {
            return rolId == PROPIETARIO || rolId == ADMIN
        }
    }

    /**
     * Límites de Negocio
     * Coinciden con ApplicationConstants.Limites del backend
     */
    object Limites {
        const val MAX_SOLICITUDES_ACTIVAS = 3
        const val TIMEOUT_SECONDS = 5
    }

    /**
     * Mensajes de Error
     * Coinciden con ApplicationConstants.Mensajes del backend
     */
    object Mensajes {
        const val USUARIO_NO_EXISTE = "El usuario con ID %d no existe"
        const val PROPIEDAD_NO_EXISTE = "La propiedad con ID %d no existe"
        const val PROPIEDAD_NO_DISPONIBLE = "La propiedad no está disponible para arriendo"
        const val ROL_INVALIDO_SOLICITUD = "Solo usuarios con rol ARRIENDATARIO pueden crear solicitudes de arriendo"
        const val MAX_SOLICITUDES_ALCANZADO = "El usuario ya tiene el máximo permitido de solicitudes activas (%d)"
        const val SOLICITUD_DUPLICADA = "Ya existe una solicitud pendiente para esta propiedad"
        const val DOCUMENTOS_NO_APROBADOS = "El usuario debe tener todos sus documentos aprobados antes de solicitar un arriendo"
        const val REGISTRO_SOLO_ACEPTADA = "Solo se pueden crear registros para solicitudes aceptadas. Estado actual: %s"
        const val REGISTRO_YA_EXISTE = "Ya existe un registro activo para esta solicitud"
        const val SOLICITUD_NO_ENCONTRADA = "Solicitud no encontrada con ID: %d"
        const val REGISTRO_NO_ENCONTRADO = "Registro no encontrado con ID: %d"
        const val REGISTRO_YA_INACTIVO = "El registro ya está inactivo"
        const val ESTADO_INVALIDO = "Estado inválido: %s"
        const val FECHAS_INVALIDAS = "La fecha de inicio no puede ser posterior a la fecha de fin"
    }
}