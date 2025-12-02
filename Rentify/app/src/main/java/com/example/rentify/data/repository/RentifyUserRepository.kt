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
     * ✅ RESTAURADO: Obtiene el nombre del rol de un usuario
     */
    suspend fun getRoleName(rolId: Long?): String {
        if (rolId == null) return "Sin Rol"
        val rol = catalogDao.getRolById(rolId)
        return rol?.nombre ?: "Sin Rol"
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
        rolSeleccionado: String = "Arrendatario"  // ✅ Por defecto Arrendatario
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

        // ✅ OBTENER ROL SEGÚN SELECCIÓN
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
            rol_id = rol.id  // ✅ ASIGNAR ROL
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
     * ✅ NUEVO: Guarda/actualiza usuario remoto en la base de datos local
     * Convierte UsuarioRemoteDTO a UsuarioEntity
     */
    suspend fun syncUsuarioFromRemote(
        usuarioRemoto: com.example.rentify.data.remote.dto.UsuarioRemoteDTO
    ): Result<Long> {
        return try {
            // Verificar si el usuario ya existe en la BD local
            val usuarioExistente = usuarioDao.getById(usuarioRemoto.id ?: 0L)

            // Parsear fecha de nacimiento
            val fnacimiento = parseFechaNacimiento(usuarioRemoto.fnacimiento)

            // Obtener estado activo
            val estadoActivo = catalogDao.getEstadoByNombre("Activo")
                ?: return Result.failure(IllegalStateException("Estado 'Activo' no encontrado"))

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
                direccion = usuarioExistente?.direccion, // Mantener dirección local si existe
                comuna = usuarioExistente?.comuna, // Mantener comuna local si existe
                fotoPerfil = usuarioExistente?.fotoPerfil, // Mantener foto local si existe
                clave = usuarioRemoto.clave,
                duoc_vip = usuarioRemoto.duocVip ?: false,
                puntos = usuarioRemoto.puntos ?: 0,
                codigo_ref = usuarioRemoto.codigoRef ?: generarCodigoReferido(),
                fcreacion = parseFechaCreacion(usuarioRemoto.fcreacion) ?: now,
                factualizacion = now,
                estado_id = usuarioRemoto.estadoId ?: estadoActivo.id,
                rol_id = usuarioRemoto.rolId
            )

            if (usuarioExistente != null) {
                // Actualizar usuario existente
                usuarioDao.update(usuarioLocal)
                Result.success(usuarioLocal.id)
            } else {
                // Insertar nuevo usuario
                val id = usuarioDao.insert(usuarioLocal)
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
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