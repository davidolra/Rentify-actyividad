package com.example.rentify.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.entities.PropiedadEntity
import com.example.rentify.data.local.entities.ComunaEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * ViewModel para gestión de propiedades con ubicación GPS
 */
class PropiedadViewModel(
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao
) : ViewModel() {

    // Estado de las propiedades
    private val _propiedades = MutableStateFlow<List<PropiedadConDistancia>>(emptyList())
    val propiedades: StateFlow<List<PropiedadConDistancia>> = _propiedades

    // Estado de ubicación del usuario
    private val _ubicacionUsuario = MutableStateFlow<UbicacionUsuario?>(null)
    val ubicacionUsuario: StateFlow<UbicacionUsuario?> = _ubicacionUsuario

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Estado de permisos
    private val _permisoUbicacion = MutableStateFlow(false)
    val permisoUbicacion: StateFlow<Boolean> = _permisoUbicacion

    /**
     * Actualiza la ubicación del usuario
     */
    fun actualizarUbicacion(latitud: Double, longitud: Double) {
        _ubicacionUsuario.value = UbicacionUsuario(latitud, longitud)
        cargarPropiedadesCercanas()
    }

    /**
     * Actualiza el estado del permiso de ubicación
     */
    fun setPermisoUbicacion(concedido: Boolean) {
        _permisoUbicacion.value = concedido
    }

    /**
     * Carga todas las propiedades activas y calcula distancias
     */
    fun cargarPropiedadesCercanas() {
        viewModelScope.launch {
            _isLoading.value = true

            // Obtener propiedades activas
            val propiedades = propiedadDao.getPropiedadesActivas()

            // Obtener ubicación del usuario
            val ubicacion = _ubicacionUsuario.value

            // Si hay ubicación, calcular distancias
            val propiedadesConDistancia = if (ubicacion != null) {
                propiedades.map { propiedad ->
                    val comuna = catalogDao.getComunaById(propiedad.comuna_id)
                    val coordenadas = obtenerCoordenadasComuna(comuna?.nombre ?: "")
                    val distancia = calcularDistancia(
                        ubicacion.latitud,
                        ubicacion.longitud,
                        coordenadas.first,
                        coordenadas.second
                    )
                    PropiedadConDistancia(propiedad, distancia, comuna?.nombre)
                }.sortedBy { it.distanciaKm } // Ordenar por cercanía
            } else {
                // Sin ubicación, mostrar todas sin orden específico
                propiedades.map { propiedad ->
                    val comuna = catalogDao.getComunaById(propiedad.comuna_id)
                    PropiedadConDistancia(propiedad, null, comuna?.nombre)
                }
            }

            _propiedades.value = propiedadesConDistancia
            _isLoading.value = false
        }
    }

    /**
     * Calcula la distancia entre dos puntos GPS (fórmula de Haversine)
     * Retorna la distancia en kilómetros
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
     * Obtiene coordenadas aproximadas de comunas de Santiago
     * En producción, estas coordenadas deberían estar en la BD
     */
    private fun obtenerCoordenadasComuna(nombreComuna: String): Pair<Double, Double> {
        return when (nombreComuna.lowercase()) {
            "santiago" -> Pair(-33.4489, -70.6693)
            "providencia" -> Pair(-33.4372, -70.6106)
            "ñuñoa" -> Pair(-33.4569, -70.5989)
            "maipú" -> Pair(-33.5115, -70.7582)
            "las condes" -> Pair(-33.4138, -70.5835)
            "la reina" -> Pair(-33.4436, -70.5385)
            "peñalolén" -> Pair(-33.4967, -70.5426)
            "san miguel" -> Pair(-33.4978, -70.6519)
            "viña del mar" -> Pair(-33.0246, -71.5518)
            else -> Pair(-33.4489, -70.6693) // Default: Santiago Centro
        }
    }
}

/**
 * Data class para ubicación del usuario
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
    val nombreComuna: String?
)