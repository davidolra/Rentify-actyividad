package com.example.rentify.ui.screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Función para crear archivo temporal
private fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
    return File(storageDir, "PROP_${timeStamp}.jpg")
}

// Función para obtener Uri del archivo
private fun getImageUriForFile(context: Context, file: File): Uri {
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
}

/**
 * Pantalla para que propietarios agreguen nuevas propiedades
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarPropiedadScreen(
    onBack: () -> Unit,
    onPropiedadCreada: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estados del formulario
    var titulo by rememberSaveable { mutableStateOf("") }
    var codigo by rememberSaveable { mutableStateOf("") }
    var precio by rememberSaveable { mutableStateOf("") }
    var m2 by rememberSaveable { mutableStateOf("") }
    var habitaciones by rememberSaveable { mutableStateOf("") }
    var banos by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var petFriendly by rememberSaveable { mutableStateOf(false) }
    var descripcion by rememberSaveable { mutableStateOf("") }

    // Estado de fotos
    var fotosUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para tomar foto
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCaptureUri != null) {
            fotosUris = fotosUris + pendingCaptureUri!!
            Toast.makeText(context, "Foto agregada", Toast.LENGTH_SHORT).show()
        }
        pendingCaptureUri = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Propiedad") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // ========== SECCIÓN: INFORMACIÓN BÁSICA ==========
            Text(
                "Información Básica",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título de la propiedad *") },
                placeholder = { Text("Ej: Dpto 2D/2B - Providencia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it.uppercase() },
                label = { Text("Código único *") },
                placeholder = { Text("Ej: DP001") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección completa *") },
                placeholder = { Text("Ej: Av. Providencia 1234, Providencia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(24.dp))

            // ========== SECCIÓN: CARACTERÍSTICAS ==========
            Text(
                "Características",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = m2,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) m2 = it },
                    label = { Text("m² *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = habitaciones,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) habitaciones = it },
                    label = { Text("Dormitorios *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = banos,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) banos = it },
                    label = { Text("Baños *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = precio,
                onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) precio = it },
                label = { Text("Precio mensual (CLP) *") },
                placeholder = { Text("Ej: 650000") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿Acepta mascotas?", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = petFriendly,
                    onCheckedChange = { petFriendly = it }
                )
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                placeholder = { Text("Descripción detallada de la propiedad...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )
            Spacer(Modifier.height(24.dp))

            // ========== SECCIÓN: FOTOS ==========
            Text(
                "Fotos de la Propiedad",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    val file = createTempImageFile(context)
                    val uri = getImageUriForFile(context, file)
                    pendingCaptureUri = uri
                    takePictureLauncher.launch(uri)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Tomar Foto de la Propiedad")
            }

            if (fotosUris.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Fotos agregadas (${fotosUris.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                fotosUris.forEachIndexed { index, uri ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(uri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto ${index + 1}",
                                modifier = Modifier.size(80.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Foto ${index + 1}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    fotosUris = fotosUris.filterIndexed { i, _ -> i != index }
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ========== BOTÓN GUARDAR ==========
            Button(
                onClick = {
                    // TODO: Validar y guardar en BD
                    if (titulo.isBlank() || codigo.isBlank() || precio.isBlank() ||
                        m2.isBlank() || habitaciones.isBlank() || banos.isBlank() || direccion.isBlank()
                    ) {
                        Toast.makeText(context, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Propiedad agregada exitosamente", Toast.LENGTH_SHORT).show()
                        onPropiedadCreada()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar Propiedad")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}