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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentify.data.local.RentifyDatabase
import com.example.rentify.data.local.entities.PropiedadEntity
import com.example.rentify.ui.viewmodel.PropiedadViewModel
import com.example.rentify.ui.viewmodel.PropiedadViewModelFactory
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

/**
 * Pantalla Gestión de Propiedades - Solo visible para ADMIN
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionPropiedadesScreen(
    onBack: () -> Unit,
    onVerDetalle: (Long) -> Unit = {},
    viewModelFactory: PropiedadViewModelFactory
) {
    val context = LocalContext.current
    val db = RentifyDatabase.getInstance(context)
    val scope = rememberCoroutineScope()

    // OBTENEMOS EL VIEWMODEL
    val viewModel: PropiedadViewModel = viewModel(factory = viewModelFactory)

    // Estados
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMsg by viewModel.errorMsg.collectAsStateWithLifecycle()
    val propiedades by viewModel.propiedades.collectAsStateWithLifecycle()

    // ------------------------------------------------------------------
    // CORRECCIÓN CLAVE: LEER EL ROL CON VALOR POR DEFECTO 1L
    // Esto asegura que el Administrador siempre vea los botones de gestión
    // si la lectura inicial del SharedPrefs falla.
    val prefs = context.getSharedPreferences("RentifyPrefs", 0)
    val currentRol = prefs.getLong("currentUserRolId", 1L) // Cambiado de -1L a 1L
    // ------------------------------------------------------------------

    // Al iniciar, forzamos la carga del listado del Microservicio
    LaunchedEffect(Unit) {
        viewModel.cargarPropiedadesCercanas()
    }

    val listaPropiedades = propiedades.map { it.propiedad }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Propiedades") },
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
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                errorMsg != null -> Text("Error de carga: $errorMsg", Modifier.align(Alignment.Center))
                listaPropiedades.isEmpty() -> Text("No hay propiedades para gestionar", Modifier.align(Alignment.Center))
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
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        "Total de propiedades: ${listaPropiedades.size}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        items(listaPropiedades) { propiedad ->
                            PropiedadAdminCard(
                                propiedad = propiedad,
                                db = db,
                                // La condición se evaluará a TRUE si currentRol es 1L
                                isAdmin = (currentRol == 1L),
                                onVerDetalle = { onVerDetalle(propiedad.id) },
                                onEliminarRemoto = { id ->
                                    viewModel.eliminarPropiedad(id)
                                },
                                onEditar = { id ->
                                    onVerDetalle(id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PropiedadAdminCard(
    propiedad: PropiedadEntity,
    db: RentifyDatabase,
    isAdmin: Boolean,
    onVerDetalle: () -> Unit,
    onEliminarRemoto: (Long) -> Unit, // Función de eliminación de la tarjeta
    onEditar: (Long) -> Unit
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
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            // encabezado con titulo y codigo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    propiedad.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        propiedad.codigo,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // comuna
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(nombreComuna)
            }

            Spacer(Modifier.height(8.dp))

            Text("Tipo: $nombreTipo")
            Text("Estado: $nombreEstado")

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            // PRECIO + BOTONES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    numberFormat.format(propiedad.precio_mensual),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Button(onClick = onVerDetalle) {
                    Icon(Icons.Filled.Visibility, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Ver")
                }
            }

            // SOLO EL ADMIN VE ESTOS BOTONES
            if (isAdmin) {
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = {
                        // Conecta al evento de edición (que navega al detalle)
                        onEditar(propiedad.id)
                    }) {
                        Icon(Icons.Filled.Edit, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Editar")
                    }

                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        onClick = {
                            // LLAMA A LA FUNCIÓN REMOTA PASADA POR LA PANTALLA
                            onEliminarRemoto(propiedad.id)
                        }
                    ) {
                        Icon(Icons.Filled.Delete, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}