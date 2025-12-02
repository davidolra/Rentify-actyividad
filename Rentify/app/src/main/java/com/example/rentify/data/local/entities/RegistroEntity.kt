package com.example.rentify.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla REGISTRO
 * Relación entre usuarios y propiedades (favoritos, vistas, interacciones)
 */
@Entity(
    tableName = "registro",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarios_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PropiedadEntity::class,
            parentColumns = ["id"],
            childColumns = ["propiedad_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("usuarios_id"),
        Index("propiedad_id")
    ]
)
data class RegistroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val fregistro: Long,              // Fecha de registro (timestamp)

    // Relaciones
    val usuarios_id: Long,            // FK a usuarios
    val propiedad_id: Long            // FK a propiedad
)