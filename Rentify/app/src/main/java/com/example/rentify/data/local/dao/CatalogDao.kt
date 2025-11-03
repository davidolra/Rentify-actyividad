package com.example.rentify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rentify.data.local.entities.*

/**
 * DAO para tablas de catálogo (Región, Comuna, Estado, Tipo, Rol, etc.)
 */
@Dao
interface CatalogDao {

    // ============ REGIÓN ============
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRegion(region: RegionEntity): Long

    @Query("SELECT * FROM region ORDER BY nombre ASC")
    suspend fun getAllRegiones(): List<RegionEntity>

    @Query("SELECT * FROM region WHERE id = :id LIMIT 1")
    suspend fun getRegionById(id: Long): RegionEntity?

    // ============ COMUNA ============
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertComuna(comuna: ComunaEntity): Long

    @Query("SELECT * FROM comuna ORDER BY nombre ASC")
    suspend fun getAllComunas(): List<ComunaEntity>

    @Query("SELECT * FROM comuna WHERE region_id = :regionId ORDER BY nombre ASC")
    suspend fun getComunasByRegion(regionId: Long): List<ComunaEntity>

    @Query("SELECT * FROM comuna WHERE id = :id LIMIT 1")
    suspend fun getComunaById(id: Long): ComunaEntity?

    // ============ ESTADO ============
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEstado(estado: EstadoEntity): Long

    @Query("SELECT * FROM estado ORDER BY id ASC")
    suspend fun getAllEstados(): List<EstadoEntity>

    @Query("SELECT * FROM estado WHERE id = :id LIMIT 1")
    suspend fun getEstadoById(id: Long): EstadoEntity?

    @Query("SELECT * FROM estado WHERE nombre = :nombre LIMIT 1")
    suspend fun getEstadoByNombre(nombre: String): EstadoEntity?

    // ============ TIPO (de propiedad) ============
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTipo(tipo: TipoEntity): Long

    @Query("SELECT * FROM tipo ORDER BY nombre ASC")
    suspend fun getAllTipos(): List<TipoEntity>

    @Query("SELECT * FROM tipo WHERE id = :id LIMIT 1")
    suspend fun getTipoById(id: Long): TipoEntity?

    // ============ CATEGORÍA ============
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategoria(categoria: CategoriaEntity): Long

    @Query("SELECT * FROM categoria ORDER BY nombre ASC")
    suspend fun getAllCategorias(): List<CategoriaEntity>

    @Query("SELECT * FROM categoria WHERE id = :id LIMIT 1")
    suspend fun getCategoriaById(id: Long): CategoriaEntity?

    // ============ ROL ============
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRol(rol: RolEntity): Long

    @Query("SELECT * FROM rol ORDER BY id ASC")
    suspend fun getAllRoles(): List<RolEntity>

    @Query("SELECT * FROM rol WHERE id = :id LIMIT 1")
    suspend fun getRolById(id: Long): RolEntity?

    @Query("SELECT * FROM rol WHERE nombre = :nombre LIMIT 1")
    suspend fun getRolByNombre(nombre: String): RolEntity?

    // ============ TIPO_DOC ============
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTipoDoc(tipoDoc: TipoDocEntity): Long

    @Query("SELECT * FROM tipo_doc ORDER BY nombre ASC")
    suspend fun getAllTiposDocs(): List<TipoDocEntity>

    // ============ TIPO_RESENA ============
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTipoResena(tipoResena: TipoResenaEntity): Long

    @Query("SELECT * FROM tipo_resena ORDER BY nombre ASC")
    suspend fun getAllTiposResenas(): List<TipoResenaEntity>
}