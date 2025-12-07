package com.example.rentify.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rentify.data.remote.dto.DocumentoRemoteDTO
import com.example.rentify.ui.viewmodel.GestionDocumentosViewModel
import com.example.rentify.ui.viewmodel.FiltroEstadoDocumento

/**
 * Pantalla de gestión de documentos para administradores.
 * Permite aprobar, rechazar y eliminar documentos de usuarios.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionDocumentosScreen(
    viewModel: GestionDocumentosViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Diálogo de confirmación para acciones
    var showDialog by remember { mutableStateOf(false) }
    var dialogAction by remember { mutableStateOf<AccionDocumento?>(null) }
    var documentoSeleccionado by remember { mutableStateOf<DocumentoRemoteDTO?>(null) }
    var motivoTexto by remember { mutableStateOf("") }

    // Cargar documentos al iniciar
    LaunchedEffect(Unit) {
        viewModel.cargarDocumentos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Documentos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarDocumentos() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filtros por estado
            FiltrosEstado(
                filtroActual = uiState.filtroEstado,
                contadores = uiState.contadores,
                onFiltroChange = viewModel::cambiarFiltro
            )

            // Contenido principal
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error!!,
                        onRetry = { viewModel.cargarDocumentos() }
                    )
                }
                uiState.documentosFiltrados.isEmpty() -> {
                    EmptyState(filtro = uiState.filtroEstado)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.documentosFiltrados,
                            key = { it.id ?: 0 }
                        ) { documento ->
                            DocumentoAdminCard(
                                documento = documento,
                                onAprobar = {
                                    documentoSeleccionado = documento
                                    dialogAction = AccionDocumento.APROBAR
                                    motivoTexto = ""
                                    showDialog = true
                                },
                                onRechazar = {
                                    documentoSeleccionado = documento
                                    dialogAction = AccionDocumento.RECHAZAR
                                    motivoTexto = ""
                                    showDialog = true
                                },
                                onEliminar = {
                                    documentoSeleccionado = documento
                                    dialogAction = AccionDocumento.ELIMINAR
                                    motivoTexto = ""
                                    showDialog = true
                                },
                                onMarcarEnRevision = {
                                    documentoSeleccionado = documento
                                    dialogAction = AccionDocumento.EN_REVISION
                                    motivoTexto = ""
                                    showDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // Diálogo de confirmación
        if (showDialog && documentoSeleccionado != null && dialogAction != null) {
            AccionDocumentoDialog(
                documento = documentoSeleccionado!!,
                accion = dialogAction!!,
                motivo = motivoTexto,
                onMotivoChange = { motivoTexto = it },
                isLoading = uiState.isProcessing,
                onConfirm = {
                    when (dialogAction) {
                        AccionDocumento.APROBAR -> {
                            viewModel.aprobarDocumento(documentoSeleccionado!!.id!!)
                        }
                        AccionDocumento.RECHAZAR -> {
                            viewModel.rechazarDocumento(documentoSeleccionado!!.id!!, motivoTexto)
                        }
                        AccionDocumento.ELIMINAR -> {
                            viewModel.eliminarDocumento(documentoSeleccionado!!.id!!, motivoTexto)
                        }
                        AccionDocumento.EN_REVISION -> {
                            viewModel.marcarEnRevision(documentoSeleccionado!!.id!!)
                        }
                        null -> {}
                    }
                    showDialog = false
                    documentoSeleccionado = null
                    dialogAction = null
                    motivoTexto = ""
                },
                onDismiss = {
                    showDialog = false
                    documentoSeleccionado = null
                    dialogAction = null
                    motivoTexto = ""
                }
            )
        }

        // Snackbar para mensajes
        uiState.mensaje?.let { mensaje ->
            LaunchedEffect(mensaje) {
                kotlinx.coroutines.delay(3000)
                viewModel.limpiarMensaje()
            }
        }
    }

    // Snackbar host
    if (uiState.mensaje != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.limpiarMensaje() }) {
                        Text("OK")
                    }
                }
            ) {
                Text(uiState.mensaje!!)
            }
        }
    }
}

/**
 * Filtros por estado de documento.
 */
