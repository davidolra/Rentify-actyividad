package com.example.rentify.data.repository

import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.dao.UsuarioDao
import com.example.rentify.data.local.entities.UsuarioEntity
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
     * Registro:crea un nuevo
     */
    suspend fun register(
        pnombre: String,
        snombre: String,
        papellido: String,
        fnacimiento: Long,
        email: String,
        rut: String,
        ntelefono: String,
        password: String
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
            rol_id = null
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
}