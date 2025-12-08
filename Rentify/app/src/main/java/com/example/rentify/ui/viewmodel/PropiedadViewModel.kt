package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.entities.PropiedadEntity
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PropiedadConDistancia(
    val propiedad: PropiedadEntity,
    val distancia: Double?,
    val nombreComuna: String?,
    val nombreTipo: String?
)

class PropiedadViewModel(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val remoteRepository: PropertyRemoteRepository
) : ViewModel() {

    private val _propiedades = MutableStateFlow<List<PropiedadConDistancia>>(emptyList())
    val propiedades: StateFlow<List<PropiedadConDistancia>> = _propiedades.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun cargarPropiedadesCercanas() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = remoteRepository.listarTodasPropiedades(includeDetails = true)) {
                is ApiResult.Success -> {
                    result.data.forEach { dto ->
                        val entity = PropiedadEntity(
                            id = dto.id ?: 0,
                            codigo = dto.codigo,
                            titulo = dto.titulo,
                            precio_mensual = dto.precioMensual.toInt(),
                            divisa = dto.divisa,
                            m2 = dto.m2,
                            n_habit = dto.nHabit,
                            n_banos = dto.nBanos,
                            pet_friendly = dto.petFriendly,
                            direccion = dto.direccion,
                            descripcion = null,
                            fcreacion = System.currentTimeMillis(),
                            estado_id = 1L,
                            tipo_id = dto.tipoId,
                            comuna_id = dto.comunaId,
                            propietario_id = 1L
                        )
                        propiedadDao.insert(entity)
                    }

                    val propiedadesLocal = propiedadDao.getAll()
                    _propiedades.value = propiedadesLocal.map { propiedad ->
                        PropiedadConDistancia(
                            propiedad = propiedad,
                            distancia = null,
                            nombreComuna = catalogDao.getByIdComuna(propiedad.comuna_id)?.nombre,
                            nombreTipo = catalogDao.getByIdTipo(propiedad.tipo_id)?.nombre
                        )
                    }
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                    val propiedadesLocal = propiedadDao.getAll()
                    _propiedades.value = propiedadesLocal.map { propiedad ->
                        PropiedadConDistancia(
                            propiedad = propiedad,
                            distancia = null,
                            nombreComuna = catalogDao.getByIdComuna(propiedad.comuna_id)?.nombre,
                            nombreTipo = catalogDao.getByIdTipo(propiedad.tipo_id)?.nombre
                        )
                    }
                }
                is ApiResult.Loading -> {}
            }
            _isLoading.value = false
        }
    }
}