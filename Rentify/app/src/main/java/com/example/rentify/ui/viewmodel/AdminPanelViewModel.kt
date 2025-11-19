package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.dao.UsuarioDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Data class para estadísticas del sistema
 */
data class EstadisticasSistema(
    val totalUsuarios: Int = 0,
    val totalPropiedades: Int = 0,
    val propiedadesActivas: Int = 0,
    val totalSolicitudes: Int = 0
)

/**
 * ViewModel para el Panel de Administración
 */
class AdminPanelViewModel(
    private val usuarioDao: UsuarioDao,
    private val propiedadDao: PropiedadDao,
    private val solicitudDao: SolicitudDao
) : ViewModel() {

    private val _estadisticas = MutableStateFlow(EstadisticasSistema())
    val estadisticas: StateFlow<EstadisticasSistema> = _estadisticas

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Carga las estadísticas generales del sistema
     */
    fun cargarEstadisticas() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val totalUsuarios = usuarioDao.count()
                val totalPropiedades = propiedadDao.getAll().size
                val propiedadesActivas = propiedadDao.countActivas()

                // Contar todas las solicitudes (necesitamos agregar este método al DAO)
                val solicitudes = solicitudDao.getAllSolicitudes()
                val totalSolicitudes = solicitudes.first().size

                _estadisticas.value = EstadisticasSistema(
                    totalUsuarios = totalUsuarios,
                    totalPropiedades = totalPropiedades,
                    propiedadesActivas = propiedadesActivas,
                    totalSolicitudes = totalSolicitudes
                )
            } catch (e: Exception) {
                // Log error
            } finally {
                _isLoading.value = false
            }
        }
    }
}

/**
 * Factory para AdminPanelViewModel
 */
class AdminPanelViewModelFactory(
    private val usuarioDao: UsuarioDao,
    private val propiedadDao: PropiedadDao,
    private val solicitudDao: SolicitudDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminPanelViewModel::class.java)) {
            return AdminPanelViewModel(usuarioDao, propiedadDao, solicitudDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}