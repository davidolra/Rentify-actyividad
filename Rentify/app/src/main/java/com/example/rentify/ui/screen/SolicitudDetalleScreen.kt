package com.example.rentify.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentify.data.local.RentifyDatabase
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.data.repository.ApplicationRemoteRepository
import com.example.rentify.data.repository.PropertyRemoteRepository
import com.example.rentify.ui.viewmodel.SolicitudesViewModel
import com.example.rentify.ui.viewmodel.SolicitudesViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Definimos las constantes aquí para evitar errores de referencia
private const val ROL_ADMIN = 1
private const val ROL_PROPIETARIO = 2
private const val ROL_ARRIENDATARIO = 3

/**
 * Pantalla de detalle de solicitud (Corregida)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudDetalleScreen(
    solicitudId: Long,
    userPreferences: UserPreferences,
    onBack: () -> Unit,
    onVerPropiedad: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Instancia de dependencias (Factory manual)
    val database = RentifyDatabase.getInstance(context)
    val applicationRepository = ApplicationRemoteRepository(
        solicitudDao = database.solicitudDao(),
        catalogDao = database.catalogDao()
    )
    val propertyRepository = PropertyRemoteRepository()

    val viewModelFactory = SolicitudesViewModelFactory(
        solicitudDao = database.solicitudDao(),
        propiedadDao = database.propiedadDao(),
        catalogDao = database.catalogDao(),
        remoteRepository = applicationRepository,
        propertyRepository = propertyRepository
    )

    val viewModel: SolicitudesViewModel = viewModel(factory = viewModelFactory)

    // Estados
    // CORRECCIÓN: Obtenemos la lista completa y buscamos la solicitud por ID
    val listaSolicitudes by viewModel.solicitudes.collectAsStateWithLifecycle()
    val solicitudSeleccionada = remember(listaSolicitudes, solicitudId) {
        listaSolicitudes.find { it.solicitud.id == solicitudId }
    }

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMsg by viewModel.errorMsg.collectAsStateWithLifecycle()
    val successMsg by viewModel.successMsg.collectAsStateWithLifecycle()

    val userId by userPreferences.userId.collectAsStateWithLifecycle(initialValue = null)
    val userRole by userPreferences.userRole.collectAsStateWithLifecycle(initialValue = null)
    val currentUserId = userId

    // Mapeo de roles
    val rolId = when (userRole?.uppercase()) {
        "ADMINISTRADOR" -> ROL_ADMIN
        "PROPIETARIO" -> ROL_PROPIETARIO
        else -> ROL_ARRIENDATARIO
    }

    var showRechazarDialog by remember { mutableStateOf(false) }
    var motivoRechazo by remember { mutableStateOf("") }

    // Efecto para cargar datos al entrar
    LaunchedEffect(solicitudId) {
        viewModel.seleccionarSolicitud(solicitudId)
    }

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
            onBack()
        }
    }

    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Solicitud") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                isLoading && solicitudSeleccionada == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Cargando solicitud...")
                    }
                }

                solicitudSeleccionada == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(errorMsg ?: "Solicitud no encontrada")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onBack) { Text("Volver") }
                    }
                }

                else -> {
                    val solicitud = solicitudSeleccionada!!
                    val estado = solicitud.nombreEstado?.uppercase() ?: "PENDIENTE"

                    // Lógica de permisos
                    val puedeGestionar = (rolId == ROL_ADMIN || rolId == ROL_PROPIETARIO) && estado == "PENDIENTE"

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // --- ESTADO ---
                        val estadoColor = when (estado) {
                            "PENDIENTE" -> MaterialTheme.colorScheme.tertiary
                            "ACEPTADA", "APROBADA" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.error
                        }

                        Surface(
                            color = estadoColor.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    when (estado) {
                                        "PENDIENTE" -> Icons.Default.HourglassEmpty
                                        "ACEPTADA", "APROBADA" -> Icons.Default.CheckCircle
                                        else -> Icons.Default.Cancel
                                    },
                                    null,
                                    tint = estadoColor,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Estado", style = MaterialTheme.typography.labelMedium)
                                    Text(estado, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = estadoColor)
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // --- PROPIEDAD ---
                        Text("Propiedad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(solicitud.tituloPropiedad ?: "Sin título", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                solicitud.codigoPropiedad?.let { Text("Código: $it", style = MaterialTheme.typography.bodyMedium) }
                                solicitud.direccionPropiedad?.let {
                                    Spacer(Modifier.height(4.dp))
                                    Row { Icon(Icons.Default.LocationOn, null, Modifier.size(16.dp)); Text(it, style = MaterialTheme.typography.bodySmall) }
                                }
                                Spacer(Modifier.height(8.dp))
                                solicitud.precioMensual?.let {
                                    Text("${numberFormat.format(it)}/mes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = { onVerPropiedad(solicitud.solicitud.propiedad_id) }) {
                                    Icon(Icons.Default.Home, null); Spacer(Modifier.width(4.dp)); Text("Ver Propiedad")
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // --- SOLICITANTE ---
                        if (rolId != ROL_ARRIENDATARIO && solicitud.nombreSolicitante != null) {
                            Text("Solicitante", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row { Icon(Icons.Default.Person, null); Spacer(Modifier.width(8.dp)); Text(solicitud.nombreSolicitante, style = MaterialTheme.typography.titleMedium) }
                                    solicitud.emailSolicitante?.let {
                                        Spacer(Modifier.height(8.dp)); Row { Icon(Icons.Default.Email, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(it) }
                                    }
                                    solicitud.telefonoSolicitante?.let {
                                        Spacer(Modifier.height(4.dp)); Row { Icon(Icons.Default.Phone, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(it) }
                                    }
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                        }

                        // --- INFO GENERAL ---
                        Text("Información de Solicitud", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row { Icon(Icons.Default.CalendarToday, null); Spacer(Modifier.width(8.dp)); Column { Text("Fecha"); Text(dateFormat.format(Date(solicitud.solicitud.fsolicitud))) } }
                                Spacer(Modifier.height(8.dp))
                                Row { Icon(Icons.Default.Tag, null); Spacer(Modifier.width(8.dp)); Column { Text("ID"); Text("#${solicitud.solicitud.id}") } }
                            }
                        }

                        // --- BOTONES DE ACCIÓN ---
                        if (puedeGestionar) {
                            Spacer(Modifier.height(32.dp))
                            Text("Acciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        // CORRECCIÓN: Llamamos solo con el ID, el VM ya tiene la lógica
                                        viewModel.aprobarSolicitud(solicitudId)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Aprobar")
                                }

                                Button(
                                    onClick = { showRechazarDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Close, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Rechazar")
                                }
                            }
                        }
                        Spacer(Modifier.height(32.dp))
                    }

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    if (showRechazarDialog) {
        AlertDialog(
            onDismissRequest = { showRechazarDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Rechazar Solicitud") },
            text = { Text("¿Estás seguro de rechazar esta solicitud?") },
            confirmButton = {
                Button(
                    onClick = {
                        // CORRECCIÓN: Llamada simplificada
                        viewModel.rechazarSolicitud(solicitudId)
                        showRechazarDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Rechazar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRechazarDialog = false }) { Text("Cancelar") }
            }
        )
    }
}