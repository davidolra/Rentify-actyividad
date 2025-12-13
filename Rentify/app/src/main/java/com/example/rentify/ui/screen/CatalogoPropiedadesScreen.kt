package com.example.rentify.ui.screen

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentify.ui.viewmodel.PropiedadConDistancia
import com.example.rentify.ui.viewmodel.PropiedadViewModel
import com.example.rentify.ui.viewmodel.PropiedadViewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.text.NumberFormat
import java.util.*

/**
 * Pantalla de catalogo de propiedades con GPS
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

    val propiedades by viewModel.propiedades.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val ubicacionUsuario by viewModel.ubicacionUsuario.collectAsStateWithLifecycle()
    val permisoUbicacion by viewModel.permisoUbicacion.collectAsStateWithLifecycle()
    val errorMsg by viewModel.errorMsg.collectAsStateWithLifecycle()

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocation || coarseLocation) {
            viewModel.setPermisoUbicacion(true)
            obtenerUbicacion(fusedLocationClient, viewModel, context)
        } else {
            viewModel.setPermisoUbicacion(false)
            viewModel.cargarPropiedadesCercanas()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

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
                        if (permisoUbicacion && ubicacionUsuario != null) {
                            Text(
                                "Ordenadas por cercania",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                actions = {
                    if (permisoUbicacion) {
                        IconButton(onClick = {
                            obtenerUbicacion(fusedLocationClient, viewModel, context)
                        }) {
                            Icon(Icons.Default.MyLocation, "Actualizar ubicacion")
                        }
                    }
                    IconButton(onClick = { viewModel.cargarPropiedadesCercanas() }) {
                        Icon(Icons.Default.Refresh, "Recargar")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
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
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No hay propiedades disponibles")
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(propiedades, key = { it.propiedad.id }) { propiedadConDistancia ->
                            PropiedadCard(
                                propiedadConDistancia = propiedadConDistancia,
                                mostrarDistancia = permisoUbicacion,
                                onClick = { onVerDetalle(propiedadConDistancia.propiedad.id) }
                            )
                        }

                        item {
                            Spacer(Modifier.height(16.dp))
                        }
                    }

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
 * Card de propiedad simplificada
 */
@Composable
private fun PropiedadCard(
    propiedadConDistancia: PropiedadConDistancia,
    mostrarDistancia: Boolean,
    onClick: () -> Unit
) {
    val propiedad = propiedadConDistancia.propiedad
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header con codigo y tipo
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

                propiedadConDistancia.nombreTipo?.let { tipoNombre ->
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

            Spacer(Modifier.height(8.dp))

            // Ubicacion y distancia
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    propiedadConDistancia.nombreComuna ?: "Comuna",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (mostrarDistancia && propiedadConDistancia.distanciaKm != null) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "%.1f km".format(propiedadConDistancia.distanciaKm),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Caracteristicas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CaracteristicaChip(Icons.Default.SquareFoot, "${propiedad.m2.toInt()} m2")
                CaracteristicaChip(Icons.Default.Bed, "${propiedad.n_habit} hab")
                CaracteristicaChip(Icons.Default.Bathroom, "${propiedad.n_banos} banos")
                if (propiedad.pet_friendly) {
                    CaracteristicaChip(Icons.Default.Pets, "Mascotas")
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Precio y boton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Arriendo mensual",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${numberFormat.format(propiedad.precio_mensual)}/${propiedad.divisa}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(onClick = onClick) {
                    Text("Ver mas")
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
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@android.annotation.SuppressLint("MissingPermission")
private fun obtenerUbicacion(
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    viewModel: PropiedadViewModel,
    context: android.content.Context
) {
    val cancellationTokenSource = CancellationTokenSource()

    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        cancellationTokenSource.token
    ).addOnSuccessListener { location ->
        if (location != null) {
            viewModel.actualizarUbicacion(location.latitude, location.longitude)
        } else {
            viewModel.cargarPropiedadesCercanas()
        }
    }.addOnFailureListener {
        Toast.makeText(context, "Error al obtener ubicacion", Toast.LENGTH_SHORT).show()
        viewModel.cargarPropiedadesCercanas()
    }
}