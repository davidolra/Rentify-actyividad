package com.example.rentify.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionDocumentosScreen(
    viewModel: GestionDocumentosViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }
    var dialogAction by remember { mutableStateOf<AccionDocumento?>(null) }
    var documentoSeleccionado by remember { mutableStateOf<DocumentoRemoteDTO?>(null) }
    var motivoTexto by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.cargarDocumentos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestion de Documentos") },
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
            FiltrosEstado(
                filtroActual = uiState.filtroEstado,
                contadores = uiState.contadores,
                onFiltroChange = viewModel::cambiarFiltro
            )

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

        uiState.mensaje?.let { mensaje ->
            LaunchedEffect(mensaje) {
                kotlinx.coroutines.delay(3000)
                viewModel.limpiarMensaje()
            }
        }
    }

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
                                    FiltroEstadoDocumento.PENDIENTE -> Color(0xFFFFA000)
                                    FiltroEstadoDocumento.EN_REVISION -> Color(0xFF2196F3)
                                    FiltroEstadoDocumento.ACEPTADO -> Color(0xFF4CAF50)
                                    FiltroEstadoDocumento.RECHAZADO -> Color(0xFFF44336)
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

@Composable
private fun DocumentoAdminCard(
    documento: DocumentoRemoteDTO,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
    onEliminar: () -> Unit,
    onMarcarEnRevision: () -> Unit
) {
    val estadoInfo = getEstadoInfo(documento.estadoId)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icono y nombre
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = documento.tipoDocNombre ?: "Documento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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

                Spacer(Modifier.width(8.dp))

                // Badge de estado - CORREGIDO
                EstadoBadgeAdmin(
                    estadoNombre = estadoInfo.nombre,
                    estadoColor = estadoInfo.color,
                    estadoIcon = estadoInfo.icon
                )
            }

            Spacer(Modifier.height(12.dp))

            // Info usuario
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
                        Column(modifier = Modifier.weight(1f)) {
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
                        usuario.rol?.let { rol ->
                            AssistChip(
                                onClick = {},
                                label = { Text(rol.nombre ?: "Usuario", maxLines = 1) },
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

            // Fecha
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

            // Botones de accion
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (documento.estadoId) {
                    1L -> { // Pendiente
                        OutlinedButton(
                            onClick = onMarcarEnRevision,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Visibility, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Revisar")
                        }
                        Button(
                            onClick = onAprobar,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Aprobar")
                        }
                        IconButton(onClick = onEliminar) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    4L -> { // En revision
                        Button(
                            onClick = onAprobar,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Aprobar")
                        }
                        Button(
                            onClick = onRechazar,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                        ) {
                            Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Rechazar")
                        }
                        IconButton(onClick = onEliminar) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    else -> { // Aceptado o Rechazado
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(
                            onClick = onEliminar,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Eliminar")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Badge de estado CORREGIDO - no se corta el texto
 */
@Composable
private fun EstadoBadgeAdmin(
    estadoNombre: String,
    estadoColor: Color,
    estadoIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        color = estadoColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                estadoIcon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = estadoColor
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = estadoNombre,
                color = estadoColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

private data class EstadoInfo(
    val nombre: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private fun getEstadoInfo(estadoId: Long?): EstadoInfo {
    return when (estadoId) {
        1L -> EstadoInfo("Pendiente", Color(0xFFFFA000), Icons.Default.Schedule)
        2L -> EstadoInfo("Aprobado", Color(0xFF4CAF50), Icons.Default.CheckCircle)
        3L -> EstadoInfo("Rechazado", Color(0xFFF44336), Icons.Default.Cancel)
        4L -> EstadoInfo("En revision", Color(0xFF2196F3), Icons.Default.Visibility)
        else -> EstadoInfo("Desconocido", Color.Gray, Icons.Default.Help)
    }
}

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
                    AccionDocumento.RECHAZAR, AccionDocumento.ELIMINAR -> Color(0xFFF44336)
                    AccionDocumento.EN_REVISION -> Color(0xFF2196F3)
                }
            )
        },
        title = {
            Text(
                when (accion) {
                    AccionDocumento.APROBAR -> "Aprobar Documento"
                    AccionDocumento.RECHAZAR -> "Rechazar Documento"
                    AccionDocumento.ELIMINAR -> "Eliminar Documento"
                    AccionDocumento.EN_REVISION -> "Marcar en Revision"
                }
            )
        },
        text = {
            Column {
                Text(
                    text = when (accion) {
                        AccionDocumento.APROBAR -> "Confirmas que deseas aprobar este documento?"
                        AccionDocumento.RECHAZAR -> "Indica el motivo del rechazo. El usuario vera este mensaje."
                        AccionDocumento.ELIMINAR -> "Esta accion no se puede deshacer. Indica el motivo."
                        AccionDocumento.EN_REVISION -> "El documento sera marcado como 'En Revision'."
                    }
                )

                Spacer(Modifier.height(8.dp))

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
                                    "Ej: Documento duplicado..."
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
                        AccionDocumento.RECHAZAR, AccionDocumento.ELIMINAR -> Color(0xFFF44336)
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
                    FiltroEstadoDocumento.EN_REVISION -> "No hay documentos en revision"
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

enum class AccionDocumento {
    APROBAR, RECHAZAR, ELIMINAR, EN_REVISION
}

private fun getIconForTipoDoc(tipoDocId: Long?): androidx.compose.ui.graphics.vector.ImageVector {
    return when (tipoDocId) {
        1L -> Icons.Default.Badge
        2L -> Icons.Default.Flight
        3L -> Icons.Default.Payments
        4L -> Icons.Default.Security
        5L -> Icons.Default.AccountBalance
        6L -> Icons.Default.Description
        else -> Icons.Default.Description
    }
}

private fun formatearFecha(fecha: java.util.Date): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
    return sdf.format(fecha)
}