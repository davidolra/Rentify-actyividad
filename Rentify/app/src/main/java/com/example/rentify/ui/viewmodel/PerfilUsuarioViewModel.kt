package com.example.rentify.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.dao.UsuarioDao
import com.example.rentify.data.local.entities.UsuarioEntity
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.DocumentoRemoteDTO
import com.example.rentify.data.remote.dto.TipoDocumentoRemoteDTO
import com.example.rentify.data.remote.dto.toEntity
import com.example.rentify.data.repository.DocumentRepository
import com.example.rentify.data.repository.UserRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PerfilUsuarioViewModel(
    private val usuarioDao: UsuarioDao,
    private val catalogDao: CatalogDao,
    private val solicitudDao: SolicitudDao,
    private val documentRepository: DocumentRepository,
    private val userRemoteRepository: UserRemoteRepository
) : ViewModel() {

    private val _usuario = MutableStateFlow<UsuarioEntity?>(null)
    val usuario: StateFlow<UsuarioEntity?> = _usuario

    private val _nombreRol = MutableStateFlow<String?>(null)
    val nombreRol: StateFlow<String?> = _nombreRol

    private val _cantidadSolicitudes = MutableStateFlow(0)
    val cantidadSolicitudes: StateFlow<Int> = _cantidadSolicitudes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isDocsLoading = MutableStateFlow(false)
    val isDocsLoading: StateFlow<Boolean> = _isDocsLoading.asStateFlow()

    private val _documentTypes = MutableStateFlow<List<TipoDocumentoRemoteDTO>>(emptyList())
    val documentTypes: StateFlow<List<TipoDocumentoRemoteDTO>> = _documentTypes.asStateFlow()

    private val _userDocuments = MutableStateFlow<List<DocumentoRemoteDTO>>(emptyList())
    val userDocuments: StateFlow<List<DocumentoRemoteDTO>> = _userDocuments.asStateFlow()

    private fun formatLongToDateString(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun cargarDatosUsuario(usuarioId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var user = usuarioDao.getById(usuarioId)
                if (user == null) {
                    Log.w("PerfilUsuarioVM", "Usuario no encontrado en BD local, buscando en remoto...")
                    when (val remoteResult = userRemoteRepository.obtenerUsuarioPorId(usuarioId)) {
                        is ApiResult.Success -> {
                            val userFromRemote = remoteResult.data.toEntity()
                            usuarioDao.insert(userFromRemote)
                            user = userFromRemote
                        }
                        is ApiResult.Error -> {
                            Log.e("PerfilUsuarioVM", "Error al obtener usuario remoto: ${remoteResult.message}")
                        }
                        else -> {
                            Log.w("PerfilUsuarioVM", "Resultado de API no manejado: $remoteResult")
                        }
                    }
                }
                _usuario.value = user

                user?.rol_id?.let {
                    val rol = catalogDao.getRolById(it)
                    _nombreRol.value = rol?.nombre
                }

                user?.id?.let {
                    val estadoPendiente = catalogDao.getEstadoByNombre("Pendiente")
                    if (estadoPendiente != null) {
                        val count = solicitudDao.countSolicitudesActivas(it, estadoPendiente.id)
                        _cantidadSolicitudes.value = count
                    }
                }
            } catch (e: Exception) {
                Log.e("PerfilUsuarioVM", "Error al cargar datos del usuario", e)
                _usuario.value = null // Asegurarse de que el usuario sea nulo si hay error
            } finally {
                _isLoading.value = false
            }

            // Cargar documentos solo si el usuario se cargó correctamente
            if (_usuario.value != null) {
                _isDocsLoading.value = true
                try {
                    _documentTypes.value = documentRepository.getDocumentTypes()
                    _userDocuments.value = documentRepository.getDocumentsByUserId(usuarioId)
                } catch (e: Exception) {
                    Log.e("PerfilUsuarioVM", "Error al cargar documentos", e)
                    _documentTypes.value = emptyList()
                    _userDocuments.value = emptyList()
                } finally {
                    _isDocsLoading.value = false
                }
            }
        }
    }

    fun uploadDocument(usuarioId: Long, tipoDocId: Long, fileName: String) {
        viewModelScope.launch {
            _isDocsLoading.value = true
            val document = DocumentoRemoteDTO(
                nombre = fileName,
                usuarioId = usuarioId,
                tipoDocId = tipoDocId,
                estadoId = 1L, // 1: Subido
                fechaSubido = Date()
            )
            val uploadedDocument = documentRepository.uploadDocument(document)
            if (uploadedDocument != null) {
                _userDocuments.value = documentRepository.getDocumentsByUserId(usuarioId)
            } else {
                Log.e("PerfilUsuarioVM", "Error al subir documento")
            }
            _isDocsLoading.value = false
        }
    }

    suspend fun actualizarPerfil(
        nombre: String,
        telefono: String,
        fotoUri: String? = null
    ): Pair<Boolean, String?> {
        val user = _usuario.value ?: return false to "Usuario no cargado"

        val nombres = nombre.split(" ")
        val pnombre = nombres.getOrNull(0) ?: ""
        val snombre = nombres.getOrNull(1) ?: ""
        val papellido = nombres.getOrNull(2) ?: ""

        val updateData: Map<String, Any?> = mapOf(
            "pnombre" to pnombre,
            "snombre" to snombre,
            "papellido" to papellido,
            "ntelefono" to telefono
        )

        val result = userRemoteRepository.actualizarUsuarioParcial(user.id, updateData)

        return if (result is ApiResult.Success) {
            Log.d("PerfilUsuarioVM", "✅ Perfil actualizado en servidor")
            val updatedUser = user.copy(
                pnombre = pnombre,
                snombre = snombre,
                papellido = papellido,
                ntelefono = telefono,
                fotoPerfil = fotoUri
            )
            usuarioDao.update(updatedUser)
            _usuario.value = updatedUser
            true to "Perfil actualizado con éxito"
        } else {
            val errorMsg = (result as? ApiResult.Error)?.message ?: "Error desconocido"
            Log.e("PerfilUsuarioVM", "❌ Error al actualizar perfil: $errorMsg")
            false to errorMsg
        }
    }
}
