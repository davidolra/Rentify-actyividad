package com.example.rentify.data.local.dao

import androidx.room.*
import com.example.rentify.data.local.entities.SolicitudEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(solicitud: SolicitudEntity): Long

    @Update
    suspend fun update(solicitud: SolicitudEntity)

    @Delete
    suspend fun delete(solicitud: SolicitudEntity)

    @Query("SELECT * FROM solicitud WHERE id = :id")
    suspend fun getById(id: Long): SolicitudEntity?

    @Query("""
        SELECT * FROM solicitud 
        WHERE usuario_id = :usuarioId 
        ORDER BY fecha_solicitud DESC
    """)
    fun getSolicitudesByUsuario(usuarioId: Long): Flow<List<SolicitudEntity>>

    @Query("""
        SELECT * FROM solicitud 
        WHERE propiedad_id = :propiedadId 
        ORDER BY fecha_solicitud DESC
    """)
    fun getSolicitudesByPropiedad(propiedadId: Long): Flow<List<SolicitudEntity>>

    @Query("""
        SELECT * FROM solicitud 
        WHERE usuario_id = :usuarioId 
        AND estado = :estado 
        ORDER BY fecha_solicitud DESC
    """)
    fun getSolicitudesByEstado(usuarioId: Long, estado: String): Flow<List<SolicitudEntity>>

    @Query("""
        SELECT COUNT(*) FROM solicitud 
        WHERE usuario_id = :usuarioId 
        AND propiedad_id = :propiedadId 
        AND estado = 'PENDIENTE'
    """)
    suspend fun existeSolicitudPendiente(usuarioId: Long, propiedadId: Long): Int

    @Query("""
        SELECT COUNT(*) FROM solicitud 
        WHERE usuario_id = :usuarioId 
        AND estado = :estado
    """)
    suspend fun contarPorEstado(usuarioId: Long, estado: String): Int

    @Query("SELECT * FROM solicitud ORDER BY fecha_solicitud DESC")
    fun getAllSolicitudes(): Flow<List<SolicitudEntity>>
}