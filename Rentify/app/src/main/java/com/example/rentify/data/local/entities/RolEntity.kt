package com.example.rentify.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla ROL
 * Representa los roles de usuario en Rentify (Administrador, Usuario/Propietario, Inquilino)
 */
@Entity(tableName = "rol")
data class RolEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String                // ej: "Administrador", "Propietario", "Inquilino"
)