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
}