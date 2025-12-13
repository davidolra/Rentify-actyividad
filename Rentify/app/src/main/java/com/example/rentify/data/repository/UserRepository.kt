package com.example.rentify.data.repository

import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.api.UserServiceApi
import com.example.rentify.data.remote.dto.UsuarioDTO
import com.example.rentify.data.remote.dto.UsuarioRemoteDTO
import com.example.rentify.data.remote.dto.UsuarioUpdateRemoteDTO
import com.example.rentify.data.remote.safeApiCall

/**
 * Repositorio para gestionar las operaciones de datos de los usuarios.
 * Usa solo el API remoto (sin DAO local)
 */
class UserRepository(private val api: UserServiceApi) {

    private fun UsuarioRemoteDTO.toUsuarioDTO(): UsuarioDTO {
        return UsuarioDTO(
            id = this.id,
            pnombre = this.pnombre,
            snombre = this.snombre,
            papellido = this.papellido,
            email = this.email,
            ntelefono = this.ntelefono,
            rolId = this.rolId?.toInt(),
            estadoId = this.estadoId?.toInt(),
            rol = this.rol?.let { UsuarioDTO.RolInfo(it.id.toInt(), it.nombre) },
            estado = this.estado?.let { UsuarioDTO.EstadoInfo(it.id.toInt(), it.nombre) },
            duocVip = this.duocVip
        )
    }

    private fun UsuarioDTO.toUsuarioUpdateRemoteDTO(): UsuarioUpdateRemoteDTO {
        // CORRECCIÓN AQUÍ: Agregamos '?: ""' para evitar enviar nulos
        return UsuarioUpdateRemoteDTO(
            pnombre = this.pnombre ?: "",
            snombre = this.snombre ?: "",
            papellido = this.papellido ?: "",
            email = this.email ?: "",
            ntelefono = this.ntelefono ?: "",
            rolId = this.rolId?.toLong(),
            estadoId = this.estadoId?.toLong() ?: this.estado?.id?.toLong()
        )
    }

    suspend fun getUsers(): ApiResult<List<UsuarioDTO>> {
        return when (val result = safeApiCall { api.obtenerTodosUsuarios(includeDetails = true) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.map { it.toUsuarioDTO() })
            is ApiResult.Error -> result
            else -> ApiResult.Loading
        }
    }

    suspend fun getUserById(userId: Long): ApiResult<UsuarioDTO> {
        return when (val result = safeApiCall { api.obtenerUsuarioPorId(userId, includeDetails = true) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toUsuarioDTO())
            is ApiResult.Error -> result
            else -> ApiResult.Loading
        }
    }

    suspend fun updateUser(userId: Long, user: UsuarioDTO): ApiResult<UsuarioDTO> {
        val updateDTO = user.toUsuarioUpdateRemoteDTO()
        return when (val result = safeApiCall { api.actualizarUsuario(userId, updateDTO) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toUsuarioDTO())
            is ApiResult.Error -> result
            else -> ApiResult.Loading
        }
    }

    suspend fun deleteUser(userId: Long): ApiResult<Unit> {
        return when (val result = safeApiCall { api.cambiarEstado(userId, estadoId = 2) }) {
            is ApiResult.Success -> ApiResult.Success(Unit)
            is ApiResult.Error -> result
            else -> ApiResult.Loading
        }
    }
}