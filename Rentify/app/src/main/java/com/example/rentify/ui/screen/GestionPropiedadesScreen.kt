package com.example.rentify.ui.screen

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
import com.example.rentify.data.local.RentifyDatabase

import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

/**
 * Pantalla de Gestión de Propiedades para ADMIN
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionPropiedadesScreen(
    onBack: () -> Unit,
    onVerDetalle: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val db = RentifyDatabase.getInstance(context)
    val scope = rememberCoroutineScope()

    var propiedades by remember { mutableStateOf<List<com.example.rentify.data.local.entities.PropiedadEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            propiedades = db.propiedadDao().getAll()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Propiedades") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
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

                    items(propiedades) { propiedad ->
                        PropiedadAdminCard(
                            propiedad = propiedad,
                            db = db,
                            onVerDetalle = { onVerDetalle(propiedad.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PropiedadAdminCard(
    propiedad: com.example.rentify.data.local.entities.PropiedadEntity,
    db: RentifyDatabase,
    onVerDetalle: () -> Unit
) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    var nombreComuna by remember { mutableStateOf("Cargando...") }
    var nombreTipo by remember { mutableStateOf("Cargando...") }
    var nombreEstado by remember { mutableStateOf("Cargando...") }

    LaunchedEffect(propiedad.id) {
        nombreComuna = db.catalogDao().getComunaById(propiedad.comuna_id)?.nombre ?: "N/A"
        nombreTipo = db.catalogDao().getTipoById(propiedad.tipo_id)?.nombre ?: "N/A"
        nombreEstado = db.catalogDao().getEstadoById(propiedad.estado_id)?.nombre ?: "N/A"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(nombreComuna, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tipo: $nombreTipo", style = MaterialTheme.typography.bodySmall)
                Text("Estado: $nombreEstado", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

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

                Button(onClick = onVerDetalle) {
                    Icon(Icons.Filled.Visibility, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ver")
                }
            }
        }
    }
}