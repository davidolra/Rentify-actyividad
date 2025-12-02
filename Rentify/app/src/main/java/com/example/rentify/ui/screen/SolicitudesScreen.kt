package com.example.rentify.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.ui.components.SolicitudCard
import com.example.rentify.ui.viewmodel.SolicitudesViewModel
import com.example.rentify.ui.viewmodel.SolicitudesViewModelFactory

/**
 * SCREEN MEJORADO: Lista de solicitudes con manejo de estados y errores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesScreen(
    userPreferences: UserPreferences,
    viewModelFactory: SolicitudesViewModelFactory,
    onNavigateToDetalle: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: SolicitudesViewModel = viewModel(factory = viewModelFactory)

    // Estados
    val solicitudes by viewModel.solicitudes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.errorMsg.collectAsState()
    val solicitudCreada by viewModel.solicitudCreada.collectAsState()

    // Obtener datos del usuario usando Flow
    val userId by userPreferences.userId.collectAsState(initial = null)
    val userRole by userPreferences.userRole.collectAsState(initial = null)

    // FIX: Crear variables locales para evitar smart cast error
    val currentUserId = userId
    val currentUserRole = userRole

    // Cargar solicitudes al iniciar
    LaunchedEffect(currentUserId) {
        currentUserId?.let { id ->
            viewModel.cargarSolicitudesUsuario(id)
        }
    }

    // Mostrar mensaje de exito cuando se crea solicitud
    LaunchedEffect(solicitudCreada) {
        if (solicitudCreada) {
            // Aqui podrias mostrar un Snackbar de exito
            viewModel.clearSolicitudCreada()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Solicitudes") },
                actions = {
                    // Boton de refresh
                    IconButton(
                        onClick = {
                            currentUserId?.let { id ->
                                viewModel.cargarSolicitudesUsuario(id)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // ESTADO: Cargando
                isLoading && solicitudes.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cargando solicitudes...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ESTADO: Error
                errorMsg != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMsg ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.clearError()
                                currentUserId?.let { id ->
                                    viewModel.cargarSolicitudesUsuario(id)
                                }
                            }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }

                // ESTADO: Sin solicitudes
                solicitudes.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Sin solicitudes",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tienes solicitudes",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Busca una propiedad y crea tu primera solicitud",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ESTADO: Lista de solicitudes
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header con estadisticas
                        item {
                            SolicitudesStatsCard(solicitudes = solicitudes)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Lista de solicitudes
                        items(
                            items = solicitudes,
                            key = { it.solicitud.id }
                        ) { solicitudConDatos ->
                            SolicitudCard(
                                solicitudConDatos = solicitudConDatos,
                                onClick = {
                                    onNavigateToDetalle(solicitudConDatos.solicitud.id)
                                },
                                onActualizarEstado = if (currentUserRole == "PROPIETARIO") {
                                    { nuevoEstado ->
                                        currentUserId?.let { id ->
                                            viewModel.actualizarEstadoSolicitud(
                                                solicitudId = solicitudConDatos.solicitud.id,
                                                nuevoEstado = nuevoEstado,
                                                usuarioId = id
                                            )
                                        }
                                    }
                                } else null
                            )
                        }

                        // Espacio al final
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Indicador de carga superpuesto
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
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Actualizando...",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * COMPONENTE: Card con estadisticas de solicitudes
 */
@Composable
private fun SolicitudesStatsCard(
    solicitudes: List<com.example.rentify.ui.viewmodel.SolicitudConDatos>
) {
    val pendientes = solicitudes.count { it.nombreEstado == "PENDIENTE" }
    val aceptadas = solicitudes.count { it.nombreEstado == "ACEPTADA" }
    val rechazadas = solicitudes.count { it.nombreEstado == "RECHAZADA" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Pendientes
            StatItem(
                label = "Pendientes",
                value = pendientes,
                emoji = "Pendiente"
            )

            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
            )

            // Aceptadas
            StatItem(
                label = "Aceptadas",
                value = aceptadas,
                emoji = "Aceptada"
            )

            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
            )

            // Rechazadas
            StatItem(
                label = "Rechazadas",
                value = rechazadas,
                emoji = "Rechazada"
            )
        }
    }
}

/**
 * COMPONENTE: Item de estadistica
 */
@Composable
private fun StatItem(
    label: String,
    value: Int,
    emoji: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}