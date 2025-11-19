package com.example.rentify.data.repository

import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.dao.UsuarioDao
import com.example.rentify.data.local.entities.UsuarioEntity
import java.util.UUID

/**
 * Repositorio para operaciones de usuarios en Rentify
 * Maneja registro, login y gestión de usuarios
 */
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
            // Verificar que el usuario esté activo
            val estadoActivo = catalogDao.getEstadoByNombre("Activo")
            if (usuario.estado_id == estadoActivo?.id) {
                Result.success(usuario)
            } else {
                Result.failure(IllegalStateException("Usuario inactivo"))
            }
        } else {
            Result.failure(IllegalArgumentException("Credenciales inválidas"))
        }
    }

    /**
     * ✅ NUEVO: Obtiene el nombre del rol de un usuario
     */
    suspend fun getRoleName(rolId: Long?): String {
        if (rolId == null) return "Sin Rol"

        val rol = catalogDao.getRolById(rolId)
        return rol?.nombre ?: "Sin Rol"
    }

    /**
     * Registro: crea un nuevo usuario con validaciones Rentify
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
        rolSeleccionado: String = "Inquilino"
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

        // Detectar si es DUOC VIP (correo @duoc.cl o @duocuc.cl)
        val isDuocVip = emailLower.endsWith("@duoc.cl") || emailLower.endsWith("@duocuc.cl")

        // Generar código de referido único
        val codigoRef = generarCodigoReferido()

        // Obtener estado Activo y rol Inquilino por defecto
        val estadoActivo = catalogDao.getEstadoByNombre("Activo")
            ?: return Result.failure(IllegalStateException("Estado 'Activo' no encontrado en BD"))

        val rol = when (rolSeleccionado) {
            "Propietario" -> catalogDao.getRolByNombre("Propietario")
            "Inquilino" -> catalogDao.getRolByNombre("Inquilino")
            else -> catalogDao.getRolByNombre("Inquilino")
        } ?: return Result.failure(IllegalStateException("Rol no encontrado en BD"))

        val now = System.currentTimeMillis()

        // Crear nuevo usuario
        val nuevoUsuario = UsuarioEntity(
            pnombre = pnombre.trim(),
            snombre = snombre.trim(),
            papellido = papellido.trim(),
            fnacimiento = fnacimiento,
            email = emailLower,
            rut = rutLimpio,
            ntelefono = ntelefono.trim(),
            clave = password, // En producción: hashear con BCrypt/Argon2
            duoc_vip = isDuocVip,
            puntos = if (isDuocVip) 100 else 0, // Bono de bienvenida para VIP
            codigo_ref = codigoRef,
            fcreacion = now,
            factualizacion = now,
            estado_id = estadoActivo.id,
            rol_id = rol.id
        )

        val id = usuarioDao.insert(nuevoUsuario)
        return Result.success(id)
    }

    /**
     * Genera un código de referido único de 8 caracteres alfanuméricos
     */
    private suspend fun generarCodigoReferido(): String {
        var codigo: String
        var existe: Boolean

        do {
            // Generar código aleatorio
            codigo = UUID.randomUUID().toString()
                .replace("-", "")
                .uppercase()
                .substring(0, 8)

            // Verificar si ya existe
            existe = usuarioDao.getByCodigoRef(codigo) != null
        } while (existe)

        return codigo
    }

    /**
     * Obtiene un usuario por ID
     */
    suspend fun getUsuarioById(id: Long): UsuarioEntity? {
        return usuarioDao.getById(id)
    }

    /**
     * Actualiza los puntos de un usuario
     */
    suspend fun actualizarPuntos(userId: Long, puntos: Int): Result<Unit> {
        return try {
            usuarioDao.actualizarPuntos(userId, puntos)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verifica si un usuario es VIP de DUOC
     */
    suspend fun esUsuarioVip(userId: Long): Boolean {
        val usuario = usuarioDao.getById(userId)
        return usuario?.duoc_vip == true
    }
}