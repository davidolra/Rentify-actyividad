package com.example.rentify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.rentify.data.repository.PropertyRemoteRepository
import com.example.rentify.navigation.Routes
import com.example.rentify.ui.viewmodel.PropiedadViewModel
import com.example.rentify.ui.viewmodel.PropiedadViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoPropiedadesScreen(
    navController: NavController,
    context: android.content.Context
) {
    val db = RentifyDatabase.getInstance(context)
    val viewModel: PropiedadViewModel = viewModel(
        factory = PropiedadViewModelFactory(
            propiedadDao = db.propiedadDao(),
            catalogDao = db.catalogDao(),
            remoteRepository = PropertyRemoteRepository()
        )
    )

    val propiedades by viewModel.propiedades.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarPropiedadesCercanas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Propiedades Disponibles") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Filtros */ }) {
                        Icon(Icons.Default.FilterList, "Filtros")
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                propiedades.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay propiedades disponibles",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(propiedades) { propiedadConDistancia ->
                            PropiedadCard(
                                propiedad = propiedadConDistancia.propiedad,
                                distancia = propiedadConDistancia.distancia,
                                nombreComuna = propiedadConDistancia.nombreComuna,
                                nombreTipo = propiedadConDistancia.nombreTipo,
                                onClick = {
                                    navController.navigate(
                                        "${Routes.PROPIEDAD_DETALLE}/${propiedadConDistancia.propiedad.id}"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropiedadCard(
    propiedad: com.example.rentify.data.local.entities.PropiedadEntity,
    distancia: Double?,
    nombreComuna: String?,
    nombreTipo: String?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = propiedad.direccion,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nombreComuna ?: "Comuna desconocida",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (distancia != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = String.format("%.1f km", distancia),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PropertyFeature(
                    icon = Icons.Default.Home,
                    text = nombreTipo ?: "Tipo"
                )
                PropertyFeature(
                    icon = Icons.Default.Bed,
                    text = "${propiedad.nHabit} hab"
                )
                PropertyFeature(
                    icon = Icons.Default.Bathtub,
                    text = "${propiedad.nBanos} baños"
                )
                if (propiedad.petFriendly == 1) {
                    PropertyFeature(
                        icon = Icons.Default.Pets,
                        text = "Pet friendly"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$${String.format("%,.0f", propiedad.precioMensual)} / mes",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PropertyFeature(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}