@Composable
private fun FiltrosEstado(
    filtroActual: FiltroEstadoDocumento,
    contadores: Map<FiltroEstadoDocumento, Int>,
    onFiltroChange: (FiltroEstadoDocumento) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = FiltroEstadoDocumento.entries.indexOf(filtroActual),
        modifier = Modifier.fillMaxWidth(),
        edgePadding = 16.dp
    ) {
        FiltroEstadoDocumento.entries.forEach { filtro ->
            val count = contadores[filtro] ?: 0
            Tab(
                selected = filtroActual == filtro,
                onClick = { onFiltroChange(filtro) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(filtro.displayName)
                        if (count > 0) {
                            Spacer(Modifier.width(4.dp))
                            Badge(
                                containerColor = when (filtro) {
                                    FiltroEstadoDocumento.PENDIENTE -> MaterialTheme.colorScheme.error
                                    FiltroEstadoDocumento.EN_REVISION -> MaterialTheme.colorScheme.tertiary
                                    FiltroEstadoDocumento.ACEPTADO -> MaterialTheme.colorScheme.primary
                                    FiltroEstadoDocumento.RECHAZADO -> MaterialTheme.colorScheme.error
                                    FiltroEstadoDocumento.TODOS -> MaterialTheme.colorScheme.secondary
                                }
                            ) {
                                Text("$count")
                            }
                        }
                    }
                }
            )
        }
    }
}

/**
 * Tarjeta de documento para administración.
 */
