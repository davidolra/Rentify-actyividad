package com.example.rentify.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.rentify.data.local.entities.PropiedadEntity

/**
 * DAO para operaciones CRUD de propiedades
 * ✅ ACTUALIZADO: Con soporte para propietarios
 */
@Dao
interface PropiedadDao {

    // Insertar una nueva propiedad
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(propiedad: PropiedadEntity): Long

    // Actualizar una propiedad existente
    @Update
    suspend fun update(propiedad: PropiedadEntity)

    // ✅ NUEVO: Eliminar propiedad
    @Delete
    suspend fun delete(propiedad: PropiedadEntity)

    // Buscar por ID
    @Query("SELECT * FROM propiedad WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PropiedadEntity?

    // Buscar por código
    @Query("SELECT * FROM propiedad WHERE codigo = :codigo LIMIT 1")
    suspend fun getByCodigo(codigo: String): PropiedadEntity?

    // ✅ NUEVO: Buscar propiedades de un propietario específico
    @Query("SELECT * FROM propiedad WHERE propietario_id = :propietarioId ORDER BY fcreacion DESC")
    suspend fun getPropiedadesByPropietario(propietarioId: Long): List<PropiedadEntity>

    // ✅ NUEVO: Buscar propiedades activas de un propietario
    @Query("SELECT * FROM propiedad WHERE propietario_id = :propietarioId AND estado_id = :estadoActivo ORDER BY fcreacion DESC")
    suspend fun getPropiedadesActivasByPropietario(propietarioId: Long, estadoActivo: Long = 1): List<PropiedadEntity>

    // Listar todas las propiedades activas
    @Query("SELECT * FROM propiedad WHERE estado_id = :estadoActivo ORDER BY fcreacion DESC")
    suspend fun getPropiedadesActivas(estadoActivo: Long = 1): List<PropiedadEntity>

    // Buscar propiedades por comuna
    @Query("SELECT * FROM propiedad WHERE comuna_id = :comunaId AND estado_id = :estadoActivo")
    suspend fun getPorComuna(comunaId: Long, estadoActivo: Long = 1): List<PropiedadEntity>

    // Buscar propiedades por rango de precio
    @Query("SELECT * FROM propiedad WHERE precio_mensual BETWEEN :minPrecio AND :maxPrecio AND estado_id = :estadoActivo")
    suspend fun getPorRangoPrecio(minPrecio: Int, maxPrecio: Int, estadoActivo: Long = 1): List<PropiedadEntity>

    // Buscar propiedades pet-friendly
    @Query("SELECT * FROM propiedad WHERE pet_friendly = 1 AND estado_id = :estadoActivo")
    suspend fun getPetFriendly(estadoActivo: Long = 1): List<PropiedadEntity>

    // Listar todas las propiedades (ADMIN)
    @Query("SELECT * FROM propiedad ORDER BY fcreacion DESC")
    suspend fun getAll(): List<PropiedadEntity>

    // Contar propiedades activas
    @Query("SELECT COUNT(*) FROM propiedad WHERE estado_id = :estadoActivo")
    suspend fun countActivas(estadoActivo: Long = 1): Int

    // ✅ NUEVO: Contar propiedades de un propietario
    @Query("SELECT COUNT(*) FROM propiedad WHERE propietario_id = :propietarioId")
    suspend fun countByPropietario(propietarioId: Long): Int
}