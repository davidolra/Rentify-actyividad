package com.example.rentify.ui.screen

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.data.local.entities.EstadoSolicitud
import com.example.rentify.ui.viewmodel.SolicitudesViewModel
import com.example.rentify.ui.viewmodel.SolicitudConPropiedad
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesScreen(
    vm: SolicitudesViewModel,
    onBack: () -> Unit,
    onVerPropiedad: (Long) -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val userId by userPrefs.userId.collectAsStateWithLifecycle(initialValue = null)

    val solicitudes by vm.solicitudes.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val errorMsg by vm.errorMsg.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        userId?.let { vm.cargarSolicitudes(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Solicitudes") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                errorMsg != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.Error, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Text(errorMsg ?: "Error desconocido", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                solicitudes.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.Assignment, null, Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(16.dp))
                        Text("No tienes solicitudes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Explora propiedades y solicita tu arriendo ideal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Total de solicitudes: ${solicitudes.size}",
                                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Máximo permitido: 3 solicitudes activas",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }

                        items(solicitudes) { solicitud ->
                            SolicitudCard(solicitud, onVerPropiedad)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SolicitudCard(
    solicitud: SolicitudConPropiedad,
    onVerPropiedad: (Long) -> Unit
) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header con estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Solicitud #${solicitud.solicitud.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = when (solicitud.solicitud.estado.uppercase()) {
                        EstadoSolicitud.PENDIENTE -> MaterialTheme.colorScheme.secondaryContainer
                        EstadoSolicitud.APROBADA -> MaterialTheme.colorScheme.primaryContainer
                        EstadoSolicitud.RECHAZADA -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        solicitud.solicitud.estado,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Fecha solicitud
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Fecha: ${dateFormat.format(Date(solicitud.solicitud.fecha_solicitud))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            // Datos de la propiedad
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        solicitud.propiedad?.titulo ?: "Propiedad desconocida",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        solicitud.propiedad?.codigo ?: "N/A",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            // Total y botón ver propiedad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Precio mensual",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        numberFormat.format(solicitud.propiedad?.precio_mensual ?: 0),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedButton(
                    onClick = { onVerPropiedad(solicitud.solicitud.propiedad_id) }
                ) {
                    Icon(Icons.Filled.Visibility, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ver Propiedad")
                }
            }
        }
    }
}
