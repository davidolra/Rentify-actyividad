package com.example.rentify.data.model

import android.net.Uri

/**
 * Tipos de documentos que un usuario puede subir durante el registro
 */
enum class TipoDocumentoRegistro(
    val id: Long,
    val displayName: String,
    val descripcion: String,
    val esObligatorio: Boolean = false
) {
    DNI(1, "Cédula de Identidad", "Foto de tu carnet de identidad (ambos lados)", true),
    PASAPORTE(2, "Pasaporte", "Foto de la página principal de tu pasaporte", false),
    LIQUIDACION_SUELDO(3, "Liquidación de Sueldo", "Última liquidación de sueldo", false),
    CERTIFICADO_ANTECEDENTE(4, "Certificado de Antecedentes", "Certificado de antecedentes vigente", false),
    CERTIFICADO_AFP(5, "Certificado AFP", "Certificado de cotizaciones AFP", false),
    CONTRATO_TRABAJO(6, "Contrato de Trabajo", "Copia de tu contrato de trabajo vigente", false);

    companion object {
        fun fromId(id: Long): TipoDocumentoRegistro? = entries.find { it.id == id }
        fun fromName(name: String): TipoDocumentoRegistro? = entries.find { it.name == name }

        // Documentos obligatorios
        fun obligatorios(): List<TipoDocumentoRegistro> = entries.filter { it.esObligatorio }

        // Documentos opcionales
        fun opcionales(): List<TipoDocumentoRegistro> = entries.filter { !it.esObligatorio }
    }
}

/**
 * Representa un documento seleccionado por el usuario durante el registro
 * (almacenado localmente hasta que se integre con el microservicio)
 */
data class DocumentoRegistro(
    val tipo: TipoDocumentoRegistro,
    val uri: Uri,
    val nombreArchivo: String,
    val mimeType: String? = null,
    val tamanoBytes: Long? = null
) {
    /**
     * Verifica si es una imagen
     */
    val esImagen: Boolean
        get() = mimeType?.startsWith("image/") == true

    /**
     * Verifica si es un PDF
     */
    val esPdf: Boolean
        get() = mimeType == "application/pdf"

    /**
     * Tamaño formateado para mostrar
     */
    val tamanoFormateado: String
        get() = when {
            tamanoBytes == null -> "Tamaño desconocido"
            tamanoBytes < 1024 -> "$tamanoBytes B"
            tamanoBytes < 1024 * 1024 -> "${tamanoBytes / 1024} KB"
            else -> String.format("%.1f MB", tamanoBytes / (1024.0 * 1024.0))
        }
}

/**
 * Estado de los documentos durante el registro
 */
data class DocumentosRegistroState(
    val documentos: Map<TipoDocumentoRegistro, DocumentoRegistro> = emptyMap(),
    val documentoEnEdicion: TipoDocumentoRegistro? = null,
    val error: String? = null
) {
    /**
     * Verifica si todos los documentos obligatorios están cargados
     */
    val todosObligatoriosCargados: Boolean
        get() = TipoDocumentoRegistro.obligatorios().all { tipo ->
            documentos.containsKey(tipo)
        }

    /**
     * Lista de documentos obligatorios faltantes
     */
    val obligatoriosFaltantes: List<TipoDocumentoRegistro>
        get() = TipoDocumentoRegistro.obligatorios().filter { tipo ->
            !documentos.containsKey(tipo)
        }

    /**
     * Cantidad de documentos cargados
     */
    val cantidadCargados: Int
        get() = documentos.size

    /**
     * Verifica si un tipo de documento ya fue cargado
     */
    fun estaCargado(tipo: TipoDocumentoRegistro): Boolean = documentos.containsKey(tipo)

    /**
     * Obtiene el documento de un tipo específico
     */
    fun obtener(tipo: TipoDocumentoRegistro): DocumentoRegistro? = documentos[tipo]
}