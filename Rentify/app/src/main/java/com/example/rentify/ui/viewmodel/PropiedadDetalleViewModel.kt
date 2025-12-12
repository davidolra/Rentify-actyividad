package com.example.rentify.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.entities.PropiedadEntity
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.FotoRemoteDTO
import com.example.rentify.data.remote.dto.PropertyRemoteDTO
import com.example.rentify.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para detalle de propiedad con integracion backend
 */
class PropiedadDetalleViewModel(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val remoteRepository: PropertyRemoteRepository? = null
) : ViewModel() {

    companion object {
        private const val TAG = "PropDetalleViewModel"
    }

    private val _propiedad = MutableStateFlow<PropiedadEntity?>(null)
    val propiedad: StateFlow<PropiedadEntity?> = _propiedad.asStateFlow()

    private val _propiedadRemota = MutableStateFlow<PropertyRemoteDTO?>(null)
    val propiedadRemota: StateFlow<PropertyRemoteDTO?> = _propiedadRemota.asStateFlow()

    private val _fotos = MutableStateFlow<List<FotoRemoteDTO>>(emptyList())
    val fotos: StateFlow<List<FotoRemoteDTO>> = _fotos.asStateFlow()

    private val _nombreComuna = MutableStateFlow<String?>(null)
    val nombreComuna: StateFlow<String?> = _nombreComuna.asStateFlow()

    private val _nombreTipo = MutableStateFlow<String?>(null)
    val nombreTipo: StateFlow<String?> = _nombreTipo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    /**
     * Cargar propiedad - intenta desde backend, fallback a local
     */
    fun cargarPropiedad(propiedadId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                Log.d(TAG, "Cargando propiedad: id=$propiedadId")

                if (remoteRepository != null) {
                    when (val result = remoteRepository.obtenerPropiedadPorId(propiedadId, includeDetails = true)) {
                        is ApiResult.Success -> {
                            val dto = result.data
                            Log.d(TAG, "Propiedad cargada desde backend: ${dto.titulo}")

                            _propiedadRemota.value = dto
                            _propiedad.value = mapRemoteToLocal(dto)
                            _fotos.value = dto.fotos ?: emptyList()
                            _nombreComuna.value = dto.comuna?.nombre
                            _nombreTipo.value = dto.tipo?.nombre
                            _isLoading.value = false
                            return@launch
                        }
                        is ApiResult.Error -> {
                            Log.w(TAG, "Error al cargar desde backend: ${result.message}")
                        }
                        is ApiResult.Loading -> {}
                    }
                }

                // Fallback a local
                cargarPropiedadLocal(propiedadId)

            } catch (e: Exception) {
                Log.e(TAG, "Excepcion: ${e.message}", e)
                cargarPropiedadLocal(propiedadId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun cargarPropiedadLocal(propiedadId: Long) {
        Log.d(TAG, "Cargando propiedad desde BD local: id=$propiedadId")

        val propiedadLocal = propiedadDao.getById(propiedadId)
        if (propiedadLocal != null) {
            _propiedad.value = propiedadLocal
            _nombreComuna.value = catalogDao.getComunaById(propiedadLocal.comuna_id)?.nombre
            _nombreTipo.value = catalogDao.getTipoById(propiedadLocal.tipo_id)?.nombre
        } else {
            _errorMsg.value = "Propiedad no encontrada"
        }
    }

    fun clearError() {
        _errorMsg.value = null
    }

    private fun mapRemoteToLocal(dto: PropertyRemoteDTO): PropiedadEntity {
        return PropiedadEntity(
            id = dto.id ?: 0L,
            codigo = dto.codigo,
            titulo = dto.titulo,
            precio_mensual = dto.precioMensual.toInt(),
            divisa = dto.divisa,
            m2 = dto.m2,
            n_habit = dto.nHabit,
            n_banos = dto.nBanos,
            pet_friendly = dto.petFriendly,
            direccion = dto.direccion,
            fcreacion = System.currentTimeMillis(),
            estado_id = 1L,
            tipo_id = dto.tipoId,
            comuna_id = dto.comunaId,
            propietario_id = dto.propietarioId ?: 0L
        )
    }
}