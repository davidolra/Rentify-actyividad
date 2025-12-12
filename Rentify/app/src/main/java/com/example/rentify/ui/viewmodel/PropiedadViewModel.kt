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
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * ViewModel para catalogo de propiedades con ubicacion GPS
 * Carga propiedades desde el backend PropertyService
 */
class PropiedadViewModel(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao,
    private val remoteRepository: PropertyRemoteRepository
) : ViewModel() {

    companion object {
        private const val TAG = "PropiedadViewModel"
    }

    // Estado de las propiedades
    private val _propiedades = MutableStateFlow<List<PropiedadConDistancia>>(emptyList())
    val propiedades: StateFlow<List<PropiedadConDistancia>> = _propiedades

    // Propiedades remotas con toda la info
    private val _propiedadesRemotas = MutableStateFlow<List<PropertyRemoteDTO>>(emptyList())
    val propiedadesRemotas: StateFlow<List<PropertyRemoteDTO>> = _propiedadesRemotas

    // Estado de ubicacion del usuario
    private val _ubicacionUsuario = MutableStateFlow<UbicacionUsuario?>(null)
    val ubicacionUsuario: StateFlow<UbicacionUsuario?> = _ubicacionUsuario

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Estado de permisos
    private val _permisoUbicacion = MutableStateFlow(false)
    val permisoUbicacion: StateFlow<Boolean> = _permisoUbicacion

    // Mensajes de error
    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg

    /**
     * Actualiza la ubicacion del usuario
     */
    fun actualizarUbicacion(latitud: Double, longitud: Double) {
        _ubicacionUsuario.value = UbicacionUsuario(latitud, longitud)
        cargarPropiedadesCercanas()
    }

    /**
     * Actualiza el estado del permiso de ubicacion
     */
    fun setPermisoUbicacion(concedido: Boolean) {
        _permisoUbicacion.value = concedido
    }

    /**
     * Carga propiedades desde el backend remoto
     */
    fun cargarPropiedadesCercanas() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                Log.d(TAG, "Cargando propiedades desde backend...")

                when (val result = remoteRepository.listarTodasPropiedades(includeDetails = true)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Propiedades recibidas: ${result.data.size}")

                        // Guardar propiedades remotas
                        _propiedadesRemotas.value = result.data

                        val ubicacion = _ubicacionUsuario.value

                        val propiedadesConDistancia = result.data.map { dto ->
                            // Mapear a entidad local
                            val entidadLocal = mapearRemotoALocal(dto)

                            // Calcular distancia si hay ubicacion
                            val nombreComuna = dto.comuna?.nombre ?: "Desconocida"
                            val coordenadas = obtenerCoordenadasComuna(nombreComuna)
                            val distancia = if (ubicacion != null) {
                                calcularDistancia(
                                    ubicacion.latitud,
                                    ubicacion.longitud,
                                    coordenadas.first,
                                    coordenadas.second
                                )
                            } else null

                            PropiedadConDistancia(
                                propiedad = entidadLocal,
                                distanciaKm = distancia,
                                nombreComuna = nombreComuna,
                                propiedadRemota = dto
                            )
                        }.sortedBy { it.distanciaKm }

                        _propiedades.value = propiedadesConDistancia
                        Log.d(TAG, "Propiedades procesadas: ${propiedadesConDistancia.size}")
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al cargar desde backend: ${result.message}")
                        _errorMsg.value = result.message
                        // Fallback: Cargar desde BD local
                        cargarPropiedadesLocales()
                    }
                    is ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion al cargar propiedades: ${e.message}", e)
                _errorMsg.value = "Error de conexion: ${e.message}"
                cargarPropiedadesLocales()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fallback para cargar propiedades desde BD local
     */
    private suspend fun cargarPropiedadesLocales() {
        Log.d(TAG, "Cargando propiedades desde BD local (fallback)")

        val propiedades = propiedadDao.getPropiedadesActivas()
        val ubicacion = _ubicacionUsuario.value

        val propiedadesConDistancia = propiedades.map { propiedad ->
            val comuna = catalogDao.getComunaById(propiedad.comuna_id)
            val coordenadas = obtenerCoordenadasComuna(comuna?.nombre ?: "")
            val distancia = if (ubicacion != null) {
                calcularDistancia(
                    ubicacion.latitud,
                    ubicacion.longitud,
                    coordenadas.first,
                    coordenadas.second
                )
            } else null
            PropiedadConDistancia(
                propiedad = propiedad,
                distanciaKm = distancia,
                nombreComuna = comuna?.nombre
            )
        }.sortedBy { it.distanciaKm }

        _propiedades.value = propiedadesConDistancia
        Log.d(TAG, "Propiedades locales cargadas: ${propiedadesConDistancia.size}")
    }

    /**
     * Obtener propiedad remota por ID
     */
    fun obtenerPropiedadRemota(propiedadId: Long): PropertyRemoteDTO? {
        return _propiedadesRemotas.value.find { it.id == propiedadId }
    }

    /**
     * Limpiar error
     */
    fun clearError() {
        _errorMsg.value = null
    }

    /**
     * Mapea DTO remoto a entidad local
     */
    private fun mapearRemotoALocal(dto: PropertyRemoteDTO): PropiedadEntity {
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

    /**
     * Calcula la distancia entre dos puntos GPS (formula de Haversine)
     */
    private fun calcularDistancia(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val radioTierra = 6371.0 // Radio de la Tierra en km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return radioTierra * c
    }

    /**
     * Obtiene coordenadas aproximadas de comunas de Chile
     */
    private fun obtenerCoordenadasComuna(nombreComuna: String): Pair<Double, Double> {
        return when (nombreComuna.lowercase()) {
            "santiago" -> Pair(-33.4489, -70.6693)
            "providencia" -> Pair(-33.4372, -70.6106)
            "nunoa", "nunoa" -> Pair(-33.4569, -70.5989)
            "maipu" -> Pair(-33.5115, -70.7582)
            "las condes" -> Pair(-33.4138, -70.5835)
            "la reina" -> Pair(-33.4436, -70.5385)
            "penalolen" -> Pair(-33.4967, -70.5426)
            "san miguel" -> Pair(-33.4978, -70.6519)
            "vina del mar" -> Pair(-33.0246, -71.5518)
            "valparaiso" -> Pair(-33.0472, -71.6127)
            "concepcion" -> Pair(-36.8270, -73.0503)
            "la florida" -> Pair(-33.5228, -70.5976)
            "puente alto" -> Pair(-33.6115, -70.5758)
            "renca" -> Pair(-33.4050, -70.7231)
            "quilicura" -> Pair(-33.3656, -70.7283)
            "huechuraba" -> Pair(-33.3689, -70.6428)
            "vitacura" -> Pair(-33.3897, -70.5681)
            "lo barnechea" -> Pair(-33.3533, -70.5186)
            else -> Pair(-33.4489, -70.6693) // Default: Santiago Centro
        }
    }
}

/**
 * Data class para ubicacion del usuario
 */
data class UbicacionUsuario(
    val latitud: Double,
    val longitud: Double
)

/**
 * Data class para propiedad con distancia calculada
 */
data class PropiedadConDistancia(
    val propiedad: PropiedadEntity,
    val distanciaKm: Double?,
    val nombreComuna: String?,
    val propiedadRemota: PropertyRemoteDTO? = null
)