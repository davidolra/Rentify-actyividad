package com.example.rentify.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla ESTADO
 * Estados generales del sistema: Activo, Inactivo, Pendiente, Aprobado, Rechazado
 */
@Entity(tableName = "estado")
data class EstadoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String  // ej: "Activo", "Inactivo", "Pendiente", etc.
)