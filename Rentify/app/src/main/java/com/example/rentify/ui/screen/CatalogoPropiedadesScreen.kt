package com.example.rentify.ui.screen

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rentify.ui.viewmodel.PropiedadConDistancia
import com.example.rentify.ui.viewmodel.PropiedadViewModel
import com.example.rentify.ui.viewmodel.PropiedadViewModelFactory
import com.google.android.gms.location.LocationServices
import java.text.NumberFormat
import java.util.*

/**
 * Pantalla de catalogo de propiedades con GPS
 * Carga propiedades desde el backend PropertyService
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoPropiedadesScreen(
    viewModelFactory: PropiedadViewModelFactory,
    onVerDetalle: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: PropiedadViewModel = viewModel(factory = viewModelFactory)

    // Estados
    val propiedades by viewModel.propiedades.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val ubicacionUsuario by viewModel.ubicacionUsuario.collectAsStateWithLifecycle()
    val permisoUbicacion by viewModel.permisoUbicacion.collectAsStateWithLifecycle()
    val errorMsg by viewModel.errorMsg.collectAsStateWithLifecycle()

    // Cliente de ubicacion
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Launcher para permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocation || coarseLocation) {
            viewModel.setPermisoUbicacion(true)
            obtenerUbicacion(fusedLocationClient, viewModel)
        } else {
            viewModel.setPermisoUbicacion(false)
            viewModel.cargarPropiedadesCercanas()
        }
    }

    // Solicitar permisos al iniciar
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Mostrar errores
    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Propiedades Disponibles")
                        if (ubicacionUsuario != null) {
                            Text(
                                "Ordenadas por cercania",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (permisoUbicacion) {
                                obtenerUbicacion(fusedLocationClient, viewModel)
                            } else {
                                viewModel.cargarPropiedadesCercanas()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, "Actualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && propiedades.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Cargando propiedades...")
                    }
                }

                propiedades.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No hay propiedades disponibles",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.cargarPropiedadesCercanas() }) {
                            Text("Reintentar")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Contador de propiedades
                        item {
                            Text(
                                "${propiedades.size} propiedades encontradas",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        items(
                            items = propiedades,
                            key = { it.propiedad.id }
                        ) { propiedadConDistancia ->
                            PropiedadCard(
                                propiedadConDistancia = propiedadConDistancia,
                                onClick = { onVerDetalle(propiedadConDistancia.propiedad.id) }
                            )
                        }

                        item {
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    // Loading overlay
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                tonalElevation = 8.dp
                            ) {
                                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card de propiedad con info completa
 */
@Composable
private fun PropiedadCard(
    propiedadConDistancia: PropiedadConDistancia,
    onClick: () -> Unit
) {
    val propiedad = propiedadConDistancia.propiedad
    val propiedadRemota = propiedadConDistancia.propiedadRemota
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Galeria de fotos
            val fotos = propiedadRemota?.fotos
            if (!fotos.isNullOrEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentPadding = PaddingValues(0.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(fotos.take(5)) { foto ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(foto.url)
                                .crossfade(true)
                                .build(),
                            contentDescription = foto.nombre,
                            modifier = Modifier
                                .width(if (fotos.size == 1) 400.dp else 200.dp)
                                .height(180.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
                // Placeholder si no hay fotos
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Header con codigo y distancia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            propiedad.codigo,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    propiedadConDistancia.distanciaKm?.let { distancia ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "%.1f km".format(distancia),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Titulo
                Text(
                    propiedad.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                // Ubicacion
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        propiedadConDistancia.nombreComuna ?: propiedad.direccion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Caracteristicas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CaracteristicaChip(
                        icon = Icons.Default.SquareFoot,
                        text = "${propiedad.m2.toInt()} m2"
                    )
                    CaracteristicaChip(
                        icon = Icons.Default.Bed,
                        text = "${propiedad.n_habit}"
                    )
                    CaracteristicaChip(
                        icon = Icons.Default.Bathtub,
                        text = "${propiedad.n_banos}"
                    )
                    if (propiedad.pet_friendly) {
                        CaracteristicaChip(
                            icon = Icons.Default.Pets,
                            text = ""
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Tipo y Precio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    propiedadRemota?.tipo?.nombre?.let { tipoNombre ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                tipoNombre,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Text(
                        "${numberFormat.format(propiedad.precio_mensual)}/${propiedad.divisa}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun CaracteristicaChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (text.isNotEmpty()) {
            Spacer(Modifier.width(4.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Obtiene la ubicacion del usuario
 */
@Suppress("MissingPermission")
private fun obtenerUbicacion(
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    viewModel: PropiedadViewModel
) {
    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                viewModel.actualizarUbicacion(location.latitude, location.longitude)
            } else {
                viewModel.cargarPropiedadesCercanas()
            }
        }.addOnFailureListener {
            viewModel.cargarPropiedadesCercanas()
        }
    } catch (e: Exception) {
        viewModel.cargarPropiedadesCercanas()
    }
}