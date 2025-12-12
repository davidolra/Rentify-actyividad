package com.example.rentify.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.entities.PropiedadEntity
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.PropertyRemoteDTO
import com.example.rentify.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data class para propiedad con info adicional
 */
data class PropiedadConInfo(
    val propiedad: PropiedadEntity,
    val nombreComuna: String?,
    val nombreTipo: String?,
    val propiedadRemota: PropertyRemoteDTO? = null
)

/**
 * ViewModel para ver propiedades del propietario
 */
class MisPropiedadesViewModel(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val propertyRepository: PropertyRemoteRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MisPropiedadesVM"
    }

    // Lista de propiedades
    private val _propiedades = MutableStateFlow<List<PropiedadConInfo>>(emptyList())
    val propiedades: StateFlow<List<PropiedadConInfo>> = _propiedades.asStateFlow()

    // Propiedades remotas
    private val _propiedadesRemotas = MutableStateFlow<List<PropertyRemoteDTO>>(emptyList())
    val propiedadesRemotas: StateFlow<List<PropertyRemoteDTO>> = _propiedadesRemotas.asStateFlow()

    // Estados de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    private val _successMsg = MutableStateFlow<String?>(null)
    val successMsg: StateFlow<String?> = _successMsg.asStateFlow()

    /**
     * Cargar propiedades del propietario
     */
    fun cargarPropiedadesPropietario(propietarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                Log.d(TAG, "Cargando propiedades del propietario: $propietarioId")

                when (val result = propertyRepository.listarTodasPropiedades(includeDetails = true)) {
                    is ApiResult.Success -> {
                        // Filtrar por propietario
                        val propiedadesFiltradas = result.data.filter {
                            it.propietarioId == propietarioId
                        }

                        Log.d(TAG, "Propiedades del propietario: ${propiedadesFiltradas.size}")
                        _propiedadesRemotas.value = propiedadesFiltradas

                        val propiedadesConInfo = propiedadesFiltradas.map { dto ->
                            PropiedadConInfo(
                                propiedad = mapRemoteToLocal(dto),
                                nombreComuna = dto.comuna?.nombre,
                                nombreTipo = dto.tipo?.nombre,
                                propiedadRemota = dto
                            )
                        }

                        _propiedades.value = propiedadesConInfo
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al cargar propiedades: ${result.message}")
                        _errorMsg.value = result.message
                        // Fallback a BD local
                        cargarPropiedadesLocales(propietarioId)
                    }
                    is ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion: ${e.message}", e)
                _errorMsg.value = "Error: ${e.message}"
                cargarPropiedadesLocales(propietarioId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cargar propiedades desde BD local (fallback)
     */
    private suspend fun cargarPropiedadesLocales(propietarioId: Long) {
        Log.d(TAG, "Cargando propiedades locales del propietario: $propietarioId")

        val propiedadesLocales = propiedadDao.getPropiedadesByPropietario(propietarioId)

        val propiedadesConInfo = propiedadesLocales.map { propiedad ->
            PropiedadConInfo(
                propiedad = propiedad,
                nombreComuna = catalogDao.getComunaById(propiedad.comuna_id)?.nombre,
                nombreTipo = catalogDao.getTipoById(propiedad.tipo_id)?.nombre
            )
        }

        _propiedades.value = propiedadesConInfo
    }

    /**
     * Eliminar propiedad
     */
    fun eliminarPropiedad(propiedadId: Long, propietarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                Log.d(TAG, "Eliminando propiedad: $propiedadId")

                when (val result = propertyRepository.eliminarPropiedad(propiedadId)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Propiedad eliminada exitosamente")
                        _successMsg.value = "Propiedad eliminada"
                        // Recargar lista
                        cargarPropiedadesPropietario(propietarioId)
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al eliminar: ${result.message}")
                        _errorMsg.value = result.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion: ${e.message}", e)
                _errorMsg.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Limpiar mensajes
     */
    fun limpiarMensajes() {
        _errorMsg.value = null
        _successMsg.value = null
    }

    /**
     * Mapear DTO remoto a entidad local
     */
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