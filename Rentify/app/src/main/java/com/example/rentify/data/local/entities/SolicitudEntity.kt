package com.example.rentify.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "solicitud",
    foreignKeys = [
        ForeignKey(
            entity = PropiedadEntity::class,
            parentColumns = ["id"],
            childColumns = ["propiedad_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuario_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("propiedad_id"),
        Index("usuario_id"),
        Index("estado")
    ]
)
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val propiedad_id: Long,
    val usuario_id: Long,
    val estado: String,
    val mensaje: String? = null,
    val respuesta: String? = null,
    val fecha_solicitud: Long,
    val fecha_respuesta: Long? = null
)

object EstadoSolicitud {
    const val PENDIENTE = "PENDIENTE"
    const val APROBADA = "APROBADA"
    const val RECHAZADA = "RECHAZADA"
}