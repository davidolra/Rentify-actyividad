package com.example.rentify.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para la tabla PROPIEDAD
 * Inmuebles disponibles para arriendo en Rentify
 */
@Entity(
    tableName = "propiedad",
    foreignKeys = [
        ForeignKey(
            entity = EstadoEntity::class,
            parentColumns = ["id"],
            childColumns = ["estado_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TipoEntity::class,
            parentColumns = ["id"],
            childColumns = ["tipo_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ComunaEntity::class,
            parentColumns = ["id"],
            childColumns = ["comuna_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["propietario_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["codigo"], unique = true),
        Index("estado_id"),
        Index("tipo_id"),
        Index("comuna_id"),
        Index("propietario_id")
    ]
)
data class PropiedadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Identificación
    val codigo: String,               // Código único (ej: DP001, CASA002)
    val titulo: String,               // Título descriptivo

    // Precio
    val precio_mensual: Int,          // Precio mensual
    val divisa: String = "CLP",       // Moneda

    // Características
    val m2: Double,                   // Metros cuadrados
    val n_habit: Int,                 // Número de habitaciones
    val n_banos: Int,                 // Número de baños
    val pet_friendly: Boolean,        // ¿Acepta mascotas?
    val direccion: String,            // Dirección completa


    val descripcion: String? = null,  // Descripción detallada de la propiedad

    // Auditoría
    val fcreacion: Long,              // Fecha creación (timestamp)

    // Relaciones
    val estado_id: Long,              // FK a estado (Activa/Inactiva)
    val tipo_id: Long,                // FK a tipo (Departamento/Casa/etc)
    val comuna_id: Long,              // FK a comuna
    val propietario_id: Long          // ✅ FK al usuario propietario
)