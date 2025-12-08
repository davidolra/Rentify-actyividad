package com.example.rentify.ui.screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    onVerDocumentos: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userPrefs = remember { UserPreferences(context) }

    val userId by userPrefs.userId.collectAsStateWithLifecycle(initialValue = null)
    val usuario by vm.usuario.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        userId?.let { vm.cargarDatosUsuario(it) }
    }

    val scrollState = rememberScrollState()

    // Estados editables - campos separados
    var pnombre by rememberSaveable { mutableStateOf("") }
    var snombre by rememberSaveable { mutableStateOf("") }
    var papellido by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var comuna by rememberSaveable { mutableStateOf("") }

    var profilePhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

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

    // Inicializar campos cuando se carga el usuario
    LaunchedEffect(usuario) {
        usuario?.let { user ->
            pnombre = user.pnombre
            snombre = user.snombre
            papellido = user.papellido
            telefono = user.ntelefono
            direccion = user.direccion ?: ""
            comuna = user.comuna ?: ""
        }
    }

    // Nombre completo para mostrar
    val nombreCompleto = remember(pnombre, snombre, papellido) {
        listOf(pnombre, snombre, papellido)
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
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
                        Icon(Icons.Filled.Logout, contentDescription = "Cerrar sesion", tint = MaterialTheme.colorScheme.error)
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
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (usuario != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // Header con avatar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
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
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        Column {
                            Text(
                                text = nombreCompleto,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = usuario?.email ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Accesos rapidos
                    Text(
                        text = "Accesos rapidos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Boton Mis Documentos
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onVerDocumentos() },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Filled.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Mis Documentos",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Boton Mis Solicitudes
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onVerSolicitudes() },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Filled.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Mis Solicitudes",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Campos editables
                    Text(
                        text = "Informacion personal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pnombre,
                        onValueChange = { pnombre = it },
                        label = { Text("Primer nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = snombre,
                        onValueChange = { snombre = it },
                        label = { Text("Segundo nombre (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = papellido,
                        onValueChange = { papellido = it },
                        label = { Text("Apellido") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Telefono") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Direccion") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) }
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = comuna,
                        onValueChange = { comuna = it },
                        label = { Text("Comuna") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                    )

                    Spacer(Modifier.height(24.dp))

                    // Boton guardar
                    Button(
                        onClick = {
                            scope.launch {
                                vm.actualizarPerfil(
                                    pnombre = pnombre,
                                    snombre = snombre,
                                    papellido = papellido,
                                    telefono = telefono,
                                    direccion = direccion.ifBlank { null },
                                    comuna = comuna.ifBlank { null },
                                    fotoUri = profilePhotoUri?.toString()
                                )
                                Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar cambios")
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}