package com.example.rentify.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla DOCUMENTOS
 * Documentos subidos por usuarios para validación
 */
@Entity(
    tableName = "documentos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarios_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EstadoEntity::class,
            parentColumns = ["id"],
            childColumns = ["estado_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TipoDocEntity::class,
            parentColumns = ["id"],
            childColumns = ["tipo_doc_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("usuarios_id"),
        Index("estado_id"),
        Index("tipo_doc_id")
    ]
)
data class DocumentoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val f_subido: Long,               // Fecha de subida (timestamp)
    val nombre: String,               // Nombre del archivo
    val url: String?,                 // URL o path local del documento

    // Relaciones
    val usuarios_id: Long,            // FK a usuarios
    val estado_id: Long,              // FK a estado (Pendiente/Aprobado/Rechazado)
    val tipo_doc_id: Long             // FK a tipo_doc
)