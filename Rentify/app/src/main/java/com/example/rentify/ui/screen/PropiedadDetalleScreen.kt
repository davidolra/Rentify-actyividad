package com.example.rentify.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
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
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.ui.viewmodel.PropiedadDetalleViewModel
import com.example.rentify.ui.viewmodel.SolicitudesViewModel
import java.text.NumberFormat
import java.util.*
import kotlinx.coroutines.launch

/**
 * Pantalla de detalle completo de una propiedad
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropiedadDetalleScreen(
    propiedadId: Long,
    vm: PropiedadDetalleViewModel,
    solicitudesVm: SolicitudesViewModel,
    onBack: () -> Unit,
    onSolicitar: () -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()

    // Estados del ViewModel
    val propiedad by vm.propiedad.collectAsStateWithLifecycle()
    val nombreComuna by vm.nombreComuna.collectAsStateWithLifecycle()
    val nombreTipo by vm.nombreTipo.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val errorMsg by vm.errorMsg.collectAsStateWithLifecycle()

    // Estado de usuario
    val isLoggedIn by userPrefs.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    val userId by userPrefs.userId.collectAsStateWithLifecycle(initialValue = null)

    // Estado para diálogo de confirmación
    var showDialog by remember { mutableStateOf(false) }
    var solicitudEnviada by remember { mutableStateOf(false) }

    // Cargar propiedad al iniciar
    LaunchedEffect(propiedadId) {
        vm.cargarPropiedad(propiedadId)
    }

    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Propiedad") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text("Cargando información...")
                        }
                    }
                }

                errorMsg != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                errorMsg ?: "Error desconocido",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = onBack) {
                                Text("Volver")
                            }
                        }
                    }
                }

                propiedad != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // ========== TÍTULO Y CÓDIGO ==========
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        propiedad!!.titulo,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            propiedad!!.codigo,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // ========== PRECIO ==========
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Precio de Arriendo",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    numberFormat.format(propiedad!!.precio_mensual) + " / mes",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // ========== UBICACIÓN ==========
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Ubicación",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    propiedad!!.direccion,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (nombreComuna != null) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        nombreComuna!!,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // ========== CARACTERÍSTICAS ==========
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Características",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(16.dp))

                                if (nombreTipo != null) {
                                    DetalleItem(
                                        icon = Icons.Filled.Home,
                                        label = "Tipo",
                                        value = nombreTipo!!
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }

                                DetalleItem(
                                    icon = Icons.Filled.SquareFoot,
                                    label = "Superficie",
                                    value = "${propiedad!!.m2} m²"
                                )
                                Spacer(Modifier.height(12.dp))

                                DetalleItem(
                                    icon = Icons.Filled.Bed,
                                    label = "Habitaciones",
                                    value = "${propiedad!!.n_habit}"
                                )
                                Spacer(Modifier.height(12.dp))

                                DetalleItem(
                                    icon = Icons.Filled.Bathroom,
                                    label = "Baños",
                                    value = "${propiedad!!.n_banos}"
                                )
                                Spacer(Modifier.height(12.dp))

                                DetalleItem(
                                    icon = Icons.Filled.Pets,
                                    label = "Mascotas",
                                    value = if (propiedad!!.pet_friendly) "Permitidas ✓" else "No permitidas ✗",
                                    valueColor = if (propiedad!!.pet_friendly)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // ========== BOTÓN DE SOLICITUD ==========
                        if (solicitudEnviada) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "¡Solicitud Enviada!",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Puedes ver tus solicitudes en el menú",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onSolicitar,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.List, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Ver Mis Solicitudes")
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (isLoggedIn) {
                                        showDialog = true
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Debes iniciar sesión para enviar solicitudes",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Icon(Icons.Filled.Send, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Enviar Solicitud de Arriendo",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    // ========== DIÁLOGO DE CONFIRMACIÓN ==========
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                Icon(Icons.Filled.Send, contentDescription = null)
            },
            title = {
                Text("Confirmar Solicitud")
            },
            text = {
                Text("¿Deseas enviar una solicitud de arriendo para ${propiedad?.titulo}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false

                        if (userId != null) {
                            scope.launch {
                                val resultado = solicitudesVm.crearSolicitud(
                                    usuarioId = userId!!,
                                    propiedadId = propiedadId,
                                    mensaje = null
                                )

                                resultado.onSuccess {
                                    solicitudEnviada = true
                                    Toast.makeText(
                                        context,
                                        "Solicitud enviada exitosamente",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }.onFailure { error ->
                                    Toast.makeText(
                                        context,
                                        error.message ?: "Error al enviar solicitud",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun DetalleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}