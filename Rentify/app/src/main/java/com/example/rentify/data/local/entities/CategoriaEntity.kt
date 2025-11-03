package com.example.rentify.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla CATEGORIA
 * Categorías/etiquetas de propiedades (Amoblado, Pet-Friendly, Con Terraza, etc.)
 */
@Entity(tableName = "categoria")
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String                // ej: "Amoblado", "Pet-Friendly", "Con Estacionamiento", "Con Terraza"
)