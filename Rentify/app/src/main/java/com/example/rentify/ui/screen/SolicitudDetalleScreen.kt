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
import com.example.rentify.ui.viewmodel.SolicitudesViewModel
import com.example.rentify.ui.viewmodel.SolicitudesViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudDetalleScreen(
    navController: NavController,
    solicitudId: Long,
    context: android.content.Context
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
    val solicitud = solicitudes.find { it.solicitud.id == solicitudId }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var mostrarDialogoAprobar by remember { mutableStateOf(false) }
    var mostrarDialogoRechazar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Solicitud") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (solicitud == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Solicitud no encontrada")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Información de la Solicitud",
                            style = MaterialTheme.typography.titleMedium
                        )

                        DetailRow(
                            icon = Icons.Default.Home,
                            label = "Propiedad",
                            value = solicitud.direccionPropiedad ?: "N/A"
                        )

                        DetailRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Fecha de solicitud",
                            value = solicitud.solicitud.fechaSolicitud
                        )

                        DetailRow(
                            icon = Icons.Default.Info,
                            label = "Estado",
                            value = solicitud.estadoNombre ?: "Pendiente"
                        )

                        if (!solicitud.solicitud.mensaje.isNullOrEmpty()) {
                            DetailRow(
                                icon = Icons.Default.Message,
                                label = "Mensaje",
                                value = solicitud.solicitud.mensaje!!
                            )
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Información del Solicitante",
                            style = MaterialTheme.typography.titleMedium
                        )

                        DetailRow(
                            icon = Icons.Default.Person,
                            label = "Nombre",
                            value = solicitud.nombreUsuario ?: "N/A"
                        )

                        DetailRow(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = "usuario@email.com"
                        )

                        DetailRow(
                            icon = Icons.Default.Phone,
                            label = "Teléfono",
                            value = "+56 9 1234 5678"
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Documentos",
                            style = MaterialTheme.typography.titleMedium
                        )

                        DocumentoRow(
                            tipoDoc = "DNI",
                            estado = "Aprobado"
                        )

                        DocumentoRow(
                            tipoDoc = "Certificado de ingresos",
                            estado = "Pendiente"
                        )

                        DocumentoRow(
                            tipoDoc = "Certificado de antecedentes",
                            estado = "Aprobado"
                        )
                    }
                }

                if (solicitud.estadoNombre == "Pendiente") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { mostrarDialogoRechazar = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rechazar")
                        }

                        Button(
                            onClick = { mostrarDialogoAprobar = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aprobar")
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoAprobar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAprobar = false },
            title = { Text("Aprobar Solicitud") },
            text = { Text("¿Está seguro que desea aprobar esta solicitud?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.aprobarSolicitud(solicitudId)
                        mostrarDialogoAprobar = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Solicitud aprobada")
                            navController.navigateUp()
                        }
                    }
                ) {
                    Text("Aprobar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoAprobar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoRechazar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoRechazar = false },
            title = { Text("Rechazar Solicitud") },
            text = { Text("¿Está seguro que desea rechazar esta solicitud?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rechazarSolicitud(solicitudId)
                        mostrarDialogoRechazar = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Solicitud rechazada")
                            navController.navigateUp()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Rechazar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoRechazar = false }) {
                    Text("Cancelar")
                }
            }
        )
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
        Column(modifier = Modifier.weight(1f)) {
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

@Composable
private fun DocumentoRow(
    tipoDoc: String,
    estado: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(text = tipoDoc, style = MaterialTheme.typography.bodyMedium)
        }

        val color = when (estado) {
            "Aprobado" -> MaterialTheme.colorScheme.primary
            "Rechazado" -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.secondary
        }

        Surface(
            color = color,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = estado,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}