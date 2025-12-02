package com.example.rentify.ui.screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.ui.viewmodel.PerfilUsuarioViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
    return File(storageDir, "IMG_${timeStamp}.jpg")
}

private fun getImageUriForFile(context: Context, file: File): Uri {
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioScreen(
    vm: PerfilUsuarioViewModel,
    onBack: () -> Unit,
    onVerSolicitudes: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userPrefs = remember { UserPreferences(context) }

    val userId by userPrefs.userId.collectAsStateWithLifecycle(initialValue = null)
    val usuario by vm.usuario.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val isDocsLoading by vm.isDocsLoading.collectAsStateWithLifecycle()
    val documentTypes by vm.documentTypes.collectAsStateWithLifecycle()
    val userDocuments by vm.userDocuments.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        userId?.let { vm.cargarDatosUsuario(it) }
    }

    var nombre by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }

    var profilePhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    var selectedDocType by remember { mutableStateOf<Long?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            profilePhotoUri = pendingCaptureUri
            Toast.makeText(context, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
        } else {
            pendingCaptureUri = null
        }
    }

    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val docId = selectedDocType
            if (docId != null && userId != null) {
                vm.uploadDocument(userId!!, docId, uri.lastPathSegment ?: "document")
                Toast.makeText(context, "Documento subido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error al subir documento", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(usuario) {
        usuario?.let {
            nombre = "${it.pnombre} ${it.snombre} ${it.papellido}"
            telefono = it.ntelefono
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                userPrefs.clearUserSession()
                                onLogout()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading && usuario == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (usuario != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    item {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable {
                                    val file = createTempImageFile(context)
                                    val uri = getImageUriForFile(context, file)
                                    pendingCaptureUri = uri
                                    takePictureLauncher.launch(uri)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profilePhotoUri != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(profilePhotoUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = "Subir foto",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre completo") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text("Teléfono") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    item {
                        Text("Mis Documentos", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                    }

                    if (isDocsLoading) {
                        item {
                           Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (documentTypes.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Text(
                                    text = "No se pudieron cargar los tipos de documento. Asegúrate de que el servicio de documentos esté en ejecución y accesible desde tu red.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(documentTypes) { docType ->
                            val isUploaded = userDocuments.any { it.tipoDocId == docType.id }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        selectedDocType = docType.id
                                        documentLauncher.launch("*/*")
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUploaded) Color.Green.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.UploadFile, contentDescription = null)
                                    Spacer(Modifier.width(12.dp))
                                    Text(docType.nombre, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    val (success, message) = vm.actualizarPerfil(
                                        nombre = nombre,
                                        telefono = telefono,
                                        fotoUri = profilePhotoUri?.toString()
                                    )
                                    val toastMessage = message ?: if (success) "Perfil actualizado con éxito" else "Error al actualizar el perfil"
                                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Guardar cambios")
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            } else {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                     Text("No se pudo cargar el perfil")
                 }
            }
        }
    }
}
