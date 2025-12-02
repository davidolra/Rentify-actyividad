package com.example.rentify.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla MAS_ATRIBUTOS (tabla de relación many-to-many)
 * Relación entre propiedades y categorías
 */
@Entity(
    tableName = "mas_atributos",
    foreignKeys = [
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoria_id"],
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
        Index("categoria_id"),
        Index("propiedad_id")
    ]
)
data class MasAtributosEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val categoria_id: Long,           // FK a categoria
    val propiedad_id: Long            // FK a propiedad
)