package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.UsuarioDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

    fun cargarDatosUsuario(usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val user = usuarioDao.getById(usuarioId)
                _usuario.value = user

                user?.rol_id?.let { rolId ->
                    val rol = catalogDao.getRolById(rolId)
                    _nombreRol.value = rol?.nombre
                }

                val estadoPendiente = catalogDao.getEstadoByNombre("Pendiente")
                if (estadoPendiente != null) {
                    val count = solicitudDao.countSolicitudesActivas(usuarioId, estadoPendiente.id)
                    _cantidadSolicitudes.value = count
                }
            } catch (e: Exception) {
                // Log error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun actualizarPerfil(
        pnombre: String,
        snombre: String,
        papellido: String,
        telefono: String,
        direccion: String?,
        comuna: String?,
        fotoUri: String? = null
    ) {
        viewModelScope.launch {
            _usuario.value?.let { user ->
                val updatedUser = user.copy(
                    pnombre = pnombre,
                    snombre = snombre,
                    papellido = papellido,
                    ntelefono = telefono,
                    direccion = direccion,
                    comuna = comuna,
                    fotoPerfil = fotoUri
                )

                usuarioDao.update(updatedUser)
                _usuario.value = updatedUser
            }
        }
    }
}