@Composable
private fun DocumentoAdminCard(
    documento: DocumentoRemoteDTO,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
    onEliminar: () -> Unit,
    onMarcarEnRevision: () -> Unit
) {
    val estadoColor = when (documento.estadoId) {
        1L -> Color(0xFFFFA000) // Pendiente - Naranja
        2L -> Color(0xFF4CAF50) // Aceptado - Verde
        3L -> Color(0xFFF44336) // Rechazado - Rojo
        4L -> Color(0xFF2196F3) // En revisión - Azul
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header con estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icono del tipo de documento
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIconForTipoDoc(documento.tipoDocId),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = documento.tipoDocNombre ?: "Documento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = documento.nombre ?: "Sin nombre",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Badge de estado
                Surface(
                    color = estadoColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = documento.estadoNombre ?: "Desconocido",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = estadoColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Información del usuario
            documento.usuario?.let { usuario ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${usuario.pnombre ?: ""} ${usuario.papellido ?: ""}".trim(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = usuario.email ?: "Sin email",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        usuario.rol?.let { rol ->
                            AssistChip(
                                onClick = {},
                                label = { Text(rol.nombre ?: "Usuario") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Badge,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Fecha de subida
            documento.fechaSubido?.let { fecha ->
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Subido: ${formatearFecha(fecha)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Botones de acción según estado actual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (documento.estadoId) {
                    1L -> { // PENDIENTE
                        // Marcar en revisión
                        OutlinedButton(
                            onClick = onMarcarEnRevision,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Revisar")
                        }
                        // Aprobar directamente
                        Button(
                            onClick = onAprobar,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Aprobar")
                        }
                    }
                    4L -> { // EN_REVISION
                        // Aprobar
                        Button(
                            onClick = onAprobar,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Aprobar")
                        }
                        // Rechazar
                        Button(
                            onClick = onRechazar,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF44336)
                            )
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Rechazar")
                        }
                    }
                    2L -> { // ACEPTADO
                        Text(
                            text = "✓ Documento aprobado",
                            color = Color(0xFF4CAF50),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    3L -> { // RECHAZADO
                        Text(
                            text = "✗ Documento rechazado",
                            color = Color(0xFFF44336),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Botón eliminar siempre visible
                IconButton(
                    onClick = onEliminar,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

/**
 * Diálogo de confirmación para acciones.
 */
@Composable
private fun AccionDocumentoDialog(
    documento: DocumentoRemoteDTO,
    accion: AccionDocumento,
    motivo: String,
    onMotivoChange: (String) -> Unit,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val requiereMotivo = accion == AccionDocumento.RECHAZAR || accion == AccionDocumento.ELIMINAR

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = {
            Icon(
                imageVector = when (accion) {
                    AccionDocumento.APROBAR -> Icons.Default.CheckCircle
                    AccionDocumento.RECHAZAR -> Icons.Default.Cancel
                    AccionDocumento.ELIMINAR -> Icons.Default.Delete
                    AccionDocumento.EN_REVISION -> Icons.Default.Visibility
                },
                contentDescription = null,
                tint = when (accion) {
                    AccionDocumento.APROBAR -> Color(0xFF4CAF50)
                    AccionDocumento.RECHAZAR -> Color(0xFFF44336)
                    AccionDocumento.ELIMINAR -> MaterialTheme.colorScheme.error
                    AccionDocumento.EN_REVISION -> Color(0xFF2196F3)
                },
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = when (accion) {
                    AccionDocumento.APROBAR -> "Aprobar Documento"
                    AccionDocumento.RECHAZAR -> "Rechazar Documento"
                    AccionDocumento.ELIMINAR -> "Eliminar Documento"
                    AccionDocumento.EN_REVISION -> "Marcar en Revisión"
                }
            )
        },
        text = {
            Column {
                Text(
                    text = when (accion) {
                        AccionDocumento.APROBAR -> "¿Confirmas que deseas aprobar este documento?"
                        AccionDocumento.RECHAZAR -> "Indica el motivo del rechazo para notificar al usuario."
                        AccionDocumento.ELIMINAR -> "Esta acción no se puede deshacer. Indica el motivo de la eliminación."
                        AccionDocumento.EN_REVISION -> "El documento será marcado como 'En Revisión'."
                    }
                )

                Spacer(Modifier.height(8.dp))

                // Info del documento
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = documento.tipoDocNombre ?: "Documento",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = documento.nombre ?: "",
                            style = MaterialTheme.typography.bodySmall
                        )
                        documento.usuario?.let { usuario ->
                            Text(
                                text = "Usuario: ${usuario.pnombre} ${usuario.papellido}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Campo de motivo si es requerido
                if (requiereMotivo) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = motivo,
                        onValueChange = onMotivoChange,
                        label = { Text("Motivo *") },
                        placeholder = {
                            Text(
                                if (accion == AccionDocumento.RECHAZAR)
                                    "Ej: Imagen borrosa, documento vencido..."
                                else
                                    "Ej: Documento duplicado, fraude detectado..."
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        isError = motivo.isBlank()
                    )
                    if (motivo.isBlank()) {
                        Text(
                            text = "El motivo es obligatorio",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading && (!requiereMotivo || motivo.isNotBlank()),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (accion) {
                        AccionDocumento.APROBAR -> Color(0xFF4CAF50)
                        AccionDocumento.RECHAZAR, AccionDocumento.ELIMINAR -> MaterialTheme.colorScheme.error
                        AccionDocumento.EN_REVISION -> Color(0xFF2196F3)
                    }
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    when (accion) {
                        AccionDocumento.APROBAR -> "Aprobar"
                        AccionDocumento.RECHAZAR -> "Rechazar"
                        AccionDocumento.ELIMINAR -> "Eliminar"
                        AccionDocumento.EN_REVISION -> "Confirmar"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Mensaje de estado vacío.
 */
@Composable
private fun EmptyState(filtro: FiltroEstadoDocumento) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = when (filtro) {
                    FiltroEstadoDocumento.PENDIENTE -> Icons.Default.Inbox
                    FiltroEstadoDocumento.EN_REVISION -> Icons.Default.Visibility
                    FiltroEstadoDocumento.ACEPTADO -> Icons.Default.CheckCircle
                    FiltroEstadoDocumento.RECHAZADO -> Icons.Default.Cancel
                    FiltroEstadoDocumento.TODOS -> Icons.Default.FolderOpen
                },
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = when (filtro) {
                    FiltroEstadoDocumento.PENDIENTE -> "No hay documentos pendientes"
                    FiltroEstadoDocumento.EN_REVISION -> "No hay documentos en revisión"
                    FiltroEstadoDocumento.ACEPTADO -> "No hay documentos aprobados"
                    FiltroEstadoDocumento.RECHAZADO -> "No hay documentos rechazados"
                    FiltroEstadoDocumento.TODOS -> "No hay documentos"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Mensaje de error.
 */
@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Reintentar")
            }
        }
    }
}

// ==================== HELPERS ====================

enum class AccionDocumento {
    APROBAR, RECHAZAR, ELIMINAR, EN_REVISION
}

private fun getIconForTipoDoc(tipoDocId: Long?): androidx.compose.ui.graphics.vector.ImageVector {
    return when (tipoDocId) {
        1L -> Icons.Default.Badge          // DNI
        2L -> Icons.Default.Flight         // Pasaporte
        3L -> Icons.Default.Payments       // Liquidación
        4L -> Icons.Default.Security       // Antecedentes
        5L -> Icons.Default.AccountBalance // AFP
        6L -> Icons.Default.Description    // Contrato
        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }
}

private fun formatearFecha(fecha: java.util.Date): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
    return sdf.format(fecha)
}
