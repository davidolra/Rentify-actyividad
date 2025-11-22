package com.example.rentify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.rentify.data.local.entities.SolicitudEntity

/**
 * DAO para operaciones CRUD de solicitudes de arriendo
 */
@Dao
interface SolicitudDao {

    // Insertar una nueva solicitud
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(solicitud: SolicitudEntity): Long

    // Actualizar una solicitud
    @Update
    suspend fun update(solicitud: SolicitudEntity)

    // Buscar por ID
    @Query("SELECT * FROM solicitud WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SolicitudEntity?

    // Listar solicitudes de un usuario
    @Query("SELECT * FROM solicitud WHERE usuarios_id = :usuarioId ORDER BY fsolicitud DESC")
    suspend fun getSolicitudesByUsuario(usuarioId: Long): List<SolicitudEntity>

    // Contar solicitudes activas de un usuario (para validar límite de 3)
    @Query("SELECT COUNT(*) FROM solicitud WHERE usuarios_id = :usuarioId AND estado_id = :estadoActivo")
    suspend fun countSolicitudesActivas(usuarioId: Long, estadoActivo: Long = 1): Int

    // Listar solicitudes de una propiedad
    @Query("SELECT * FROM solicitud WHERE propiedad_id = :propiedadId ORDER BY fsolicitud DESC")
    suspend fun getSolicitudesByPropiedad(propiedadId: Long): List<SolicitudEntity>

    // Listar todas las solicitudes
    @Query("SELECT * FROM solicitud ORDER BY fsolicitud DESC")
    suspend fun getAll(): List<SolicitudEntity>

    // Cambiar estado de solicitud
    @Query("UPDATE solicitud SET estado_id = :nuevoEstadoId WHERE id = :solicitudId")
    suspend fun cambiarEstado(solicitudId: Long, nuevoEstadoId: Long)
}