package com.example.rentify.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.rentify.data.local.database.RentifyDatabase
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.ui.viewmodel.MisPropiedadesViewModel
import com.example.rentify.ui.viewmodel.MisPropiedadesViewModelFactory
import java.text.NumberFormat
import java.util.*

/**
 * Pantalla para que el PROPIETARIO vea sus propiedades publicadas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPropiedadesScreen(
    onBack: () -> Unit,
    onAgregarPropiedad: () -> Unit,
    onEditarPropiedad: (Long) -> Unit = {},
    onVerDetalle: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val userId by userPrefs.userId.collectAsStateWithLifecycle(initialValue = null)

    // ViewModel
    val db = RentifyDatabase.getInstance(context)
    val vm: MisPropiedadesViewModel = viewModel(
        factory = MisPropiedadesViewModelFactory(
            db.propiedadDao(),
            db.catalogDao()
        )
    )

    val propiedades by vm.propiedades.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()

    // Estado para confirmar eliminación
    var propiedadAEliminar by remember { mutableStateOf<Long?>(null) }

    // Cargar propiedades al iniciar
    LaunchedEffect(userId) {
        userId?.let { vm.cargarPropiedadesPropietario(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Propiedades") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onAgregarPropiedad) {
                        Icon(Icons.Filled.Add, "Agregar Propiedad")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarPropiedad,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, "Agregar Propiedad")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                propiedades.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Business,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No tienes propiedades publicadas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Explora propiedades y solicita tu arriendo ideal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onAgregarPropiedad) {
                            Icon(Icons.Filled.Add, null)
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
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Total de propiedades: ${propiedades.size}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        items(propiedades) { item ->
                            PropiedadPropietarioCard(
                                propiedadInfo = item,
                                onVerDetalle = { onVerDetalle(item.propiedad.id) },
                                onEditar = { onEditarPropiedad(item.propiedad.id) },
                                onEliminar = { propiedadAEliminar = item.propiedad.id }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación de eliminación
    if (propiedadAEliminar != null) {
        AlertDialog(
            onDismissRequest = { propiedadAEliminar = null },
            icon = { Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar esta propiedad? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        val id = propiedadAEliminar!!
                        propiedadAEliminar = null

                        vm.eliminarPropiedad(id) { success ->
                            if (success) {
                                Toast.makeText(context, "Propiedad eliminada", Toast.LENGTH_SHORT).show()
                                userId?.let { vm.cargarPropiedadesPropietario(it) }
                            } else {
                                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { propiedadAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PropiedadPropietarioCard(
    propiedadInfo: com.example.rentify.ui.viewmodel.PropiedadConInfo,
    onVerDetalle: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    val propiedad = propiedadInfo.propiedad

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
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
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        propiedad.codigo,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Ubicación
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    propiedadInfo.nombreComuna ?: "Comuna desconocida",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(12.dp))

            // Características
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(Icons.Filled.SquareFoot, "${propiedad.m2} m²")
                InfoChip(Icons.Filled.Bed, "${propiedad.n_habit} hab")
                InfoChip(Icons.Filled.Bathroom, "${propiedad.n_banos} baños")
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            // Precio y acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    numberFormat.format(propiedad.precio_mensual),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row {
                    IconButton(onClick = onVerDetalle) {
                        Icon(Icons.Filled.Visibility, "Ver", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Filled.Edit, "Editar", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall)
        }
    }
}