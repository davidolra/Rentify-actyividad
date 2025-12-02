package com.example.rentify.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.rentify.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD de usuarios
 */
@Dao
interface UsuarioDao {

    // Insertar un nuevo usuario
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(usuario: UsuarioEntity): Long

    // Actualizar un usuario existente
    @Update
    suspend fun update(usuario: UsuarioEntity)

    @Delete
    suspend fun delete(usuario: UsuarioEntity)

    // Buscar por email (para login)
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UsuarioEntity?

    // Buscar por RUT
    @Query("SELECT * FROM usuarios WHERE rut = :rut LIMIT 1")
    suspend fun getByRut(rut: String): UsuarioEntity?

    // Buscar por código de referido
    @Query("SELECT * FROM usuarios WHERE codigo_ref = :codigoRef LIMIT 1")
    suspend fun getByCodigoRef(codigoRef: String): UsuarioEntity?

    // Buscar por ID
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UsuarioEntity?

    // Buscar por ID y devolver como Flow
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun getByIdAsFlow(id: Long): Flow<UsuarioEntity?>

    // Listar todos los usuarios
    @Query("SELECT * FROM usuarios ORDER BY id ASC")
    suspend fun getAll(): List<UsuarioEntity>

    // Listar usuarios VIP de DUOC
    @Query("SELECT * FROM usuarios WHERE duoc_vip = 1 AND estado_id = :estadoActivo")
    suspend fun getUsuariosVip(estadoActivo: Long = 1): List<UsuarioEntity>

    // Contar total de usuarios
    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun count(): Int

    // Actualizar puntos de usuario
    @Query("UPDATE usuarios SET puntos = puntos + :puntos WHERE id = :userId")
    suspend fun actualizarPuntos(userId: Long, puntos: Int)
}