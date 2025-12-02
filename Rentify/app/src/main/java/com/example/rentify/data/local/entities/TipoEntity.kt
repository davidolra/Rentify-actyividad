package com.example.rentify.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla TIPO
 * Tipos de propiedad
 */
@Entity(tableName = "tipo")
data class TipoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String                // ej: "Departamento", "Casa", "Oficina", "Estudio/Loft", "Habitación"
)