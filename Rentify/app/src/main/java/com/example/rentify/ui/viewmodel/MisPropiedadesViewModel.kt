package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.entities.PropiedadEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Data class para mostrar propiedad con información adicional
 */
data class PropiedadConInfo(
    val propiedad: PropiedadEntity,
    val nombreComuna: String?,
    val nombreTipo: String?,
    val nombreEstado: String?
)

/**
 * ViewModel para gestionar las propiedades de un propietario
 */
class MisPropiedadesViewModel(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao
) : ViewModel() {

    private val _propiedades = MutableStateFlow<List<PropiedadConInfo>>(emptyList())
    val propiedades: StateFlow<List<PropiedadConInfo>> = _propiedades

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Carga las propiedades de un propietario específico
     */
    fun cargarPropiedadesPropietario(propietarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val props = propiedadDao.getPropiedadesByPropietario(propietarioId)

                val propsConInfo = props.map { propiedad ->
                    val comuna = catalogDao.getComunaById(propiedad.comuna_id)
                    val tipo = catalogDao.getTipoById(propiedad.tipo_id)
                    val estado = catalogDao.getEstadoById(propiedad.estado_id)

                    PropiedadConInfo(
                        propiedad = propiedad,
                        nombreComuna = comuna?.nombre,
                        nombreTipo = tipo?.nombre,
                        nombreEstado = estado?.nombre
                    )
                }

                _propiedades.value = propsConInfo
            } catch (e: Exception) {
                // Log error
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina una propiedad
     */
    fun eliminarPropiedad(propiedadId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val propiedad = propiedadDao.getById(propiedadId)
                if (propiedad != null) {
                    propiedadDao.delete(propiedad)
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}

/**
 * Factory para MisPropiedadesViewModel
 */
class MisPropiedadesViewModelFactory(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MisPropiedadesViewModel::class.java)) {
            return MisPropiedadesViewModel(propiedadDao, catalogDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}