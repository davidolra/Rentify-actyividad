package com.example.rentify.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.ui.viewmodel.PropiedadDetalleViewModel
import com.example.rentify.ui.viewmodel.PropiedadDetalleViewModelFactory
import java.text.NumberFormat
import java.util.*

/**
 * Pantalla de detalle de propiedad
 * Permite al inquilino solicitar arriendo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropiedadDetalleScreen(
    propiedadId: Long,
    userPreferences: UserPreferences,
    viewModelFactory: PropiedadDetalleViewModelFactory,
    onNavigateBack: () -> Unit,
    onNavigateToSolicitudes: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: PropiedadDetalleViewModel = viewModel(factory = viewModelFactory)

    // Estados del ViewModel
    val propiedad by viewModel.propiedad.collectAsStateWithLifecycle()
    val propiedadRemota by viewModel.propiedadRemota.collectAsStateWithLifecycle()
    val fotos by viewModel.fotos.collectAsStateWithLifecycle()
    val nombreComuna by viewModel.nombreComuna.collectAsStateWithLifecycle()
    val nombreTipo by viewModel.nombreTipo.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMsg by viewModel.errorMsg.collectAsStateWithLifecycle()

    // Estados de solicitud
    val solicitudCreada by viewModel.solicitudCreada.collectAsStateWithLifecycle()
    val solicitudError by viewModel.solicitudError.collectAsStateWithLifecycle()
    val solicitudLoading by viewModel.solicitudLoading.collectAsStateWithLifecycle()

    // Datos del usuario
    val userId by userPreferences.userId.collectAsStateWithLifecycle(initialValue = null)
    val userRole by userPreferences.userRole.collectAsStateWithLifecycle(initialValue = null)

    val currentUserId = userId
    val currentUserRole = userRole

    // Dialog para confirmar solicitud
    var showSolicitarDialog by remember { mutableStateOf(false) }

    // Cargar propiedad al iniciar
    LaunchedEffect(propiedadId) {
        viewModel.cargarPropiedad(propiedadId)
    }

    // Mostrar toast de solicitud creada
    LaunchedEffect(solicitudCreada) {
        if (solicitudCreada) {
            Toast.makeText(context, "Solicitud enviada exitosamente", Toast.LENGTH_SHORT).show()
            viewModel.limpiarEstadoSolicitud()
            onNavigateToSolicitudes()
        }
    }

    // Mostrar toast de error
    LaunchedEffect(solicitudError) {
        solicitudError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarEstadoSolicitud()
        }
    }

    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(propiedad?.titulo ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            // Boton de solicitar arriendo (solo para inquilino)
            if (currentUserRole?.uppercase() == "ARRENDATARIO" && propiedad != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Precio mensual",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${numberFormat.format(propiedad?.precio_mensual ?: 0)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = { showSolicitarDialog = true },
                            enabled = !solicitudLoading,
                            modifier = Modifier.height(48.dp)
                        ) {
                            if (solicitudLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.Send, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Solicitar Arriendo")
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && propiedad == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Cargando propiedad...")
                    }
                }

                propiedad == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
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
                        Text(
                            errorMsg ?: "Propiedad no encontrada",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Volver")
                        }
                    }
                }

                else -> {
                    val prop = propiedad!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Galeria de fotos
                        if (fotos.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp),
                                contentPadding = PaddingValues(0.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                items(fotos) { foto ->
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(foto.url)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = foto.nombre,
                                        modifier = Modifier
                                            .width(if (fotos.size == 1) 400.dp else 300.dp)
                                            .height(250.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Home,
                                        contentDescription = null,
                                        modifier = Modifier.size(60.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Sin fotos",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header con codigo y tipo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        "Codigo: ${prop.codigo}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                nombreTipo?.let { tipo ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            tipo,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Titulo
                            Text(
                                prop.titulo,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(8.dp))

                            // Ubicacion
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${prop.direccion}, ${nombreComuna ?: ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            // Precio
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
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Precio mensual",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        "${numberFormat.format(prop.precio_mensual)} ${prop.divisa}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Caracteristicas
                            Text(
                                "Caracteristicas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                CaracteristicaItem(
                                    icon = Icons.Default.SquareFoot,
                                    valor = "${prop.m2.toInt()}",
                                    unidad = "m2"
                                )
                                CaracteristicaItem(
                                    icon = Icons.Default.Bed,
                                    valor = "${prop.n_habit}",
                                    unidad = "Habitaciones"
                                )
                                CaracteristicaItem(
                                    icon = Icons.Default.Bathtub,
                                    valor = "${prop.n_banos}",
                                    unidad = "Banos"
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            // Pet Friendly
                            if (prop.pet_friendly) {
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Pets,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Acepta mascotas",
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Info adicional
                            Text(
                                "Informacion adicional",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))

                            InfoRow("Direccion", prop.direccion)
                            nombreComuna?.let { InfoRow("Comuna", it) }
                            nombreTipo?.let { InfoRow("Tipo", it) }
                            InfoRow("Fotos", "${fotos.size} imagenes")

                            Spacer(Modifier.height(80.dp)) // Espacio para el bottom bar
                        }
                    }
                }
            }
        }
    }

    // Dialog de confirmacion de solicitud
    if (showSolicitarDialog) {
        AlertDialog(
            onDismissRequest = { showSolicitarDialog = false },
            icon = { Icon(Icons.Default.Home, null) },
            title = { Text("Solicitar Arriendo") },
            text = {
                Column {
                    Text("Estas a punto de enviar una solicitud de arriendo para:")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        propiedad?.titulo ?: "",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${numberFormat.format(propiedad?.precio_mensual ?: 0)}/mes",
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "El propietario revisara tu solicitud y documentos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSolicitarDialog = false
                        currentUserId?.let { uid ->
                            viewModel.crearSolicitud(uid, propiedadId)
                        }
                    }
                ) {
                    Text("Confirmar Solicitud")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSolicitarDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun CaracteristicaItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valor: String,
    unidad: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            valor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            unidad,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}