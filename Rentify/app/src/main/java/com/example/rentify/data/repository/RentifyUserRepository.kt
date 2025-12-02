package com.example.rentify.data.repository

import android.util.Log
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.dao.UsuarioDao
import com.example.rentify.data.local.entities.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class RentifyUserRepository(
    private val usuarioDao: UsuarioDao,
    private val catalogDao: CatalogDao
) {

    /**
     * Login: valida email y contraseña
     */
    suspend fun login(email: String, password: String): Result<UsuarioEntity> {
        val usuario = usuarioDao.getByEmail(email.trim().lowercase())

        return if (usuario != null && usuario.clave == password) {
            val estadoActivo = catalogDao.getEstadoByNombre("Activo")
            if (estadoActivo == null) {
                Result.failure(IllegalStateException("Estado 'Activo' no encontrado en BD"))
            } else if (usuario.estado_id != estadoActivo.id) {
                Result.failure(IllegalStateException("Usuario inactivo"))
            } else {
                Result.success(usuario)
            }
        } else {
            Result.failure(IllegalArgumentException("Credenciales inválidas"))
        }
    }

    /**
     * ✅ CORREGIDO: Obtiene el nombre del rol con manejo robusto de errores
     */
    suspend fun getRoleName(rolId: Long?): String {
        return withContext(Dispatchers.IO) {
            try {
                // Manejar IDs inválidos
                if (rolId == null || rolId == 0L) {
                    Log.w("RentifyUserRepository", "rolId inválido: $rolId")
                    return@withContext "Usuario"
                }

                // Intentar obtener el rol
                val rol = catalogDao.getRolById(rolId)

                if (rol == null) {
                    Log.w("RentifyUserRepository", "Rol no encontrado para ID: $rolId")
                    return@withContext "Usuario"
                }

                Log.d("RentifyUserRepository", "✅ Rol obtenido: ${rol.nombre} (ID: $rolId)")
                rol.nombre

            } catch (e: Exception) {
                Log.e("RentifyUserRepository", "❌ Error al obtener rol: ${e.message}", e)
                "Usuario"  // Fallback seguro
            }
        }
    }

    /**
     * Registro: crea un nuevo usuario con rol seleccionado
     */
    suspend fun register(
        pnombre: String,
        snombre: String,
        papellido: String,
        fnacimiento: Long,
        email: String,
        rut: String,
        ntelefono: String,
        password: String,
        rolSeleccionado: String = "Arrendatario"
    ): Result<Long> {
        val emailLower = email.trim().lowercase()
        val rutLimpio = rut.trim().replace(".", "")

        // Validar duplicados
        if (usuarioDao.getByEmail(emailLower) != null) {
            return Result.failure(IllegalStateException("El email ya está registrado"))
        }

        if (usuarioDao.getByRut(rutLimpio) != null) {
            return Result.failure(IllegalStateException("El RUT ya está registrado"))
        }

        // Detectar si es DUOC VIP
        val isDuocVip = emailLower.endsWith("@duoc.cl") || emailLower.endsWith("@duocuc.cl")

        // Generar código de referido único
        val codigoRef = generarCodigoReferido()

        // Obtener estado Activo
        val estadoActivo = catalogDao.getEstadoByNombre("Activo")
            ?: return Result.failure(IllegalStateException("Estado 'Activo' no encontrado en BD"))

        // Obtener rol según selección
        val rol = when (rolSeleccionado) {
            "Propietario" -> catalogDao.getRolByNombre("Propietario")
            "Arrendatario" -> catalogDao.getRolByNombre("Arrendatario")
            else -> catalogDao.getRolByNombre("Arrendatario")
        } ?: return Result.failure(IllegalStateException("Rol '$rolSeleccionado' no encontrado en BD"))

        val now = System.currentTimeMillis()

        val nuevoUsuario = UsuarioEntity(
            pnombre = pnombre.trim(),
            snombre = snombre.trim(),
            papellido = papellido.trim(),
            fnacimiento = fnacimiento,
            email = emailLower,
            rut = rutLimpio,
            ntelefono = ntelefono.trim(),
            clave = password,
            duoc_vip = isDuocVip,
            puntos = if (isDuocVip) 100 else 0,
            codigo_ref = codigoRef,
            fcreacion = now,
            factualizacion = now,
            estado_id = estadoActivo.id,
            rol_id = rol.id
        )

        val id = usuarioDao.insert(nuevoUsuario)
        return Result.success(id)
    }

    private suspend fun generarCodigoReferido(): String {
        var codigo: String
        var existe: Boolean

        do {
            codigo = UUID.randomUUID().toString().replace("-", "").uppercase().substring(0, 8)
            existe = usuarioDao.getByCodigoRef(codigo) != null
        } while (existe)

        return codigo
    }

    suspend fun getUsuarioById(id: Long): UsuarioEntity? = usuarioDao.getById(id)

    suspend fun actualizarPuntos(userId: Long, puntos: Int): Result<Unit> {
        return try {
            usuarioDao.actualizarPuntos(userId, puntos)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun esUsuarioVip(userId: Long): Boolean {
        val usuario = usuarioDao.getById(userId)
        return usuario?.duoc_vip == true
    }

    /**
     * ✅ MEJORADO: Guarda/actualiza usuario remoto en BD local con mejor manejo de errores
     */
    suspend fun syncUsuarioFromRemote(
        usuarioRemoto: com.example.rentify.data.remote.dto.UsuarioRemoteDTO
    ): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("RentifyUserRepository", "Sincronizando usuario: ${usuarioRemoto.email}")

                val usuarioExistente = usuarioDao.getById(usuarioRemoto.id ?: 0L)
                val fnacimiento = parseFechaNacimiento(usuarioRemoto.fnacimiento)

                // ✅ Verificar que la BD esté inicializada
                val estadoActivo = catalogDao.getEstadoByNombre("Activo")
                if (estadoActivo == null) {
                    Log.e("RentifyUserRepository", "⚠️ Estado 'Activo' no encontrado - BD no inicializada")
                    return@withContext Result.failure(
                        IllegalStateException("Base de datos no inicializada")
                    )
                }

                val now = System.currentTimeMillis()

                val usuarioLocal = UsuarioEntity(
                    id = usuarioRemoto.id ?: 0L,
                    pnombre = usuarioRemoto.pnombre,
                    snombre = usuarioRemoto.snombre,
                    papellido = usuarioRemoto.papellido,
                    fnacimiento = fnacimiento,
                    email = usuarioRemoto.email,
                    rut = usuarioRemoto.rut,
                    ntelefono = usuarioRemoto.ntelefono,
                    direccion = usuarioExistente?.direccion,
                    comuna = usuarioExistente?.comuna,
                    fotoPerfil = usuarioExistente?.fotoPerfil,
                    clave = usuarioRemoto.clave ?: usuarioExistente?.clave ?: "",
                    duoc_vip = usuarioRemoto.duocVip ?: false,
                    puntos = usuarioRemoto.puntos ?: 0,
                    codigo_ref = usuarioRemoto.codigoRef ?: generarCodigoReferido(),
                    fcreacion = parseFechaCreacion(usuarioRemoto.fcreacion) ?: now,
                    factualizacion = now,
                    estado_id = usuarioRemoto.estadoId ?: estadoActivo.id,
                    rol_id = usuarioRemoto.rolId
                )

                val id = if (usuarioExistente != null) {
                    usuarioDao.update(usuarioLocal)
                    Log.d("RentifyUserRepository", "✅ Usuario actualizado: ${usuarioLocal.id}")
                    usuarioLocal.id
                } else {
                    val newId = usuarioDao.insert(usuarioLocal)
                    Log.d("RentifyUserRepository", "✅ Usuario insertado: $newId")
                    newId
                }

                Result.success(id)
            } catch (e: Exception) {
                Log.e("RentifyUserRepository", "❌ Error al sincronizar usuario: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Parsea fecha en formato "yyyy-MM-dd" a timestamp
     */
    private fun parseFechaNacimiento(fechaString: String): Long {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            sdf.parse(fechaString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    /**
     * Parsea fecha de creación (puede ser null)
     */
    private fun parseFechaCreacion(fechaString: String?): Long? {
        if (fechaString == null) return null
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            sdf.parse(fechaString)?.time
        } catch (e: Exception) {
            null
        }
    }
}