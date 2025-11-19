package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.UsuarioDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.entities.UsuarioEntity
import com.example.rentify.data.local.entities.EstadoSolicitud
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el perfil del usuario
 */
class PerfilUsuarioViewModel(
    private val usuarioDao: UsuarioDao,
    private val catalogDao: CatalogDao,
    private val solicitudDao: SolicitudDao
) : ViewModel() {

    private val _usuario = MutableStateFlow<UsuarioEntity?>(null)
    val usuario: StateFlow<UsuarioEntity?> = _usuario

    private val _nombreRol = MutableStateFlow<String?>(null)
    val nombreRol: StateFlow<String?> = _nombreRol

    private val _cantidadSolicitudes = MutableStateFlow(0)
    val cantidadSolicitudes: StateFlow<Int> = _cantidadSolicitudes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Carga los datos completos del usuario
     */
    fun cargarDatosUsuario(usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Cargar usuario
                val user = usuarioDao.getById(usuarioId)
                _usuario.value = user

                // Cargar nombre del rol
                user?.rol_id?.let { rolId ->
                    val rol = catalogDao.getRolById(rolId)
                    _nombreRol.value = rol?.nombre
                }

                // Contar solicitudes activas
                try {
                    val count = solicitudDao.contarPorEstado(usuarioId, EstadoSolicitud.PENDIENTE)
                    _cantidadSolicitudes.value = count
                } catch (e: Exception) {
                    _cantidadSolicitudes.value = 0
                }

            } catch (e: Exception) {
                // Log error si quieres
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza los datos del perfil del usuario
     */
    fun actualizarPerfil(
        nombre: String,
        telefono: String,
        direccion: String,
        comuna: String,
        fotoUri: String? = null
    ) {
        viewModelScope.launch {
            _usuario.value?.let { user ->
                // Separar nombre completo en partes
                val nombres = nombre.split(" ")
                val pnombre = nombres.getOrNull(0) ?: ""
                val snombre = nombres.getOrNull(1) ?: ""
                val papellido = nombres.getOrNull(2) ?: ""

                // Crear usuario actualizado
                val updatedUser = user.copy(
                    pnombre = pnombre,
                    snombre = snombre,
                    papellido = papellido,
                    ntelefono = telefono,
                    direccion = direccion,
                    comuna = comuna,
                    fotoPerfil = fotoUri
                )

                // Guardar en la base de datos
                usuarioDao.update(updatedUser)

                // Actualizar el StateFlow
                _usuario.value = updatedUser
            }
        }
    }

}
