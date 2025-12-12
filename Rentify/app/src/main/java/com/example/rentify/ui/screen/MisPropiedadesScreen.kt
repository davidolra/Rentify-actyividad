package com.example.rentify.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.ui.viewmodel.MisPropiedadesViewModel
import com.example.rentify.ui.viewmodel.MisPropiedadesViewModelFactory
import com.example.rentify.ui.viewmodel.PropiedadConInfo
import java.text.NumberFormat
import java.util.*

/**
 * Pantalla de propiedades del propietario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPropiedadesScreen(
    userPreferences: UserPreferences,
    viewModelFactory: MisPropiedadesViewModelFactory,
    onNavigateToAgregar: () -> Unit,
    onNavigateToDetalle: (Long) -> Unit,
    onNavigateToEditar: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: MisPropiedadesViewModel = viewModel(factory = viewModelFactory)

    // Estados
    val propiedades by viewModel.propiedades.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMsg by viewModel.errorMsg.collectAsStateWithLifecycle()
    val successMsg by viewModel.successMsg.collectAsStateWithLifecycle()

    // Datos del usuario
    val userId by userPreferences.userId.collectAsStateWithLifecycle(initialValue = null)

    // Dialog de confirmacion de eliminacion
    var showDeleteDialog by remember { mutableStateOf(false) }
    var propiedadParaEliminar by remember { mutableStateOf<PropiedadConInfo?>(null) }

    // Cargar propiedades al iniciar
    LaunchedEffect(userId) {
        userId?.let {
            viewModel.cargarPropiedadesPropietario(it)
        }
    }

    // Mostrar mensajes
    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarMensajes()
        }
    }

    LaunchedEffect(successMsg) {
        successMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Propiedades") },
                actions = {
                    IconButton(
                        onClick = {
                            userId?.let { viewModel.cargarPropiedadesPropietario(it) }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, "Actualizar")
                    }
                    IconButton(onClick = onNavigateToAgregar) {
                        Icon(Icons.Default.Add, "Agregar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAgregar) {
                Icon(Icons.Default.Add, "Agregar propiedad")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && propiedades.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Cargando propiedades...")
                    }
                }

                propiedades.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No tienes propiedades publicadas",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Agrega tu primera propiedad para comenzar",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onNavigateToAgregar) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar Propiedad")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                "${propiedades.size} propiedades",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        items(
                            items = propiedades,
                            key = { it.propiedad.id }
                        ) { propiedadConInfo ->
                            PropiedadPropietarioCard(
                                propiedadConInfo = propiedadConInfo,
                                onVerDetalle = { onNavigateToDetalle(propiedadConInfo.propiedad.id) },
                                onEditar = { onNavigateToEditar(propiedadConInfo.propiedad.id) },
                                onEliminar = {
                                    propiedadParaEliminar = propiedadConInfo
                                    showDeleteDialog = true
                                }
                            )
                        }

                        item {
                            Spacer(Modifier.height(80.dp)) // Espacio para FAB
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

    // Dialog de confirmacion de eliminacion
    if (showDeleteDialog && propiedadParaEliminar != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar Propiedad") },
            text = {
                Column {
                    Text("Esta seguro de eliminar esta propiedad?")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        propiedadParaEliminar?.propiedad?.titulo ?: "",
                        fontWeight = FontWeight.Bold
                    )
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
                    onClick = {
                        userId?.let { uid ->
                            propiedadParaEliminar?.propiedad?.id?.let { propId ->
                                viewModel.eliminarPropiedad(propId, uid)
                            }
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Card de propiedad para propietario
 */
@Composable
private fun PropiedadPropietarioCard(
    propiedadConInfo: PropiedadConInfo,
    onVerDetalle: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val propiedad = propiedadConInfo.propiedad
    val propiedadRemota = propiedadConInfo.propiedadRemota
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onVerDetalle),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Fotos
            val fotos = propiedadRemota?.fotos
            if (!fotos.isNullOrEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(fotos.take(3)) { foto ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(foto.url)
                                .crossfade(true)
                                .build(),
                            contentDescription = foto.nombre,
                            modifier = Modifier
                                .width(if (fotos.size == 1) 400.dp else 180.dp)
                                .height(150.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        propiedad.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            propiedad.codigo,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Ubicacion
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${propiedadConInfo.nombreComuna ?: ""} - ${propiedad.direccion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Caracteristicas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoChip(Icons.Default.SquareFoot, "${propiedad.m2.toInt()} m2")
                    InfoChip(Icons.Default.Bed, "${propiedad.n_habit}")
                    InfoChip(Icons.Default.Bathtub, "${propiedad.n_banos}")
                    if (propiedad.pet_friendly) {
                        Icon(
                            Icons.Default.Pets,
                            contentDescription = "Pet friendly",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Precio y tipo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    propiedadConInfo.nombreTipo?.let { tipo ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                tipo,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Text(
                        "${numberFormat.format(propiedad.precio_mensual)}/${propiedad.divisa}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(8.dp))

                // Acciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onVerDetalle) {
                        Icon(Icons.Default.Visibility, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ver")
                    }
                    TextButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Editar")
                    }
                    TextButton(
                        onClick = onEliminar,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}