package com.example.rentify.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla USUARIOS
 * Usuario de Rentify con beneficio DUOC y programa de referidos
 */
@Entity(
    tableName = "usuarios",
    foreignKeys = [
        ForeignKey(
            entity = EstadoEntity::class,
            parentColumns = ["id"],
            childColumns = ["estado_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RolEntity::class,
            parentColumns = ["id"],
            childColumns = ["rol_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("estado_id"),
        Index("rol_id"),
        Index(value = ["email"], unique = true),
        Index(value = ["rut"], unique = true),
        Index(value = ["codigo_ref"], unique = true)
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Datos personales
    val pnombre: String,              // Primer nombre
    val snombre: String,              // Segundo nombre
    val papellido: String,            // Apellido
    val fnacimiento: Long,            // Fecha de nacimiento (timestamp)
    val email: String,                // Email (único)
    val rut: String,                  // RUT chileno (único) formato: 12345678-9
    val ntelefono: String,            // Teléfono

    // Seguridad
    val clave: String,                // Contraseña (hasheada en prod)

    // Beneficios Rentify
    val duoc_vip: Boolean,            // ¿Es correo @duoc.cl? (20% descuento)
    val puntos: Int = 0,              // RentifyPoints acumulados
    val codigo_ref: String,           // Código de referido único

    // Auditoría
    val fcreacion: Long,              // Fecha creación (timestamp)
    val factualizacion: Long,         // Última actualización (timestamp)

    // Relaciones
    val estado_id: Long,              // FK a estado (Activo/Inactivo)
    val rol_id: Long?                 // FK a rol (puede ser null)
)