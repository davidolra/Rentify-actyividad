package com.example.rentify.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.*
import com.example.rentify.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel para agregar/editar propiedades
 * Usado por propietarios y admin
 */
class AgregarPropiedadViewModel(
    private val propertyRepository: PropertyRemoteRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AgregarPropiedadVM"
    }

    // Catalogos
    private val _tipos = MutableStateFlow<List<TipoRemoteDTO>>(emptyList())
    val tipos: StateFlow<List<TipoRemoteDTO>> = _tipos.asStateFlow()

    private val _regiones = MutableStateFlow<List<RegionRemoteDTO>>(emptyList())
    val regiones: StateFlow<List<RegionRemoteDTO>> = _regiones.asStateFlow()

    private val _comunas = MutableStateFlow<List<ComunaRemoteDTO>>(emptyList())
    val comunas: StateFlow<List<ComunaRemoteDTO>> = _comunas.asStateFlow()

    private val _comunasFiltradas = MutableStateFlow<List<ComunaRemoteDTO>>(emptyList())
    val comunasFiltradas: StateFlow<List<ComunaRemoteDTO>> = _comunasFiltradas.asStateFlow()

    private val _categorias = MutableStateFlow<List<CategoriaRemoteDTO>>(emptyList())
    val categorias: StateFlow<List<CategoriaRemoteDTO>> = _categorias.asStateFlow()

    // Propiedad creada
    private val _propiedadCreada = MutableStateFlow<PropertyRemoteDTO?>(null)
    val propiedadCreada: StateFlow<PropertyRemoteDTO?> = _propiedadCreada.asStateFlow()

    // Estados de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // Mensajes
    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    private val _successMsg = MutableStateFlow<String?>(null)
    val successMsg: StateFlow<String?> = _successMsg.asStateFlow()

    // Fotos subidas
    private val _fotosSubidas = MutableStateFlow<List<FotoRemoteDTO>>(emptyList())
    val fotosSubidas: StateFlow<List<FotoRemoteDTO>> = _fotosSubidas.asStateFlow()

    private val _fotoSubiendo = MutableStateFlow(false)
    val fotoSubiendo: StateFlow<Boolean> = _fotoSubiendo.asStateFlow()

    init {
        cargarCatalogos()
    }

    /**
     * Cargar todos los catalogos desde el backend
     */
    fun cargarCatalogos() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null

            try {
                Log.d(TAG, "Cargando catalogos...")

                // Cargar tipos
                when (val result = propertyRepository.listarTipos()) {
                    is ApiResult.Success -> {
                        _tipos.value = result.data
                        Log.d(TAG, "Tipos cargados: ${result.data.size}")
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al cargar tipos: ${result.message}")
                    }
                    else -> {}
                }

                // Cargar regiones
                when (val result = propertyRepository.listarRegiones()) {
                    is ApiResult.Success -> {
                        _regiones.value = result.data
                        Log.d(TAG, "Regiones cargadas: ${result.data.size}")
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al cargar regiones: ${result.message}")
                    }
                    else -> {}
                }

                // Cargar todas las comunas
                when (val result = propertyRepository.listarComunas()) {
                    is ApiResult.Success -> {
                        _comunas.value = result.data
                        Log.d(TAG, "Comunas cargadas: ${result.data.size}")
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al cargar comunas: ${result.message}")
                    }
                    else -> {}
                }

                // Cargar categorias
                when (val result = propertyRepository.listarCategorias()) {
                    is ApiResult.Success -> {
                        _categorias.value = result.data
                        Log.d(TAG, "Categorias cargadas: ${result.data.size}")
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al cargar categorias: ${result.message}")
                    }
                    else -> {}
                }

            } catch (e: Exception) {
                Log.e(TAG, "Excepcion al cargar catalogos: ${e.message}", e)
                _errorMsg.value = "Error al cargar datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Filtrar comunas por region seleccionada
     */
    fun cargarComunasPorRegion(regionId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "Filtrando comunas por region: $regionId")

            when (val result = propertyRepository.obtenerComunasPorRegion(regionId)) {
                is ApiResult.Success -> {
                    _comunasFiltradas.value = result.data
                    Log.d(TAG, "Comunas filtradas: ${result.data.size}")
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Error al filtrar comunas: ${result.message}")
                    // Fallback: filtrar localmente
                    _comunasFiltradas.value = _comunas.value.filter { it.regionId == regionId }
                }
                else -> {}
            }
        }
    }

    /**
     * Crear nueva propiedad
     */
    fun crearPropiedad(
        codigo: String,
        titulo: String,
        precioMensual: Double,
        divisa: String,
        m2: Double,
        nHabit: Int,
        nBanos: Int,
        petFriendly: Boolean,
        direccion: String,
        tipoId: Long,
        comunaId: Long,
        propietarioId: Long?
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMsg.value = null

            try {
                Log.d(TAG, "Creando propiedad: codigo=$codigo, titulo=$titulo")

                when (val result = propertyRepository.crearPropiedad(
                    codigo = codigo,
                    titulo = titulo,
                    precioMensual = precioMensual,
                    divisa = divisa,
                    m2 = m2,
                    nHabit = nHabit,
                    nBanos = nBanos,
                    petFriendly = petFriendly,
                    direccion = direccion,
                    tipoId = tipoId,
                    comunaId = comunaId,
                    propietarioId = propietarioId
                )) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Propiedad creada: id=${result.data.id}")
                        _propiedadCreada.value = result.data
                        _successMsg.value = "Propiedad creada exitosamente"
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al crear propiedad: ${result.message}")
                        _errorMsg.value = result.message
                    }
                    is ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion al crear propiedad: ${e.message}", e)
                _errorMsg.value = "Error: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    /**
     * Subir foto a la propiedad
     */
    fun subirFoto(propiedadId: Long, file: File) {
        viewModelScope.launch {
            _fotoSubiendo.value = true

            try {
                Log.d(TAG, "Subiendo foto: propiedad=$propiedadId, archivo=${file.name}")

                when (val result = propertyRepository.subirFoto(propiedadId, file)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Foto subida: id=${result.data.id}")
                        _fotosSubidas.value = _fotosSubidas.value + result.data
                        _successMsg.value = "Foto subida"
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error al subir foto: ${result.message}")
                        _errorMsg.value = result.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepcion al subir foto: ${e.message}", e)
                _errorMsg.value = "Error al subir foto: ${e.message}"
            } finally {
                _fotoSubiendo.value = false
            }
        }
    }

    /**
     * Eliminar foto
     */
    fun eliminarFoto(fotoId: Long) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Eliminando foto: id=$fotoId")

                when (val result = propertyRepository.eliminarFoto(fotoId)) {
                    is ApiResult.Success -> {
                        _fotosSubidas.value = _fotosSubidas.value.filter { it.id != fotoId }
                        _successMsg.value = "Foto eliminada"
                    }
                    is ApiResult.Error -> {
                        _errorMsg.value = result.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _errorMsg.value = "Error: ${e.message}"
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
     * Resetear estado para nueva propiedad
     */
    fun resetearEstado() {
        _propiedadCreada.value = null
        _fotosSubidas.value = emptyList()
        _errorMsg.value = null
        _successMsg.value = null
    }
}