package com.example.rentify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.rentify.data.repository.PropertyRemoteRepository
import com.example.rentify.ui.viewmodel.PropiedadDetalleViewModel
import com.example.rentify.ui.viewmodel.PropiedadDetalleViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropiedadDetalleScreen(
    navController: NavController,
    propiedadId: Long,
    context: android.content.Context
) {
    val db = RentifyDatabase.getInstance(context)
    val viewModel: PropiedadDetalleViewModel = viewModel(
        factory = PropiedadDetalleViewModelFactory(
            propiedadId = propiedadId,
            propertyRepository = PropertyRemoteRepository(),
            applicationRepository = ApplicationRemoteRepository(
                solicitudDao = db.solicitudDao(),
                catalogDao = db.catalogDao()
            ),
            usuarioDao = db.usuarioDao()
        )
    )

    val propiedad by viewModel.propiedad.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val solicitudCreada by viewModel.solicitudCreada.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(propiedadId) {
        viewModel.cargarPropiedad()
    }

    LaunchedEffect(solicitudCreada) {
        if (solicitudCreada) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Solicitud creada exitosamente",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Propiedad") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.cargarPropiedad() }) {
                            Text("Reintentar")
                        }
                    }
                }
                propiedad != null -> {
                    PropiedadDetalleContent(
                        propiedad = propiedad!!,
                        onSolicitarArriendo = { viewModel.crearSolicitud() }
                    )
                }
            }
        }
    }
}

@Composable
private fun PropiedadDetalleContent(
    propiedad: com.example.rentify.data.remote.dto.PropertyRemoteDTO,
    onSolicitarArriendo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = propiedad.titulo,
                style = MaterialTheme.typography.headlineMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Precio mensual",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "$${String.format("%,.0f", propiedad.precioMensual)} ${propiedad.divisa}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Características",
                        style = MaterialTheme.typography.titleMedium
                    )

                    DetailRow(
                        icon = Icons.Default.Home,
                        label = "Tipo",
                        value = propiedad.tipo?.nombre ?: "N/A"
                    )
                    DetailRow(
                        icon = Icons.Default.LocationOn,
                        label = "Ubicación",
                        value = "${propiedad.comuna?.nombre ?: "N/A"}, ${propiedad.comuna?.region?.nombre ?: "N/A"}"
                    )
                    DetailRow(
                        icon = Icons.Default.Place,
                        label = "Dirección",
                        value = propiedad.direccion
                    )
                    DetailRow(
                        icon = Icons.Default.SquareFoot,
                        label = "Superficie",
                        value = "${propiedad.m2} m²"
                    )
                    DetailRow(
                        icon = Icons.Default.Bed,
                        label = "Habitaciones",
                        value = propiedad.nHabit.toString()
                    )
                    DetailRow(
                        icon = Icons.Default.Bathtub,
                        label = "Baños",
                        value = propiedad.nBanos.toString()
                    )
                    DetailRow(
                        icon = Icons.Default.Pets,
                        label = "Mascotas",
                        value = if (propiedad.petFriendly) "Permitidas" else "No permitidas"
                    )
                }
            }

            if (!propiedad.categorias.isNullOrEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Características adicionales",
                            style = MaterialTheme.typography.titleMedium
                        )

                        propiedad.categorias?.forEach { categoria ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = categoria.nombre,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onSolicitarArriendo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Solicitar Arriendo")
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}