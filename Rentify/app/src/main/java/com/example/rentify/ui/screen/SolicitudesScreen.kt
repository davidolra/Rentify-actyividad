package com.example.rentify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rentify.data.local.RentifyDatabase
import com.example.rentify.data.repository.ApplicationRemoteRepository
import com.example.rentify.navigation.Routes
import com.example.rentify.ui.viewmodel.SolicitudesViewModel
import com.example.rentify.ui.viewmodel.SolicitudesViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesScreen(
    navController: NavController,
    context: android.content.Context,
    esArrendatario: Boolean = true
) {
    val db = RentifyDatabase.getInstance(context)
    val viewModel: SolicitudesViewModel = viewModel(
        factory = SolicitudesViewModelFactory(
            solicitudDao = db.solicitudDao(),
            usuarioDao = db.usuarioDao(),
            applicationRepository = ApplicationRemoteRepository(
                solicitudDao = db.solicitudDao(),
                catalogDao = db.catalogDao()
            )
        )
    )

    val solicitudes by viewModel.solicitudes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val filtroEstado by viewModel.filtroEstado.collectAsState()

    LaunchedEffect(esArrendatario) {
        if (esArrendatario) {
            viewModel.cargarSolicitudesArrendatario()
        } else {
            viewModel.cargarSolicitudesPropietario()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esArrendatario) "Mis Solicitudes" else "Solicitudes Recibidas") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtroEstado == null,
                    onClick = { viewModel.setFiltroEstado(null) },
                    label = { Text("Todas") }
                )
                FilterChip(
                    selected = filtroEstado == 1L,
                    onClick = { viewModel.setFiltroEstado(1L) },
                    label = { Text("Pendientes") }
                )
                FilterChip(
                    selected = filtroEstado == 2L,
                    onClick = { viewModel.setFiltroEstado(2L) },
                    label = { Text("Aprobadas") }
                )
                FilterChip(
                    selected = filtroEstado == 3L,
                    onClick = { viewModel.setFiltroEstado(3L) },
                    label = { Text("Rechazadas") }
                )
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                solicitudes.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay solicitudes",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(solicitudes) { solicitudConDetalles ->
                            SolicitudCard(
                                solicitud = solicitudConDetalles,
                                esArrendatario = esArrendatario,
                                onClick = {
                                    navController.navigate(
                                        "${Routes.SOLICITUD_DETALLE}/${solicitudConDetalles.solicitud.id}"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SolicitudCard(
    solicitud: com.example.rentify.ui.viewmodel.SolicitudConDetalles,
    esArrendatario: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = solicitud.direccionPropiedad ?: "Propiedad",
                    style = MaterialTheme.typography.titleMedium
                )

                EstadoChip(estadoNombre = solicitud.estadoNombre ?: "Pendiente")
            }

            if (!esArrendatario) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = solicitud.nombreUsuario ?: "Usuario",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = solicitud.solicitud.fechaSolicitud,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EstadoChip(estadoNombre: String) {
    val color = when (estadoNombre) {
        "Pendiente" -> MaterialTheme.colorScheme.secondary
        "Aprobada" -> MaterialTheme.colorScheme.primary
        "Rechazada" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = estadoNombre,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}