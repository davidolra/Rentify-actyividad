package com.example.rentify.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla RESENA
 * Reseñas y calificaciones de propiedades/propietarios/inquilinos
 */
@Entity(
    tableName = "resena",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarios_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TipoResenaEntity::class,
            parentColumns = ["id"],
            childColumns = ["tipo_resena_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("usuarios_id"),
        Index("tipo_resena_id")
    ]
)
data class ResenaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val f_resena: Long,               // Fecha de la reseña (timestamp)
    val comentario: String,           // Texto de la reseña
    val calif: Int,                   // Calificación (1-10)
    val f_baneo: Long?,               // Fecha de baneo (si fue reportada/eliminada)

    // Relaciones
    val usuarios_id: Long,            // FK a usuarios (quien escribe)
    val tipo_resena_id: Long          // FK a tipo_resena
)