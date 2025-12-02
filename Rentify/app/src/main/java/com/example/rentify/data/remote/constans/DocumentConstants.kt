package com.example.rentify.data.remote.constants

/**
 * Constantes para Document Service
 * ✅ Sincronizadas con DocumentConstants.java del backend
 */
object DocumentConstants {

    /**
     * Estados de Documentos
     * Coinciden con DocumentConstants.EstadoDocumento del backend
     */
    object EstadoDocumento {
        // Nombres de estados
        const val PENDIENTE = "PENDIENTE"
        const val ACEPTADO = "ACEPTADO"
        const val RECHAZADO = "RECHAZADO"
        const val EN_REVISION = "EN_REVISION"

        // IDs de estados (según DataInitializerConfig.java)
        const val PENDIENTE_ID = 1L
        const val ACEPTADO_ID = 2L
        const val RECHAZADO_ID = 3L
        const val EN_REVISION_ID = 4L

        fun esValido(estado: String): Boolean {
            return estado in listOf(PENDIENTE, ACEPTADO, RECHAZADO, EN_REVISION)
        }

        fun getColor(estado: String): androidx.compose.ui.graphics.Color {
            return when (estado.uppercase()) {
                PENDIENTE -> androidx.compose.ui.graphics.Color(0xFFFFA000)    // Naranja
                ACEPTADO -> androidx.compose.ui.graphics.Color(0xFF4CAF50)     // Verde
                RECHAZADO -> androidx.compose.ui.graphics.Color(0xFFD32F2F)    // Rojo
                EN_REVISION -> androidx.compose.ui.graphics.Color(0xFF2196F3)  // Azul
                else -> androidx.compose.ui.graphics.Color.Gray
            }
        }
    }

    /**
     * Tipos de Documentos
     * Coinciden con DocumentConstants.TipoDocumento del backend
     */
    object TipoDocumento {
        // Nombres de tipos
        const val DNI = "DNI"
        const val PASAPORTE = "PASAPORTE"
        const val LIQUIDACION_SUELDO = "LIQUIDACION_SUELDO"
        const val CERTIFICADO_ANTECEDENTES = "CERTIFICADO_ANTECEDENTES"
        const val CERTIFICADO_AFP = "CERTIFICADO_AFP"
        const val CONTRATO_TRABAJO = "CONTRATO_TRABAJO"

        // IDs de tipos (según DataInitializerConfig.java)
        const val DNI_ID = 1L
        const val PASAPORTE_ID = 2L
        const val LIQUIDACION_SUELDO_ID = 3L
        const val CERTIFICADO_ANTECEDENTES_ID = 4L
        const val CERTIFICADO_AFP_ID = 5L
        const val CONTRATO_TRABAJO_ID = 6L

        /**
         * Obtiene un nombre amigable para mostrar en UI
         */
        fun getNombreAmigable(tipo: String): String {
            return when (tipo) {
                DNI -> "DNI / Cédula de Identidad"
                PASAPORTE -> "Pasaporte"
                LIQUIDACION_SUELDO -> "Liquidación de Sueldo"
                CERTIFICADO_ANTECEDENTES -> "Certificado de Antecedentes"
                CERTIFICADO_AFP -> "Certificado AFP"
                CONTRATO_TRABAJO -> "Contrato de Trabajo"
                else -> tipo
            }
        }
    }

    /**
     * Roles del Sistema (para permisos de documentos)
     * Coinciden con DocumentConstants.Roles del backend
     */
    object Roles {
        const val ADMIN = "ADMIN"
        const val PROPIETARIO = "PROPIETARIO"
        const val ARRIENDATARIO = "ARRIENDATARIO"

        /**
         * Verifica si un rol puede subir documentos
         * ✅ ACTUALIZADO: Propietarios también pueden subir documentos
         */
        fun puedeSubirDocumentos(rol: String?): Boolean {
            return rol in listOf(ARRIENDATARIO, PROPIETARIO, ADMIN)
        }

        /**
         * Verifica si un rol puede validar/aprobar documentos
         */
        fun puedeValidarDocumentos(rol: String?): Boolean {
            return rol in listOf(ADMIN, PROPIETARIO)
        }
    }

    /**
     * Límites de Negocio
     * Coinciden con DocumentConstants.Limites del backend
     */
    object Limites {
        const val MAX_NOMBRE_LENGTH = 60
        const val MAX_DOCUMENTOS_POR_USUARIO = 10
        const val TIMEOUT_SECONDS = 5
    }

    /**
     * Mensajes de Error
     * Coinciden con DocumentConstants.Mensajes del backend
     */
    object Mensajes {
        // Documentos
        const val DOCUMENTO_NO_ENCONTRADO = "El documento con ID %d no existe"
        const val DOCUMENTOS_NO_ENCONTRADOS_USUARIO = "No se encontraron documentos para el usuario con ID %d"

        // Estados
        const val ESTADO_NO_ENCONTRADO = "El estado con ID %d no existe"
        const val ESTADO_INVALIDO = "El estado '%s' no es válido"

        // Tipos de documento
        const val TIPO_DOC_NO_ENCONTRADO = "El tipo de documento con ID %d no existe"

        // Usuario
        const val USUARIO_NO_EXISTE = "El usuario con ID %d no existe"
        const val USUARIO_NO_PUEDE_SUBIR = "El usuario con rol '%s' no tiene permisos para subir documentos"

        // Validaciones
        const val MAX_DOCUMENTOS_ALCANZADO = "El usuario ya tiene el máximo de %d documentos permitidos"
        const val NOMBRE_DOCUMENTO_REQUERIDO = "El nombre del documento es obligatorio"

        // Errores de comunicación
        const val ERROR_COMUNICACION_DOCUMENT_SERVICE = "No se pudo verificar el documento. Intente nuevamente."
    }
}