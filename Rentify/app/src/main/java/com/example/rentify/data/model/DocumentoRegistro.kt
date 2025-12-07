package com.example.rentify.data.model

import android.net.Uri

/**
 * Enum con los tipos de documentos disponibles para registro.
 * Los IDs coinciden exactamente con los del backend DocumentService.
 */
enum class TipoDocumentoRegistro(
    val id: Long,
    val displayName: String,
    val descripcion: String,
    val esObligatorio: Boolean
) {
    DNI(
        id = 1L,
        displayName = "Cédula de Identidad",
        descripcion = "Foto de tu carnet de identidad (ambos lados)",
        esObligatorio = true
    ),
    PASAPORTE(
        id = 2L,
        displayName = "Pasaporte",
        descripcion = "Foto de la página principal de tu pasaporte",
        esObligatorio = false
    ),
    LIQUIDACION_SUELDO(
        id = 3L,
        displayName = "Liquidación de Sueldo",
        descripcion = "Última liquidación de sueldo",
        esObligatorio = false
    ),
    CERTIFICADO_ANTECEDENTES(
        id = 4L,
        displayName = "Certificado de Antecedentes",
        descripcion = "Certificado de antecedentes vigente",
        esObligatorio = false
    ),
    CERTIFICADO_AFP(
        id = 5L,
        displayName = "Certificado AFP",
        descripcion = "Certificado de cotizaciones AFP",
        esObligatorio = false
    ),
    CONTRATO_TRABAJO(
        id = 6L,
        displayName = "Contrato de Trabajo",
        descripcion = "Copia de tu contrato de trabajo vigente",
        esObligatorio = false
    );

    companion object {
        fun fromId(id: Long): TipoDocumentoRegistro? = entries.find { it.id == id }

        val obligatorios: List<TipoDocumentoRegistro>
            get() = entries.filter { it.esObligatorio }

        val opcionales: List<TipoDocumentoRegistro>
            get() = entries.filter { !it.esObligatorio }
    }
}

/**
 * Representa un documento seleccionado por el usuario durante el registro.
 * Almacena la URI local del archivo antes de subirlo al servidor.
 */
data class DocumentoRegistro(
    val tipo: TipoDocumentoRegistro,
    val uri: Uri,
    val nombreArchivo: String,
    val mimeType: String? = null,
    val tamanoBytes: Long? = null
)

/**
 * Estado de los documentos durante el proceso de registro.
 */
data class DocumentosRegistroState(
    val documentos: Map<TipoDocumentoRegistro, DocumentoRegistro> = emptyMap(),
    val documentoEnEdicion: TipoDocumentoRegistro? = null,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val error: String? = null
) {
    /**
     * Verifica si todos los documentos obligatorios están cargados.
     */
    val todosObligatoriosCargados: Boolean
        get() = TipoDocumentoRegistro.obligatorios.all { tipo ->
            documentos.containsKey(tipo)
        }

    /**
     * Lista de documentos obligatorios que faltan por cargar.
     */
    val obligatoriosFaltantes: List<TipoDocumentoRegistro>
        get() = TipoDocumentoRegistro.obligatorios.filter { tipo ->
            !documentos.containsKey(tipo)
        }

    /**
     * Cantidad total de documentos cargados.
     */
    val cantidadCargados: Int
        get() = documentos.size

    /**
     * Verifica si un tipo específico de documento está cargado.
     */
    fun estaDocumentoCargado(tipo: TipoDocumentoRegistro): Boolean = documentos.containsKey(tipo)

    /**
     * Obtiene el documento de un tipo específico si existe.
     */
    fun obtenerDocumento(tipo: TipoDocumentoRegistro): DocumentoRegistro? = documentos[tipo]
}

/**
 * Estados posibles de un documento en el backend.
 * Coincide con los estados del DocumentService.
 */
enum class EstadoDocumento(val id: Long, val nombre: String) {
    PENDIENTE(1L, "PENDIENTE"),
    ACEPTADO(2L, "ACEPTADO"),
    RECHAZADO(3L, "RECHAZADO"),
    EN_REVISION(4L, "EN_REVISION");

    companion object {
        fun fromId(id: Long): EstadoDocumento? = entries.find { it.id == id }
        fun fromNombre(nombre: String): EstadoDocumento? = entries.find { it.nombre == nombre }
    }
}