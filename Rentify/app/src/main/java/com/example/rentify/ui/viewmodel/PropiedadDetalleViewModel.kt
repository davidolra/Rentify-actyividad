package com.example.rentify.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.entities.PropiedadEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de detalle de propiedad
 */
class PropiedadDetalleViewModel(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao
) : ViewModel() {

    private val _propiedad = MutableStateFlow<PropiedadEntity?>(null)
    val propiedad: StateFlow<PropiedadEntity?> = _propiedad

    private val _nombreComuna = MutableStateFlow<String?>(null)
    val nombreComuna: StateFlow<String?> = _nombreComuna

    private val _nombreTipo = MutableStateFlow<String?>(null)
    val nombreTipo: StateFlow<String?> = _nombreTipo

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg

    /**
     * Carga la propiedad por ID
     */
    fun cargarPropiedad(propiedadId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                val prop = propiedadDao.getById(propiedadId)

                if (prop != null) {
                    _propiedad.value = prop

                    // Cargar nombre de comuna
                    val comuna = catalogDao.getComunaById(prop.comuna_id)
                    _nombreComuna.value = comuna?.nombre

                    // Cargar nombre de tipo
                    val tipo = catalogDao.getTipoById(prop.tipo_id)
                    _nombreTipo.value = tipo?.nombre
                } else {
                    _errorMsg.value = "Propiedad no encontrada"
                }
            } catch (e: Exception) {
                _errorMsg.value = "Error al cargar propiedad: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}