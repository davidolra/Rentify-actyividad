package com.example.rentify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rentify.data.local.entities.*

@Dao
interface CatalogDao {

    // ============ TIPO ============
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTipo(tipo: TipoEntity): Long

    @Query("SELECT * FROM tipo WHERE id = :id")
    suspend fun getTipoById(id: Long): TipoEntity?

    @Query("SELECT * FROM tipo")
    suspend fun getAllTipos(): List<TipoEntity>

    @Query("SELECT * FROM tipo WHERE nombre = :nombre LIMIT 1")
    suspend fun getTipoByNombre(nombre: String): TipoEntity?

    // ============ COMUNA ============
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComuna(comuna: ComunaEntity): Long

    @Query("SELECT * FROM comuna WHERE id = :id")
    suspend fun getComunaById(id: Long): ComunaEntity?

    @Query("SELECT * FROM comuna")
    suspend fun getAllComunas(): List<ComunaEntity>

    @Query("SELECT * FROM comuna WHERE region_id = :regionId ORDER BY nombre ASC")
    suspend fun getComunasByRegion(regionId: Long): List<ComunaEntity>

    // ============ REGION ============
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegion(region: RegionEntity): Long

    @Query("SELECT * FROM region WHERE id = :id")
    suspend fun getRegionById(id: Long): RegionEntity?

    @Query("SELECT * FROM region")
    suspend fun getAllRegiones(): List<RegionEntity>

    // ============ ESTADO ============
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstado(estado: EstadoEntity): Long

    @Query("SELECT * FROM estado WHERE id = :id")
    suspend fun getEstadoById(id: Long): EstadoEntity?

    @Query("SELECT * FROM estado")
    suspend fun getAllEstados(): List<EstadoEntity>

    @Query("SELECT * FROM estado WHERE nombre = :nombre LIMIT 1")
    suspend fun getEstadoByNombre(nombre: String): EstadoEntity?

    // ============ CATEGORIA ============
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoria(categoria: CategoriaEntity): Long

    @Query("SELECT * FROM categoria WHERE id = :id")
    suspend fun getCategoriaById(id: Long): CategoriaEntity?

    @Query("SELECT * FROM categoria")
    suspend fun getAllCategorias(): List<CategoriaEntity>

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