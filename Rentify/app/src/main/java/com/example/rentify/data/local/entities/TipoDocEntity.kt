package com.example.rentify.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla TIPO_DOC
 * Tipos de documentos que pueden subir los usuarios
 */
@Entity(tableName = "tipo_doc")
data class TipoDocEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String                // ej: "Cédula Identidad", "Liquidación Sueldo", "Certificado Antecedentes"
)