package com.example.rentify.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla REGION
 * Regiones de Chile
 */
@Entity(tableName = "region")
data class RegionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String                // ej: "Región Metropolitana", "Región de Valparaíso"
)