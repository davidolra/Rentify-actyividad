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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rentify.data.remote.dto.ResenaDTO
import com.example.rentify.ui.viewmodel.ReviewViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla/Sección de reseñas para mostrar en detalle de propiedad
 * ✅ Integrado con Review Service
 */
@Composable
fun PropiedadResenasSection(
    propiedadId: Long,
    usuarioActualId: Long?,
    reviewViewModel: ReviewViewModel,
    modifier: Modifier = Modifier
) {
    val resenas by reviewViewModel.resenas.collectAsState()
    val promedioCalificacion by reviewViewModel.promedioCalificacion.collectAsState()
    val isLoading by reviewViewModel.isLoading.collectAsState()
    val errorMessage by reviewViewModel.errorMessage.collectAsState()
    val successMessage by reviewViewModel.successMessage.collectAsState()

    var mostrarDialogoCrearResena by remember { mutableStateOf(false) }

    // Cargar reseñas al inicio
    LaunchedEffect(propiedadId) {
        reviewViewModel.cargarResenasPorPropiedad(propiedadId)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // ==================== HEADER CON PROMEDIO ====================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Reseñas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                if (promedioCalificacion != null && promedioCalificacion!! > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Calificación",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f/10", promedioCalificacion),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${resenas.size} reseñas)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                } else {
                    Text(
                        text = "Sin reseñas aún",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            // Botón para crear reseña
            if (usuarioActualId != null) {
                Button(
                    onClick = { mostrarDialogoCrearResena = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar reseña"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reseñar")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==================== MENSAJES ====================
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = error, color = Color.Red)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        successMessage?.let { success ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Éxito",
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = success, color = Color(0xFF4CAF50))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ==================== LISTA DE RESEÑAS ====================
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (resenas.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Esta propiedad aún no tiene reseñas",
                            color = Color.Gray
                        )
                        if (usuarioActualId != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "¡Sé el primero en reseñarla!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(resenas) { resena ->
                    ResenaCard(resena = resena)
                }
            }
        }
    }

    // ==================== DIÁLOGO CREAR RESEÑA ====================
    if (mostrarDialogoCrearResena && usuarioActualId != null) {
        CrearResenaDialog(
            propiedadId = propiedadId,
            usuarioId = usuarioActualId,
            reviewViewModel = reviewViewModel,
            onDismiss = {
                mostrarDialogoCrearResena = false
                reviewViewModel.clearMessages()
            }
        )
    }
}

/**
 * Card individual de reseña
 */
@Composable
fun ResenaCard(
    resena: ResenaDTO,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Usuario y fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = resena.usuario?.let {
                            "${it.pnombre} ${it.papellido}"
                        } ?: "Usuario",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    resena.fechaResena?.let { fecha ->
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        Text(
                            text = formatter.format(fecha),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                // Puntaje con estrellas
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Calificación",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${resena.puntaje}/10",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                }
            }

            // Comentario
            if (!resena.comentario.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = resena.comentario,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }

            // Badge de estado si no está activa
            if (resena.estado != "ACTIVA") {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = when (resena.estado) {
                        "BANEADA" -> Color.Red.copy(alpha = 0.1f)
                        "OCULTA" -> Color.Gray.copy(alpha = 0.1f)
                        else -> Color.LightGray
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = resena.estado ?: "DESCONOCIDO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = when (resena.estado) {
                            "BANEADA" -> Color.Red
                            "OCULTA" -> Color.Gray
                            else -> Color.Black
                        }
                    )
                }
            }
        }
    }
}

/**
 * Diálogo para crear nueva reseña
 */
@Composable
fun CrearResenaDialog(
    propiedadId: Long,
    usuarioId: Long,
    reviewViewModel: ReviewViewModel,
    onDismiss: () -> Unit
) {
    var puntaje by remember { mutableStateOf(5) }
    var comentario by remember { mutableStateOf("") }
    var errorComentario by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Crear Reseña")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Selector de puntaje
                Text(
                    text = "Calificación: $puntaje/10",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = puntaje.toFloat(),
                    onValueChange = { puntaje = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de comentario
                OutlinedTextField(
                    value = comentario,
                    onValueChange = {
                        comentario = it
                        errorComentario = null
                    },
                    label = { Text("Comentario (opcional)") },
                    placeholder = { Text("Escribe tu experiencia con esta propiedad...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 5,
                    isError = errorComentario != null,
                    supportingText = errorComentario?.let { { Text(it) } }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validar comentario
                    val (esValido, mensajeError) = reviewViewModel.validarComentario(comentario)
                    if (!esValido) {
                        errorComentario = mensajeError
                        return@Button
                    }

                    // Crear reseña
                    reviewViewModel.crearResenaPropiedad(
                        usuarioId = usuarioId,
                        propiedadId = propiedadId,
                        puntaje = puntaje,
                        comentario = comentario.ifBlank { null },
                        tipoResenaId = 1L // RESENA_PROPIEDAD
                    )

                    onDismiss()
                }
            ) {
                Text("Publicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}