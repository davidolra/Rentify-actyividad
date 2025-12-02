package com.example.rentify.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla COMUNA
 * Comunas asociadas a regiones
 */
@Entity(
    tableName = "comuna",
    foreignKeys = [
        ForeignKey(
            entity = RegionEntity::class,
            parentColumns = ["id"],
            childColumns = ["region_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("region_id")]
)
data class ComunaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String,               // ej: "Santiago", "Providencia", "Viña del Mar"
    val region_id: Long               // FK a region
)