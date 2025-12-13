package com.example.rentify.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.entities.PropiedadEntity
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * Clase para propiedad con distancia y datos adicionales
 */
data class PropiedadConDistancia(
    val propiedad: PropiedadEntity,
    val distanciaKm: Double? = null,
    val nombreComuna: String? = null,
    val nombreTipo: String? = null
)

/**
 * ViewModel para gestion de propiedades
 */
class PropiedadViewModel(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val remoteRepository: PropertyRemoteRepository
) : ViewModel() {

    companion object {
        private const val TAG = "PropiedadViewModel"
    }

    private val _propiedades = MutableStateFlow<List<PropiedadConDistancia>>(emptyList())
    val propiedades: StateFlow<List<PropiedadConDistancia>> = _propiedades.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    private val _permisoUbicacion = MutableStateFlow(false)
    val permisoUbicacion: StateFlow<Boolean> = _permisoUbicacion.asStateFlow()

    private val _ubicacionUsuario = MutableStateFlow<Pair<Double, Double>?>(null)
    val ubicacionUsuario: StateFlow<Pair<Double, Double>?> = _ubicacionUsuario.asStateFlow()

    fun setPermisoUbicacion(concedido: Boolean) {
        _permisoUbicacion.value = concedido
    }

    fun actualizarUbicacion(lat: Double, lng: Double) {
        _ubicacionUsuario.value = Pair(lat, lng)
        cargarPropiedadesCercanas()
    }

    fun clearError() {
        _errorMsg.value = null
    }

    /**
     * Cargar propiedades cercanas desde el backend
     */
    fun cargarPropiedadesCercanas() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                when (val result = remoteRepository.listarTodasPropiedades(includeDetails = true)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Propiedades cargadas: ${result.data.size}")

                        // Guardar en BD local
                        withContext(Dispatchers.IO) {
                            result.data.forEach { dto ->
                                val entity = PropiedadEntity(
                                    id = dto.id ?: 0,
                                    codigo = dto.codigo ?: "",
                                    titulo = dto.titulo ?: "",
                                    precio_mensual = dto.precioMensual?.toInt() ?: 0,
                                    divisa = dto.divisa ?: "CLP",
                                    m2 = dto.m2 ?: 0.0,
                                    n_habit = dto.nHabit ?: 0,
                                    n_banos = dto.nBanos ?: 0,
                                    pet_friendly = dto.petFriendly ?: false,
                                    direccion = dto.direccion ?: "",
                                    descripcion = null,
                                    fcreacion = System.currentTimeMillis(),
                                    estado_id = 1L,
                                    tipo_id = dto.tipoId ?: 1L,
                                    comuna_id = dto.comunaId ?: 1L,
                                    propietario_id = dto.propietarioId ?: 1L
                                )
                                propiedadDao.insert(entity)
                            }
                        }

                        cargarPropiedadesLocales()
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error: ${result.message}")
                        _errorMsg.value = result.message
                        cargarPropiedadesLocales()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion: ${e.message}", e)
                _errorMsg.value = e.message
                cargarPropiedadesLocales()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun cargarPropiedadesLocales() {
        withContext(Dispatchers.IO) {
            val propiedadesLocal = propiedadDao.getAll()
            val ubicacion = _ubicacionUsuario.value

            _propiedades.value = propiedadesLocal.map { propiedad ->
                val distancia = if (ubicacion != null) {
                    // Calcular distancia usando formula Haversine simplificada
                    val comunaCoords = getComunaCoordinates(propiedad.comuna_id)
                    if (comunaCoords != null) {
                        calcularDistancia(
                            ubicacion.first, ubicacion.second,
                            comunaCoords.first, comunaCoords.second
                        )
                    } else null
                } else null

                PropiedadConDistancia(
                    propiedad = propiedad,
                    distanciaKm = distancia,
                    nombreComuna = catalogDao.getComunaById(propiedad.comuna_id)?.nombre,
                    nombreTipo = catalogDao.getTipoById(propiedad.tipo_id)?.nombre
                )
            }.let { lista ->
                // Ordenar por distancia si hay ubicacion
                if (ubicacion != null) {
                    lista.sortedBy { it.distanciaKm ?: Double.MAX_VALUE }
                } else {
                    lista
                }
            }
        }
    }

    /**
     * Obtener coordenadas aproximadas de una comuna (simplificado)
     */
    private fun getComunaCoordinates(comunaId: Long): Pair<Double, Double>? {
        // Coordenadas aproximadas de algunas comunas de Santiago
        return when (comunaId) {
            1L -> Pair(-33.4489, -70.6693)  // Santiago Centro
            2L -> Pair(-33.4372, -70.6506)  // Providencia
            3L -> Pair(-33.4103, -70.5669)  // Las Condes
            4L -> Pair(-33.3950, -70.5720)  // Vitacura
            5L -> Pair(-33.4269, -70.6165)  // Nunoa
            6L -> Pair(-33.4580, -70.6420)  // San Miguel
            7L -> Pair(-33.5022, -70.7624)  // Maipu
            8L -> Pair(-33.4050, -70.6010)  // La Reina
            9L -> Pair(-33.4833, -70.6167)  // La Florida
            10L -> Pair(-33.5200, -70.5800) // Puente Alto
            else -> null
        }
    }

    /**
     * Calcular distancia entre dos puntos usando formula Haversine
     */
    private fun calcularDistancia(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371.0 // Radio de la Tierra en km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }
}