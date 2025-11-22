package com.example.rentify.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.entities.SolicitudEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
<<<<<<< HEAD
 * Data class para combinar solicitud con información de la propiedad
 */
data class SolicitudConPropiedad(
    val solicitud: SolicitudEntity,
    val propiedad: PropiedadEntity?,
    val nombreComuna: String?
)

/**
 * ViewModel para gestionar solicitudes de arriendo
 * ✅ CORREGIDO: Ahora carga correctamente las solicitudes
=======
 * ViewModel para gestión de solicitudes
>>>>>>> parent of f51e70f (cambio propiedades)
 */
class SolicitudesViewModel(
    private val solicitudDao: SolicitudDao,
    private val propiedadDao: PropiedadDao,
    private val catalogDao: CatalogDao
) : ViewModel() {

    private val _solicitudes = MutableStateFlow<List<SolicitudConDatos>>(emptyList())
    val solicitudes: StateFlow<List<SolicitudConDatos>> = _solicitudes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg

<<<<<<< HEAD
    private val _cantidadPendientes = MutableStateFlow(0)
    val cantidadPendientes: StateFlow<Int> = _cantidadPendientes.asStateFlow()

    /**
     * ✅ FIX: Cargar solicitudes correctamente
=======
    private val _solicitudCreada = MutableStateFlow(false)
    val solicitudCreada: StateFlow<Boolean> = _solicitudCreada

    /**
     * Carga las solicitudes de un usuario
>>>>>>> parent of f51e70f (cambio propiedades)
     */
    fun cargarSolicitudesUsuario(usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
<<<<<<< HEAD
                // ✅ CAMBIO: Recoger el Flow correctamente
                solicitudDao.getSolicitudesByUsuario(usuarioId).collect { lista ->
                    val solicitudesConPropiedad = lista.map { solicitud ->
                        val propiedad = propiedadDao.getById(solicitud.propiedad_id)
                        val nombreComuna = propiedad?.let {
                            catalogDao.getComunaById(it.comuna_id)?.nombre
                        }
=======
                val listaSolicitudes = solicitudDao.getSolicitudesByUsuario(usuarioId)
>>>>>>> parent of f51e70f (cambio propiedades)

                // Enriquecer con datos de propiedad y estado
                val solicitudesConDatos = listaSolicitudes.map { solicitud ->
                    val propiedad = propiedadDao.getById(solicitud.propiedad_id)
                    val estado = catalogDao.getEstadoById(solicitud.estado_id)

<<<<<<< HEAD
                    _solicitudes.value = solicitudesConPropiedad
                    _isLoading.value = false  // ✅ Mover aquí el isLoading = false
                    actualizarEstadisticas(usuarioId)
=======
                    SolicitudConDatos(
                        solicitud = solicitud,
                        tituloPropiedad = propiedad?.titulo,
                        codigoPropiedad = propiedad?.codigo,
                        nombreEstado = estado?.nombre
                    )
>>>>>>> parent of f51e70f (cambio propiedades)
                }

                _solicitudes.value = solicitudesConDatos
            } catch (e: Exception) {
                _errorMsg.value = "Error al cargar solicitudes: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Crea una nueva solicitud de arriendo
     */
    fun crearSolicitud(
        usuarioId: Long,
        propiedadId: Long,
        mesesArriendo: Int = 1
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null
            _solicitudCreada.value = false

            try {
                // Validar límite de 3 solicitudes activas
                val estadoActivo = catalogDao.getEstadoByNombre("Pendiente")
                    ?: throw IllegalStateException("Estado 'Pendiente' no encontrado")

                val solicitudesActivas = solicitudDao.countSolicitudesActivas(
                    usuarioId,
                    estadoActivo.id
                )

<<<<<<< HEAD
                val id = solicitudDao.insert(solicitud)
                // ✅ Recargar solicitudes después de crear una nueva
                cargarSolicitudes(usuarioId)
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
=======
                if (solicitudesActivas >= 3) {
                    _errorMsg.value = "Ya tienes el máximo de 3 solicitudes activas"
                    return@launch
                }
>>>>>>> parent of f51e70f (cambio propiedades)

                // Obtener datos de la propiedad
                val propiedad = propiedadDao.getById(propiedadId)
                    ?: throw IllegalStateException("Propiedad no encontrada")

                // Calcular total
                val canon = propiedad.precio_mensual * mesesArriendo
                val garantia = propiedad.precio_mensual // 1 mes de garantía
                val comision = (propiedad.precio_mensual * 0.10).toInt() // 10% comisión base
                val total = canon + garantia + comision

                // Crear solicitud
                val nuevaSolicitud = SolicitudEntity(
                    fsolicitud = System.currentTimeMillis(),
                    total = total,
                    usuarios_id = usuarioId,
                    estado_id = estadoActivo.id,
                    propiedad_id = propiedadId
                )

                solicitudDao.insert(nuevaSolicitud)
                _solicitudCreada.value = true

                // Recargar solicitudes
                cargarSolicitudesUsuario(usuarioId)
            } catch (e: Exception) {
                _errorMsg.value = "Error al crear solicitud: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Limpia el estado de solicitud creada
     */
    fun clearSolicitudCreada() {
        _solicitudCreada.value = false
    }
<<<<<<< HEAD
}
=======
}

/**
 * Data class para solicitud con datos enriquecidos
 */
data class SolicitudConDatos(
    val solicitud: SolicitudEntity,
    val tituloPropiedad: String?,
    val codigoPropiedad: String?,
    val nombreEstado: String?
)
>>>>>>> parent of f51e70f (cambio propiedades)
