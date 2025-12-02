package com.example.rentify.data.local.dao

import androidx.room.*
import com.example.rentify.data.local.entities.DocumentoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para gestión de documentos de usuarios
 */
@Dao
interface DocumentoDao {

    /**
     * Insertar un nuevo documento
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(documento: DocumentoEntity): Long

    /**
     * Actualizar un documento existente
     */
    @Update
    suspend fun update(documento: DocumentoEntity)

    /**
     * Eliminar un documento
     */
    @Delete
    suspend fun delete(documento: DocumentoEntity)

    /**
     * Obtener un documento por ID
     */
    @Query("SELECT * FROM documentos WHERE id = :documentoId")
    suspend fun getById(documentoId: Long): DocumentoEntity?

    /**
     * Obtener todos los documentos de un usuario
     */
    @Query("SELECT * FROM documentos WHERE usuarios_id = :usuarioId ORDER BY f_subido DESC")
    fun getByUsuarioId(usuarioId: Long): Flow<List<DocumentoEntity>>

    /**
     * Obtener documentos por estado
     */
    @Query("SELECT * FROM documentos WHERE estado_id = :estadoId ORDER BY f_subido DESC")
    fun getByEstadoId(estadoId: Long): Flow<List<DocumentoEntity>>

    /**
     * Obtener documentos por tipo
     */
    @Query("SELECT * FROM documentos WHERE tipo_doc_id = :tipoDocId ORDER BY f_subido DESC")
    fun getByTipoDocId(tipoDocId: Long): Flow<List<DocumentoEntity>>

    /**
     * Obtener documentos de un usuario con un tipo específico
     */
    @Query("""
        SELECT * FROM documentos 
        WHERE usuarios_id = :usuarioId 
        AND tipo_doc_id = :tipoDocId 
        ORDER BY f_subido DESC
    """)
    fun getByUsuarioAndTipo(usuarioId: Long, tipoDocId: Long): Flow<List<DocumentoEntity>>

    /**
     * Eliminar todos los documentos de un usuario
     */
    @Query("DELETE FROM documentos WHERE usuarios_id = :usuarioId")
    suspend fun deleteByUsuarioId(usuarioId: Long)

    /**
     * Contar documentos de un usuario
     */
    @Query("SELECT COUNT(*) FROM documentos WHERE usuarios_id = :usuarioId")
    suspend fun countByUsuarioId(usuarioId: Long): Int

    /**
     * Verificar si un usuario tiene un tipo de documento específico
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM documentos 
            WHERE usuarios_id = :usuarioId 
            AND tipo_doc_id = :tipoDocId
        )
    """)
    suspend fun hasDocumentoTipo(usuarioId: Long, tipoDocId: Long): Boolean

    /**
     * Obtener todos los documentos (para administrador)
     */
    @Query("SELECT * FROM documentos ORDER BY f_subido DESC")
    fun getAllDocumentos(): Flow<List<DocumentoEntity>>
}