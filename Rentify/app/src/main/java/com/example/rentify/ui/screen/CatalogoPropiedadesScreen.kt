package com.example.rentify.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rentify.ui.viewmodel.PropiedadViewModel
import com.example.rentify.ui.viewmodel.PropiedadConDistancia  // ✅ IMPORT CORREGIDO
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.text.NumberFormat
import java.util.*

/**
 * Pantalla de catálogo de propiedades con GPS
 */
@Composable
fun CatalogoPropiedadesScreen(
    vm: PropiedadViewModel,
    onVerDetalle: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val propiedades by vm.propiedades.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val permisoUbicacion by vm.permisoUbicacion.collectAsStateWithLifecycle()
    val ubicacionUsuario by vm.ubicacionUsuario.collectAsStateWithLifecycle()

    // Launcher para solicitar permisos de ubicación
    val permisosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        vm.setPermisoUbicacion(concedido)

        if (concedido) {
            obtenerUbicacionActual(context, vm)
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            // Cargar propiedades sin orden por distancia
            vm.cargarPropiedadesCercanas()
        }
    }

    // Efecto para solicitar permisos al iniciar
    LaunchedEffect(Unit) {
        val tienePermiso = verificarPermisoUbicacion(context)
        vm.setPermisoUbicacion(tienePermiso)

        if (tienePermiso) {
            obtenerUbicacionActual(context, vm)
        } else {
            // Solicitar permisos
            permisosLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val bg = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ========== HEADER ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Propiedades Disponibles",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (permisoUbicacion && ubicacionUsuario != null) {
                                Text(
                                    "Ordenadas por cercanía a tu ubicación",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Botón para actualizar ubicación
                    if (permisoUbicacion) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { obtenerUbicacionActual(context, vm) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Actualizar mi ubicación")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ========== LISTA DE PROPIEDADES ==========
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Cargando propiedades...")
                    }
                }
            } else if (propiedades.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No hay propiedades disponibles")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(propiedades) { item ->
                        PropiedadCard(item, permisoUbicacion)
                    }
                }
            }
        }
    }
}

/**
 * Card individual de propiedad
 */
@Composable
private fun PropiedadCard(
    item: PropiedadConDistancia,  // ✅ TIPO CORREGIDO
    mostrarDistancia: Boolean
) {
    val propiedad = item.propiedad
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Título y código
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    propiedad.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        propiedad.codigo,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Ubicación y distancia
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    item.nombreComuna ?: "Comuna desconocida",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (mostrarDistancia && item.distanciaKm != null) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "%.1f km".format(item.distanciaKm),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Características
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CaracteristicaChip(Icons.Filled.SquareFoot, "${propiedad.m2} m²")
                CaracteristicaChip(Icons.Filled.Bed, "${propiedad.n_habit} hab")
                CaracteristicaChip(Icons.Filled.Bathroom, "${propiedad.n_banos} baños")
                if (propiedad.pet_friendly) {
                    CaracteristicaChip(Icons.Filled.Pets, "Mascotas")
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            // Precio y botón
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
                        numberFormat.format(propiedad.precio_mensual),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(onClick = { /* TODO: Ver detalles */ }) {
                    Text("Ver más")
                }
            }
        }
    }
}

/**
 * Chip para mostrar características
 */
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
            Text(
                text,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/**
 * Verifica si se tiene permiso de ubicación
 */
private fun verificarPermisoUbicacion(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}

/**
 * Obtiene la ubicación actual del dispositivo
 */
@SuppressLint("MissingPermission")
private fun obtenerUbicacionActual(context: Context, vm: PropiedadViewModel) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val cancellationTokenSource = CancellationTokenSource()

    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        cancellationTokenSource.token
    ).addOnSuccessListener { location ->
        if (location != null) {
            vm.actualizarUbicacion(location.latitude, location.longitude)
            Toast.makeText(
                context,
                "Ubicación actualizada: ${location.latitude}, ${location.longitude}",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                context,
                "No se pudo obtener la ubicación",
                Toast.LENGTH_SHORT
            ).show()
            vm.cargarPropiedadesCercanas()
        }
    }.addOnFailureListener {
        Toast.makeText(
            context,
            "Error al obtener ubicación: ${it.message}",
            Toast.LENGTH_SHORT
        ).show()
        vm.cargarPropiedadesCercanas()
    }
}