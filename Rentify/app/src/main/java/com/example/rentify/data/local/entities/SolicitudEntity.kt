package com.example.rentify.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla SOLICITUD
 * Solicitudes de arriendo de usuarios a propiedades
 */
@Entity(
    tableName = "solicitud",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarios_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EstadoEntity::class,
            parentColumns = ["id"],
            childColumns = ["estado_id"],
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
        Index("estado_id"),
        Index("propiedad_id")
    ]
)
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val fsolicitud: Long,             // Fecha de solicitud (timestamp)
    val total: Int,                   // Monto total (canon + garantía + comisión)

    // Relaciones
    val usuarios_id: Long,            // FK a usuarios
    val estado_id: Long,              // FK a estado (Pendiente/Aprobada/Rechazada)
    val propiedad_id: Long            // FK a propiedad
)