package com.example.rentify.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.ui.viewmodel.SolicitudConDatos
import com.example.rentify.ui.viewmodel.SolicitudesViewModel
import com.example.rentify.ui.viewmodel.SolicitudesViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de solicitudes multi-rol
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesScreen(
    userPreferences: UserPreferences,
    viewModelFactory: SolicitudesViewModelFactory,
    onNavigateToDetalle: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: SolicitudesViewModel = viewModel(factory = viewModelFactory)

    // Estados del ViewModel
    val solicitudes by viewModel.solicitudes.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMsg by viewModel.errorMsg.collectAsStateWithLifecycle()
    val successMsg by viewModel.successMsg.collectAsStateWithLifecycle()
    val solicitudCreada by viewModel.solicitudCreada.collectAsStateWithLifecycle()

    // Datos del usuario
    val userId by userPreferences.userId.collectAsStateWithLifecycle(initialValue = null)
    val userRole by userPreferences.userRole.collectAsStateWithLifecycle(initialValue = null)

    val currentUserId = userId
    val currentUserRole = userRole

    // Mapear rol a ID
    val rolId = when (currentUserRole?.uppercase()) {
        "ADMINISTRADOR" -> SolicitudesViewModel.ROL_ADMIN
        "PROPIETARIO" -> SolicitudesViewModel.ROL_PROPIETARIO
        "ARRENDATARIO" -> SolicitudesViewModel.ROL_ARRIENDATARIO
        else -> SolicitudesViewModel.ROL_ARRIENDATARIO
    }

    // Dialogs
    var showDetalleDialog by remember { mutableStateOf(false) }
    var showRechazarDialog by remember { mutableStateOf(false) }
    var showEliminarDialog by remember { mutableStateOf(false) }
    var solicitudParaAccion by remember { mutableStateOf<SolicitudConDatos?>(null) }
    var motivoRechazo by remember { mutableStateOf("") }

    // Cargar solicitudes al iniciar
    LaunchedEffect(currentUserId, rolId) {
        currentUserId?.let { id ->
            viewModel.cargarSolicitudes(id, rolId)
        }
    }

    // Mostrar mensajes
    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(successMsg) {
        successMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccess()
        }
    }

    LaunchedEffect(solicitudCreada) {
        if (solicitudCreada) {
            viewModel.clearSolicitudCreada()
        }
    }

    // Titulo segun rol
    val titulo = when (rolId) {
        SolicitudesViewModel.ROL_ADMIN -> "Gestion de Solicitudes"
        SolicitudesViewModel.ROL_PROPIETARIO -> "Solicitudes de Mis Propiedades"
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
                                viewModel.cargarSolicitudes(id, rolId)
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
                isLoading && solicitudes.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Cargando solicitudes...")
                    }
                }

                solicitudes.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            when (rolId) {
                                SolicitudesViewModel.ROL_ADMIN -> "No hay solicitudes en el sistema"
                                SolicitudesViewModel.ROL_PROPIETARIO -> "No hay solicitudes para tus propiedades"
                                else -> "No tienes solicitudes"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Estadisticas
                        item {
                            SolicitudesStatsCard(solicitudes = solicitudes)
                            Spacer(Modifier.height(8.dp))
                        }

                        // Lista de solicitudes
                        items(
                            items = solicitudes,
                            key = { it.solicitud.id }
                        ) { solicitudConDatos ->
                            SolicitudCardCompleta(
                                solicitudConDatos = solicitudConDatos,
                                userRole = rolId,
                                onVerDetalle = {
                                    solicitudParaAccion = solicitudConDatos
                                    showDetalleDialog = true
                                },
                                onVerPropiedad = {
                                    onNavigateToDetalle(solicitudConDatos.solicitud.propiedad_id)
                                },
                                onAprobar = {
                                    currentUserId?.let { uid ->
                                        viewModel.aprobarSolicitud(
                                            solicitudConDatos.solicitud.id,
                                            uid,
                                            rolId
                                        )
                                    }
                                },
                                onRechazar = {
                                    solicitudParaAccion = solicitudConDatos
                                    showRechazarDialog = true
                                },
                                onEliminar = {
                                    solicitudParaAccion = solicitudConDatos
                                    showEliminarDialog = true
                                }
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

    // Dialog de detalle
    if (showDetalleDialog && solicitudParaAccion != null) {
        SolicitudDetalleDialog(
            solicitud = solicitudParaAccion!!,
            userRole = rolId,
            onDismiss = { showDetalleDialog = false },
            onAprobar = {
                currentUserId?.let { uid ->
                    viewModel.aprobarSolicitud(solicitudParaAccion!!.solicitud.id, uid, rolId)
                }
                showDetalleDialog = false
            },
            onRechazar = {
                showDetalleDialog = false
                showRechazarDialog = true
            }
        )
    }

    // Dialog de rechazo
    if (showRechazarDialog && solicitudParaAccion != null) {
        RechazarDialog(
            onDismiss = {
                showRechazarDialog = false
                motivoRechazo = ""
            },
            onConfirm = {
                currentUserId?.let { uid ->
                    viewModel.rechazarSolicitud(
                        solicitudParaAccion!!.solicitud.id,
                        motivoRechazo,
                        uid,
                        rolId
                    )
                }
                showRechazarDialog = false
                motivoRechazo = ""
            },
            motivo = motivoRechazo,
            onMotivoChange = { motivoRechazo = it }
        )
    }

    // Dialog de eliminacion
    if (showEliminarDialog && solicitudParaAccion != null) {
        EliminarDialog(
            onDismiss = { showEliminarDialog = false },
            onConfirm = {
                currentUserId?.let { uid ->
                    viewModel.eliminarSolicitud(solicitudParaAccion!!.solicitud.id, uid)
                }
                showEliminarDialog = false
            }
        )
    }
}

/**
 * Card de estadisticas
 */
@Composable
private fun SolicitudesStatsCard(solicitudes: List<SolicitudConDatos>) {
    val pendientes = solicitudes.count { it.nombreEstado?.uppercase() == "PENDIENTE" }
    val aprobadas = solicitudes.count {
        it.nombreEstado?.uppercase() in listOf("ACEPTADA", "APROBADA")
    }
    val rechazadas = solicitudes.count {
        it.nombreEstado?.uppercase() in listOf("RECHAZADA", "RECHAZADO")
    }

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
                icon = Icons.Default.HourglassEmpty,
                label = "Pendientes",
                value = pendientes,
                color = MaterialTheme.colorScheme.tertiary
            )

            VerticalDivider(Modifier.height(48.dp))

            StatItem(
                icon = Icons.Default.CheckCircle,
                label = "Aprobadas",
                value = aprobadas,
                color = MaterialTheme.colorScheme.primary
            )

            VerticalDivider(Modifier.height(48.dp))

            StatItem(
                icon = Icons.Default.Cancel,
                label = "Rechazadas",
                value = rechazadas,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            value.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Card de solicitud completa
 */
@Composable
private fun SolicitudCardCompleta(
    solicitudConDatos: SolicitudConDatos,
    userRole: Int,
    onVerDetalle: () -> Unit,
    onVerPropiedad: () -> Unit,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
    onEliminar: () -> Unit
) {
    val estado = solicitudConDatos.nombreEstado?.uppercase() ?: "PENDIENTE"
    val puedeGestionar = userRole in listOf(SolicitudesViewModel.ROL_ADMIN, SolicitudesViewModel.ROL_PROPIETARIO) &&
            estado == "PENDIENTE"

    val estadoColor = when (estado) {
        "PENDIENTE" -> MaterialTheme.colorScheme.tertiaryContainer
        "ACEPTADA", "APROBADA" -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }

    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        solicitudConDatos.tituloPropiedad ?: "Sin titulo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    solicitudConDatos.codigoPropiedad?.let { codigo ->
                        Text(
                            "Codigo: $codigo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = estadoColor,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        estado,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Info del solicitante (para propietario/admin)
            if (userRole != SolicitudesViewModel.ROL_ARRIENDATARIO && solicitudConDatos.nombreSolicitante != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        solicitudConDatos.nombreSolicitante,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                solicitudConDatos.emailSolicitante?.let { email ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(email, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Fecha y precio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        dateFormat.format(Date(solicitudConDatos.solicitud.fsolicitud)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                solicitudConDatos.precioMensual?.let { precio ->
                    Text(
                        "${numberFormat.format(precio)}/mes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // Acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onVerDetalle) {
                    Icon(Icons.Default.Info, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Detalle")
                }

                TextButton(onClick = onVerPropiedad) {
                    Icon(Icons.Default.Home, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ver Propiedad")
                }

                if (puedeGestionar) {
                    TextButton(onClick = onAprobar) {
                        Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Aprobar")
                    }

                    TextButton(
                        onClick = onRechazar,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Rechazar")
                    }
                }

                if (userRole == SolicitudesViewModel.ROL_ADMIN) {
                    TextButton(
                        onClick = onEliminar,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/**
 * Dialog de detalle de solicitud
 */
@Composable
private fun SolicitudDetalleDialog(
    solicitud: SolicitudConDatos,
    userRole: Int,
    onDismiss: () -> Unit,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    val estado = solicitud.nombreEstado?.uppercase() ?: "PENDIENTE"
    val puedeGestionar = userRole in listOf(SolicitudesViewModel.ROL_ADMIN, SolicitudesViewModel.ROL_PROPIETARIO) &&
            estado == "PENDIENTE"

    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalle de Solicitud") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Propiedad
                Text("Propiedad", style = MaterialTheme.typography.labelMedium)
                Text(
                    solicitud.tituloPropiedad ?: "Sin titulo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                solicitud.codigoPropiedad?.let {
                    Text("Codigo: $it", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(16.dp))

                // Solicitante
                if (solicitud.nombreSolicitante != null) {
                    Text("Solicitante", style = MaterialTheme.typography.labelMedium)
                    Text(solicitud.nombreSolicitante, style = MaterialTheme.typography.bodyMedium)
                    solicitud.emailSolicitante?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                    solicitud.telefonoSolicitante?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Estado
                Text("Estado", style = MaterialTheme.typography.labelMedium)
                Text(
                    estado,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                // Fecha
                Text("Fecha de solicitud", style = MaterialTheme.typography.labelMedium)
                Text(
                    dateFormat.format(Date(solicitud.solicitud.fsolicitud)),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(16.dp))

                // Precio
                solicitud.precioMensual?.let { precio ->
                    Text("Precio mensual", style = MaterialTheme.typography.labelMedium)
                    Text(
                        numberFormat.format(precio),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            if (puedeGestionar) {
                Row {
                    TextButton(onClick = onRechazar) {
                        Text("Rechazar", color = MaterialTheme.colorScheme.error)
                    }
                    Button(onClick = onAprobar) {
                        Text("Aprobar")
                    }
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        },
        dismissButton = {
            if (puedeGestionar) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}

/**
 * Dialog de rechazo con motivo
 */
@Composable
private fun RechazarDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    motivo: String,
    onMotivoChange: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Rechazar Solicitud") },
        text = {
            Column {
                Text("Indique el motivo del rechazo:")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = motivo,
                    onValueChange = onMotivoChange,
                    label = { Text("Motivo *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = motivo.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Rechazar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Dialog de confirmacion de eliminacion
 */
@Composable
private fun EliminarDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Eliminar Solicitud") },
        text = {
            Column {
                Text("Esta seguro de eliminar esta solicitud?")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Esta accion no se puede deshacer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}