package com.example.rentify.ui.screen

import android.Manifest // <-- AGREGADO
import android.content.Context
import android.content.pm.PackageManager // <-- AGREGADO
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat // <-- AGREGADO
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.ui.viewmodel.AgregarPropiedadViewModel
import com.example.rentify.ui.viewmodel.AgregarPropiedadViewModelFactory
import java.io.File
import java.io.FileOutputStream

/**
 * Pantalla para agregar nueva propiedad
 * Usado por propietarios y admin
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarPropiedadScreen(
    userPreferences: UserPreferences,
    viewModelFactory: AgregarPropiedadViewModelFactory,
    onNavigateBack: () -> Unit,
    onPropiedadCreada: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: AgregarPropiedadViewModel = viewModel(factory = viewModelFactory)

    // Estados del ViewModel
    val tipos by viewModel.tipos.collectAsStateWithLifecycle()
    val regiones by viewModel.regiones.collectAsStateWithLifecycle()
    val comunasFiltradas by viewModel.comunasFiltradas.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val errorMsg by viewModel.errorMsg.collectAsStateWithLifecycle()
    val successMsg by viewModel.successMsg.collectAsStateWithLifecycle()
    val propiedadCreada by viewModel.propiedadCreada.collectAsStateWithLifecycle()
    val fotosSubidas by viewModel.fotosSubidas.collectAsStateWithLifecycle()
    val fotoSubiendo by viewModel.fotoSubiendo.collectAsStateWithLifecycle()

    // Datos del usuario
    val userId by userPreferences.userId.collectAsStateWithLifecycle(initialValue = null)

    // Estados del formulario
    var codigo by remember { mutableStateOf("") }
    var titulo by remember { mutableStateOf("") }
    var precioMensual by remember { mutableStateOf("") }
    var divisa by remember { mutableStateOf("CLP") }
    var m2 by remember { mutableStateOf("") }
    var nHabit by remember { mutableStateOf("") }
    var nBanos by remember { mutableStateOf("") }
    var petFriendly by remember { mutableStateOf(false) }
    var direccion by remember { mutableStateOf("") }

    // Selecciones de dropdowns
    var tipoSeleccionado by remember { mutableStateOf<Long?>(null) }
    var regionSeleccionada by remember { mutableStateOf<Long?>(null) }
    var comunaSeleccionada by remember { mutableStateOf<Long?>(null) }

    // Estados de dropdowns expandidos
    var tipoExpanded by remember { mutableStateOf(false) }
    var regionExpanded by remember { mutableStateOf(false) }
    var comunaExpanded by remember { mutableStateOf(false) }
    var divisaExpanded by remember { mutableStateOf(false) }

    // Fotos locales pendientes de subir
    var fotosLocales by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Para captura de foto
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    // ====================================================================
    // LANZADORES DE CÁMARA Y PERMISOS (Corregido)

    // 1. Launcher para tomar foto
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCaptureUri != null) {
            fotosLocales = fotosLocales + pendingCaptureUri!!
        }
        pendingCaptureUri = null // Limpiar la URI pendiente después del intento
    }

    // 2. Launcher para solicitar permiso de cámara <-- AGREGADO
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Si el permiso es otorgado, procede a lanzar la cámara
            val file = createTempImageFile(context)
            val uri = getImageUriForFile(context, file)
            pendingCaptureUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }
    // ====================================================================


    // Launcher para seleccionar imagen de galeria
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            fotosLocales = fotosLocales + it
        }
    }

    // Cargar comunas cuando cambia la region
    LaunchedEffect(regionSeleccionada) {
        regionSeleccionada?.let {
            comunaSeleccionada = null
            // Llama a la versión corregida del ViewModel (solo filtro local)
            viewModel.cargarComunasPorRegion(it)
        }
    }

    // Mostrar mensajes
    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarMensajes()
        }
    }

    LaunchedEffect(successMsg) {
        successMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()
        }
    }

    // Cuando se crea la propiedad, subir fotos
    LaunchedEffect(propiedadCreada) {
        propiedadCreada?.let { propiedad ->
            if (fotosLocales.isNotEmpty() && propiedad.id != null) {
                fotosLocales.forEach { uri ->
                    // Aquí se llama a la función uriToFile y luego a subirFoto
                    val file = uriToFile(context, uri)
                    file?.let {
                        viewModel.subirFoto(propiedad.id, it)
                    }
                }
            }
        }
    }

    // Navegar cuando todas las fotos esten subidas
    LaunchedEffect(fotosSubidas, propiedadCreada) {
        if (propiedadCreada != null && fotosSubidas.size >= fotosLocales.size && !fotoSubiendo) {
            propiedadCreada?.id?.let { id ->
                onPropiedadCreada(id)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Propiedad") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Cargando datos...")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Seccion de fotos
                    Text(
                        "Fotos de la propiedad",
                        style = MaterialTheme.typography.titleMedium
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Boton para tomar foto
                        item {
                            Surface(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        // ====================================================================
                                        // LÓGICA DE SOLICITUD DE PERMISO
                                        val permissionCheck = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CAMERA
                                        )
                                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                            // Permiso otorgado: lanzar la cámara directamente
                                            val file = createTempImageFile(context)
                                            val uri = getImageUriForFile(context, file)
                                            pendingCaptureUri = uri
                                            takePictureLauncher.launch(uri)
                                        } else {
                                            // Pedir permiso al usuario
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                        // ====================================================================
                                    },
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.CameraAlt, "Tomar foto")
                                    Text("Camara", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // Boton para galeria
                        item {
                            Surface(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        pickImageLauncher.launch("image/*")
                                    },
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, "Galeria")
                                    Text("Galeria", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // Fotos locales seleccionadas
                        items(fotosLocales) { uri ->
                            Box {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(uri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Foto",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = {
                                        fotosLocales = fotosLocales.filter { it != uri }
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        "Eliminar",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    Divider()

                    // Codigo
                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it.uppercase().take(10) },
                        label = { Text("Codigo *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = { Text("Maximo 10 caracteres, unico") }
                    )

                    // Titulo
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it.take(100) },
                        label = { Text("Titulo *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Direccion
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it.take(200) },
                        label = { Text("Direccion *") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.LocationOn, null) }
                    )

                    // Region y Comuna
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Region
                        ExposedDropdownMenuBox(
                            expanded = regionExpanded,
                            onExpandedChange = { regionExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = regiones.find { it.id == regionSeleccionada }?.nombre ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Region *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(regionExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = regionExpanded,
                                onDismissRequest = { regionExpanded = false }
                            ) {
                                regiones.forEach { region ->
                                    DropdownMenuItem(
                                        text = { Text(region.nombre) },
                                        onClick = {
                                            regionSeleccionada = region.id
                                            regionExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Comuna
                        ExposedDropdownMenuBox(
                            expanded = comunaExpanded,
                            onExpandedChange = { comunaExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = comunasFiltradas.find { it.id == comunaSeleccionada }?.nombre ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Comuna *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(comunaExpanded) },
                                modifier = Modifier.menuAnchor(),
                                enabled = regionSeleccionada != null
                            )
                            ExposedDropdownMenu(
                                expanded = comunaExpanded,
                                onDismissRequest = { comunaExpanded = false }
                            ) {
                                comunasFiltradas.forEach { comuna ->
                                    DropdownMenuItem(
                                        text = { Text(comuna.nombre) },
                                        onClick = {
                                            comunaSeleccionada = comuna.id
                                            comunaExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Tipo
                    ExposedDropdownMenuBox(
                        expanded = tipoExpanded,
                        onExpandedChange = { tipoExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = tipos.find { it.id == tipoSeleccionado }?.nombre ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de propiedad *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tipoExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = tipoExpanded,
                            onDismissRequest = { tipoExpanded = false }
                        ) {
                            tipos.forEach { tipo ->
                                DropdownMenuItem(
                                    text = { Text(tipo.nombre) },
                                    onClick = {
                                        tipoSeleccionado = tipo.id
                                        tipoExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Caracteristicas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = m2,
                            onValueChange = { m2 = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("M2 *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = nHabit,
                            onValueChange = { nHabit = it.filter { c -> c.isDigit() } },
                            label = { Text("Habitaciones *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = nBanos,
                            onValueChange = { nBanos = it.filter { c -> c.isDigit() } },
                            label = { Text("Banos *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    // Precio y Divisa
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = precioMensual,
                            onValueChange = { precioMensual = it.filter { c -> c.isDigit() } },
                            label = { Text("Precio mensual *") },
                            modifier = Modifier.weight(2f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        ExposedDropdownMenuBox(
                            expanded = divisaExpanded,
                            onExpandedChange = { divisaExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = divisa,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Divisa") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(divisaExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = divisaExpanded,
                                onDismissRequest = { divisaExpanded = false }
                            ) {
                                listOf("CLP", "UF", "USD").forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d) },
                                        onClick = {
                                            divisa = d
                                            divisaExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Pet Friendly
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Pets, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Acepta mascotas")
                        Spacer(Modifier.weight(1f))
                        Switch(
                            checked = petFriendly,
                            onCheckedChange = { petFriendly = it }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Boton guardar
                    Button(
                        onClick = {
                            // Validaciones
                            when {
                                codigo.isBlank() -> {
                                    Toast.makeText(context, "Ingresa el codigo", Toast.LENGTH_SHORT).show()
                                }
                                titulo.isBlank() -> {
                                    Toast.makeText(context, "Ingresa el titulo", Toast.LENGTH_SHORT).show()
                                }
                                direccion.isBlank() -> {
                                    Toast.makeText(context, "Ingresa la direccion", Toast.LENGTH_SHORT).show()
                                }
                                tipoSeleccionado == null -> {
                                    Toast.makeText(context, "Selecciona el tipo", Toast.LENGTH_SHORT).show()
                                }
                                comunaSeleccionada == null -> {
                                    Toast.makeText(context, "Selecciona la comuna", Toast.LENGTH_SHORT).show()
                                }
                                m2.isBlank() || m2.toDoubleOrNull() == null -> {
                                    Toast.makeText(context, "Ingresa los metros cuadrados", Toast.LENGTH_SHORT).show()
                                }
                                nHabit.isBlank() || nHabit.toIntOrNull() == null -> {
                                    Toast.makeText(context, "Ingresa el numero de habitaciones", Toast.LENGTH_SHORT).show()
                                }
                                nBanos.isBlank() || nBanos.toIntOrNull() == null -> {
                                    Toast.makeText(context, "Ingresa el numero de banos", Toast.LENGTH_SHORT).show()
                                }
                                precioMensual.isBlank() || precioMensual.toDoubleOrNull() == null -> {
                                    Toast.makeText(context, "Ingresa el precio mensual", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    viewModel.crearPropiedad(
                                        codigo = codigo,
                                        titulo = titulo,
                                        precioMensual = precioMensual.toDouble(),
                                        divisa = divisa,
                                        m2 = m2.toDouble(),
                                        nHabit = nHabit.toInt(),
                                        nBanos = nBanos.toInt(),
                                        petFriendly = petFriendly,
                                        direccion = direccion,
                                        tipoId = tipoSeleccionado!!,
                                        comunaId = comunaSeleccionada!!,
                                        propietarioId = userId
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isSaving && !fotoSubiendo
                    ) {
                        if (isSaving || fotoSubiendo) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (fotoSubiendo) "Subiendo fotos..." else "Guardando...")
                        } else {
                            Icon(Icons.Default.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Guardar Propiedad")
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

/**
 * Crear archivo temporal para foto
 */
private fun createTempImageFile(context: android.content.Context): File {
    val timeStamp = System.currentTimeMillis()

    // CREA Y ASEGURA EL DIRECTORIO 'images' DENTRO DE LA CACHÉ
    val storageDir = File(context.cacheDir, "images").apply {
        if (!exists()) {
            mkdirs() // Asegura que la carpeta images exista
        }
    }

    // CREA EL ARCHIVO DENTRO DE ESA CARPETA
    return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
}

private fun getImageUriForFile(context: android.content.Context, file: File): Uri {
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

/**
 * Convertir URI a File
 */
private fun uriToFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()
        file
    } catch (e: Exception) {
        null
    }
}