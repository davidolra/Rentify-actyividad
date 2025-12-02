package com.example.rentify.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.EstadoRemoteDTO
import com.example.rentify.data.remote.dto.RolRemoteDTO
import com.example.rentify.data.remote.safeApiCall
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ✅ NUEVO: Repositorio con cache para datos maestros (Roles y Estados)
 *
 * Los roles y estados son datos que NO cambian frecuentemente,
 * por lo que podemos cachearlos localmente para:
 * - ✅ Reducir peticiones innecesarias al backend
 * - ✅ Mejorar performance de la app
 * - ✅ Permitir acceso offline a datos maestros
 *
 * ESTRATEGIA DE CACHE:
 * 1. Primera carga: Obtener del servidor y cachear
 * 2. Cargas posteriores: Usar cache si es válido
 * 3. Refrescar cache cada 24 horas o manualmente
 */
class MasterDataRepository(context: Context) {

    private val api = RetrofitClient.userServiceApi
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "rentify_master_data"
        private const val KEY_ROLES = "cached_roles"
        private const val KEY_ESTADOS = "cached_estados"
        private const val KEY_ROLES_TIMESTAMP = "roles_timestamp"
        private const val KEY_ESTADOS_TIMESTAMP = "estados_timestamp"
        private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 horas
    }

    // ==================== ROLES ====================

    /**
     * Obtiene todos los roles (usa cache si está disponible)
     */
    suspend fun obtenerRoles(forceRefresh: Boolean = false): ApiResult<List<RolRemoteDTO>> {
        return withContext(Dispatchers.IO) {
            // Si no se fuerza el refresh, intentar usar cache
            if (!forceRefresh && isCacheValid(KEY_ROLES_TIMESTAMP)) {
                getCachedRoles()?.let { cachedRoles ->
                    return@withContext ApiResult.Success(cachedRoles)
                }
            }

            // Cache no válido o forzar refresh: obtener del servidor
            val result = safeApiCall { api.obtenerTodosRoles() }

            if (result is ApiResult.Success) {
                cacheRoles(result.data)
            }

            result
        }
    }

    /**
     * Obtiene un rol por ID (primero busca en cache)
     */
    suspend fun obtenerRolPorId(id: Long): ApiResult<RolRemoteDTO> {
        return withContext(Dispatchers.IO) {
            // Intentar buscar en cache primero
            getCachedRoles()?.find { it.id == id }?.let { rol ->
                return@withContext ApiResult.Success(rol)
            }

            // No está en cache: obtener del servidor
            safeApiCall { api.obtenerRolPorId(id) }
        }
    }

    /**
     * Obtiene un rol por nombre (primero busca en cache)
     */
    suspend fun obtenerRolPorNombre(nombre: String): ApiResult<RolRemoteDTO> {
        return withContext(Dispatchers.IO) {
            // Intentar buscar en cache primero
            getCachedRoles()?.find {
                it.nombre.equals(nombre, ignoreCase = true)
            }?.let { rol ->
                return@withContext ApiResult.Success(rol)
            }

            // No está en cache: obtener del servidor
            safeApiCall { api.obtenerRolPorNombre(nombre) }
        }
    }

    /**
     * ✅ Helper: Obtiene el ID de un rol por su nombre (útil para registros)
     */
    suspend fun obtenerRolId(nombre: String): Long? {
        return when (val result = obtenerRolPorNombre(nombre)) {
            is ApiResult.Success -> result.data.id
            else -> null
        }
    }

    // ==================== ESTADOS ====================

    /**
     * Obtiene todos los estados (usa cache si está disponible)
     */
    suspend fun obtenerEstados(forceRefresh: Boolean = false): ApiResult<List<EstadoRemoteDTO>> {
        return withContext(Dispatchers.IO) {
            // Si no se fuerza el refresh, intentar usar cache
            if (!forceRefresh && isCacheValid(KEY_ESTADOS_TIMESTAMP)) {
                getCachedEstados()?.let { cachedEstados ->
                    return@withContext ApiResult.Success(cachedEstados)
                }
            }

            // Cache no válido o forzar refresh: obtener del servidor
            val result = safeApiCall { api.obtenerTodosEstados() }

            if (result is ApiResult.Success) {
                cacheEstados(result.data)
            }

            result
        }
    }

    /**
     * Obtiene un estado por ID (primero busca en cache)
     */
    suspend fun obtenerEstadoPorId(id: Long): ApiResult<EstadoRemoteDTO> {
        return withContext(Dispatchers.IO) {
            // Intentar buscar en cache primero
            getCachedEstados()?.find { it.id == id }?.let { estado ->
                return@withContext ApiResult.Success(estado)
            }

            // No está en cache: obtener del servidor
            safeApiCall { api.obtenerEstadoPorId(id) }
        }
    }

    /**
     * Obtiene un estado por nombre (primero busca en cache)
     */
    suspend fun obtenerEstadoPorNombre(nombre: String): ApiResult<EstadoRemoteDTO> {
        return withContext(Dispatchers.IO) {
            // Intentar buscar en cache primero
            getCachedEstados()?.find {
                it.nombre.equals(nombre, ignoreCase = true)
            }?.let { estado ->
                return@withContext ApiResult.Success(estado)
            }

            // No está en cache: obtener del servidor
            safeApiCall { api.obtenerEstadoPorNombre(nombre) }
        }
    }

    /**
     * ✅ Helper: Obtiene el ID de un estado por su nombre
     */
    suspend fun obtenerEstadoId(nombre: String): Long? {
        return when (val result = obtenerEstadoPorNombre(nombre)) {
            is ApiResult.Success -> result.data.id
            else -> null
        }
    }

    // ==================== GESTIÓN DE CACHE ====================

    /**
     * Invalida el cache de roles y estados (fuerza refresh en próxima consulta)
     */
    fun invalidarCache() {
        prefs.edit()
            .remove(KEY_ROLES)
            .remove(KEY_ESTADOS)
            .remove(KEY_ROLES_TIMESTAMP)
            .remove(KEY_ESTADOS_TIMESTAMP)
            .apply()
    }

    /**
     * Verifica si el cache es válido (no ha expirado)
     */
    private fun isCacheValid(timestampKey: String): Boolean {
        val timestamp = prefs.getLong(timestampKey, 0L)
        if (timestamp == 0L) return false

        val elapsed = System.currentTimeMillis() - timestamp
        return elapsed < CACHE_DURATION_MS
    }

    /**
     * Obtiene roles del cache
     */
    private fun getCachedRoles(): List<RolRemoteDTO>? {
        val json = prefs.getString(KEY_ROLES, null) ?: return null
        return try {
            val type = object : TypeToken<List<RolRemoteDTO>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Cachea roles en SharedPreferences
     */
    private fun cacheRoles(roles: List<RolRemoteDTO>) {
        val json = gson.toJson(roles)
        prefs.edit()
            .putString(KEY_ROLES, json)
            .putLong(KEY_ROLES_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    /**
     * Obtiene estados del cache
     */
    private fun getCachedEstados(): List<EstadoRemoteDTO>? {
        val json = prefs.getString(KEY_ESTADOS, null) ?: return null
        return try {
            val type = object : TypeToken<List<EstadoRemoteDTO>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Cachea estados en SharedPreferences
     */
    private fun cacheEstados(estados: List<EstadoRemoteDTO>) {
        val json = gson.toJson(estados)
        prefs.edit()
            .putString(KEY_ESTADOS, json)
            .putLong(KEY_ESTADOS_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    // ==================== HELPERS ÚTILES ====================

    /**
     * ✅ Constantes para IDs de roles (según backend)
     */
    object RolIds {
        const val ADMIN = 1L
        const val PROPIETARIO = 2L
        const val ARRIENDATARIO = 3L
    }

    /**
     * ✅ Constantes para IDs de estados (según backend)
     */
    object EstadoIds {
        const val ACTIVO = 1L
        const val INACTIVO = 2L
        const val SUSPENDIDO = 3L
    }

    /**
     * ✅ Verifica si un usuario es ADMIN
     */
    fun esAdmin(rolId: Long): Boolean = rolId == RolIds.ADMIN

    /**
     * ✅ Verifica si un usuario es PROPIETARIO
     */
    fun esPropietario(rolId: Long): Boolean = rolId == RolIds.PROPIETARIO

    /**
     * ✅ Verifica si un usuario es ARRIENDATARIO
     */
    fun esArriendatario(rolId: Long): Boolean = rolId == RolIds.ARRIENDATARIO

    /**
     * ✅ Verifica si un usuario está ACTIVO
     */
    fun estaActivo(estadoId: Long): Boolean = estadoId == EstadoIds.ACTIVO
}

/**
 * ✅ EJEMPLO DE USO:
 *
 * ```kotlin
 * class RegistroViewModel(context: Context) : ViewModel() {
 *     private val masterDataRepo = MasterDataRepository(context)
 *
 *     fun cargarRoles() {
 *         viewModelScope.launch {
 *             when (val result = masterDataRepo.obtenerRoles()) {
 *                 is ApiResult.Success -> {
 *                     // roles disponibles: result.data
 *                 }
 *                 is ApiResult.Error -> {
 *                     // manejar error
 *                 }
 *             }
 *         }
 *     }
 *
 *     suspend fun obtenerIdRolArriendatario(): Long? {
 *         return masterDataRepo.obtenerRolId("ARRIENDATARIO")
 *     }
 * }
 * ```
 */