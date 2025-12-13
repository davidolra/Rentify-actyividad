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
 * Pantalla de solicitudes multi-rol
 * - ARRIENDATARIO: Ve sus propias solicitudes
 * - PROPIETARIO: Ve solicitudes de sus propiedades y puede aceptar/rechazar
 * - ADMIN: Ve todas las solicitudes
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

    // Estados del ViewModel
    val solicitudes by viewModel.solicitudes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.errorMsg.collectAsState()
    val successMsg by viewModel.successMsg.collectAsState()
    val solicitudCreada by viewModel.solicitudCreada.collectAsState()

    // Datos del usuario
    val userId by userPreferences.userId.collectAsState(initial = null)
    val userRoleId by userPreferences.userRoleId.collectAsState(initial = null)

    // Variables locales para evitar smart cast errors
    val currentUserId = userId
    val currentRoleId = userRoleId ?: 3  // Default: ARRIENDATARIO

    // Snackbar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar solicitudes segun rol al iniciar
    LaunchedEffect(currentUserId, currentRoleId) {
        currentUserId?.let { id ->
            viewModel.cargarSolicitudes(id, currentRoleId)
        }
    }

    // Mostrar mensaje de exito
    LaunchedEffect(successMsg) {
        successMsg?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccess()
        }
    }

    // Mostrar mensaje cuando se crea solicitud
    LaunchedEffect(solicitudCreada) {
        if (solicitudCreada) {
            snackbarHostState.showSnackbar(
                message = "Solicitud enviada exitosamente",
                duration = SnackbarDuration.Short
            )
            viewModel.clearSolicitudCreada()
        }
    }

    // Titulo segun rol
    val titulo = when (currentRoleId) {
        1 -> "Todas las Solicitudes"
        2 -> "Solicitudes Recibidas"
        else -> "Mis Solicitudes"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                actions = {
                    IconButton(
                        onClick = {
                            currentUserId?.let { id ->
                                viewModel.cargarSolicitudes(id, currentRoleId)
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error
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
                                    viewModel.cargarSolicitudes(id, currentRoleId)
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
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (currentRoleId) {
                                1 -> "No hay solicitudes en el sistema"
                                2 -> "No has recibido solicitudes para tus propiedades"
                                else -> "No tienes solicitudes. Busca una propiedad y crea tu primera solicitud."
                            },
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
                            // Determinar si puede gestionar esta solicitud
                            val puedeGestionar = when (currentRoleId) {
                                1 -> true  // Admin puede gestionar todas
                                2 -> solicitudConDatos.nombreEstado == "PENDIENTE"  // Propietario solo pendientes
                                else -> false  // Arriendatario no puede gestionar
                            }

                            SolicitudCard(
                                solicitudConDatos = solicitudConDatos,
                                onClick = {
                                    onNavigateToDetalle(solicitudConDatos.solicitud.propiedad_id)
                                },
                                mostrarSolicitante = currentRoleId != 3,  // No mostrar al arriendatario
                                onActualizarEstado = if (puedeGestionar) {
                                    { nuevoEstado ->
                                        currentUserId?.let { id ->
                                            viewModel.actualizarEstadoSolicitud(
                                                solicitudId = solicitudConDatos.solicitud.id,
                                                nuevoEstado = nuevoEstado,
                                                usuarioId = id,
                                                rolId = currentRoleId
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
 * Card con estadisticas de solicitudes
 */
@Composable
private fun SolicitudesStatsCard(
    solicitudes: List<com.example.rentify.ui.viewmodel.SolicitudConDatos>
) {
    val pendientes = solicitudes.count { it.nombreEstado == "PENDIENTE" }
    val aceptadas = solicitudes.count {
        it.nombreEstado == "ACEPTADA" || it.nombreEstado == "APROBADA"
    }
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
            StatItem(
                label = "Pendientes",
                value = pendientes,
                color = MaterialTheme.colorScheme.tertiary
            )

            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
            )

            StatItem(
                label = "Aceptadas",
                value = aceptadas,
                color = MaterialTheme.colorScheme.primary
            )

            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
            )

            StatItem(
                label = "Rechazadas",
                value = rechazadas,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}