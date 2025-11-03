package com.example.rentify.data.local.storage


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Extension para obtener un DataStore desde el Context
 */
val Context.dataStore by preferencesDataStore(name = "rentify_user_prefs")

/**
 * Clase para gestionar preferencias de usuario con DataStore
 * Maneja la sesión del usuario en Rentify
 */
class UserPreferences(private val context: Context) {

    // ========== KEYS ==========
    private val isLoggedInKey = booleanPreferencesKey("is_logged_in")
    private val userIdKey = longPreferencesKey("user_id")
    private val userEmailKey = stringPreferencesKey("user_email")
    private val userNameKey = stringPreferencesKey("user_name")
    private val isDuocVipKey = booleanPreferencesKey("is_duoc_vip")

    // ========== SETTERS ==========

    /**
     * Guarda la sesión completa del usuario
     */
    suspend fun saveUserSession(
        userId: Long,
        email: String,
        name: String,
        isDuocVip: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[isLoggedInKey] = true
            prefs[userIdKey] = userId
            prefs[userEmailKey] = email
            prefs[userNameKey] = name
            prefs[isDuocVipKey] = isDuocVip
        }
    }

    /**
     * Cierra la sesión del usuario (limpia todas las preferencias)
     */
    suspend fun clearUserSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    /**
     * Actualiza solo el estado de login
     */
    suspend fun setLoggedIn(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[isLoggedInKey] = value
        }
    }

    // ========== GETTERS (FLOWS) ==========

    /**
     * Flow que indica si el usuario está logueado
     */
    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[isLoggedInKey] ?: false }

    /**
     * Flow con el ID del usuario actual
     */
    val userId: Flow<Long?> = context.dataStore.data
        .map { prefs -> prefs[userIdKey] }

    /**
     * Flow con el email del usuario actual
     */
    val userEmail: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[userEmailKey] }

    /**
     * Flow con el nombre del usuario actual
     */
    val userName: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[userNameKey] }

    /**
     * Flow que indica si el usuario es VIP de DUOC
     */
    val isDuocVip: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[isDuocVipKey] ?: false }
}