package com.example.rentify.data.local.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "rentify_user_prefs")

class UserPreferences(private val context: Context) {

    // ========== KEYS (SIN ROL) ==========
    private val isLoggedInKey = booleanPreferencesKey("is_logged_in")
    private val userIdKey = longPreferencesKey("user_id")
    private val userEmailKey = stringPreferencesKey("user_email")
    private val userNameKey = stringPreferencesKey("user_name")
    private val isDuocVipKey = booleanPreferencesKey("is_duoc_vip")

    // ========== SETTERS (SIN ROL) ==========
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

    suspend fun clearUserSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun setLoggedIn(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[isLoggedInKey] = value
        }
    }

    // ========== GETTERS (SIN ROL) ==========
    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[isLoggedInKey] ?: false }

    val userId: Flow<Long?> = context.dataStore.data
        .map { prefs -> prefs[userIdKey] }

    val userEmail: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[userEmailKey] }

    val userName: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[userNameKey] }

    val isDuocVip: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[isDuocVipKey] ?: false }
}