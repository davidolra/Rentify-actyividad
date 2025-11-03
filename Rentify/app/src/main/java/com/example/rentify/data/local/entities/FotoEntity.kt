package com.example.rentify.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla FOTOS
 * Fotos asociadas a propiedades
 */
@Entity(
    tableName = "fotos",
    foreignKeys = [
        ForeignKey(
            entity = PropiedadEntity::class,
            parentColumns = ["id"],
            childColumns = ["propiedad_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("propiedad_id")]
)
data class FotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val nombre: String,               // Descripción de la foto
    val url: String,                  // URL o path de la imagen

    // Relación
    val propiedad_id: Long            // FK a propiedad
)