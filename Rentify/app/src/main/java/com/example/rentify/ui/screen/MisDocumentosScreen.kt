package com.example.rentify.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.data.remote.dto.DocumentoRemoteDTO
import com.example.rentify.ui.viewmodel.MisDocumentosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisDocumentosScreen(
    viewModel: MisDocumentosViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val userId by userPrefs.userId.collectAsStateWithLifecycle(initialValue = null)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showResubirDialog by remember { mutableStateOf(false) }
    var documentoAResubir by remember { mutableStateOf<DocumentoRemoteDTO?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { _ ->
            documentoAResubir?.let { doc ->
                userId?.let { uid ->
                    viewModel.resubirDocumentoRechazado(
                        documentoRechazadoId = doc.id ?: 0L,
                        usuarioId = uid,
                        tipoDocId = doc.tipoDocId ?: 1L,
                        nombreArchivo = "${doc.tipoDocNombre}_${System.currentTimeMillis()}"
                    )
                }
            }
            showResubirDialog = false
            documentoAResubir = null
        }
    }

    LaunchedEffect(userId) {
        userId?.let { viewModel.cargarMisDocumentos(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Documentos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { userId?.let { viewModel.cargarMisDocumentos(it) } }) {
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
            if (uiState.documentos.isNotEmpty()) {
                MisDocumentosResumen(
                    total = uiState.documentos.size,
                    aprobados = uiState.documentos.count { it.estadoId == 2L },
                    pendientes = uiState.documentos.count { it.estadoId == 1L || it.estadoId == 4L },
                    rechazados = uiState.documentos.count { it.estadoId == 3L }
                )
            }

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
                    MisDocumentosErrorState(
                        message = uiState.error!!,
                        onRetry = { userId?.let { viewModel.cargarMisDocumentos(it) } }
                    )
                }
                uiState.documentos.isEmpty() -> {
                    MisDocumentosEmptyState()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.documentos,
                            key = { it.id ?: 0 }
                        ) { documento ->
                            DocumentoUsuarioCard(
                                documento = documento,
                                onResubir = {
                                    documentoAResubir = documento
                                    showResubirDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showResubirDialog && documentoAResubir != null) {
            AlertDialog(
                onDismissRequest = {
                    showResubirDialog = false
                    documentoAResubir = null
                },
                icon = {
                    Icon(
                        Icons.Default.Upload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = { Text("Resubir Documento") },
                text = {
                    Column {
                        Text("Vas a subir un nuevo documento de tipo:")
                        Spacer(Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = documentoAResubir?.tipoDocNombre ?: "Documento",
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "El documento rechazado sera eliminado y reemplazado por el nuevo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { filePickerLauncher.launch("*/*") }) {
                        Text("Seleccionar Archivo")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showResubirDialog = false
                            documentoAResubir = null
                        }
                    ) {
                        Text("Cancelar")
                    }
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
private fun MisDocumentosResumen(
    total: Int,
    aprobados: Int,
    pendientes: Int,
    rechazados: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MisDocumentosEstadisticaItem(
                valor = total,
                label = "Total",
                color = MaterialTheme.colorScheme.primary
            )
            MisDocumentosEstadisticaItem(
                valor = aprobados,
                label = "Aprobados",
                color = Color(0xFF4CAF50)
            )
            MisDocumentosEstadisticaItem(
                valor = pendientes,
                label = "Pendientes",
                color = Color(0xFFFFA000)
            )
            MisDocumentosEstadisticaItem(
                valor = rechazados,
                label = "Rechazados",
                color = Color(0xFFF44336)
            )
        }
    }
}

@Composable
private fun MisDocumentosEstadisticaItem(
    valor: Int,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$valor",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DocumentoUsuarioCard(
    documento: DocumentoRemoteDTO,
    onResubir: () -> Unit
) {
    val estadoNombre = getEstadoNombreUsuario(documento.estadoId)
    val estadoColor = getEstadoColorUsuario(documento.estadoId)
    val estadoIcon = getEstadoIconUsuario(documento.estadoId)
    val esRechazado = documento.estadoId == 3L

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
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
                            imageVector = getIconForTipoDocUsuario(documento.tipoDocId),
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
                            text = documento.nombre ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Badge de estado
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

            documento.fechaSubido?.let { fecha ->
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Subido: ${formatearFechaUsuario(fecha)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (esRechazado) {
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFFF44336)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Motivo del rechazo:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF44336)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = documento.observaciones?.takeIf { it.isNotBlank() }
                                ?: "El administrador no especifico un motivo. Por favor, revisa que el documento sea legible y este vigente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onResubir,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Subir nuevo documento")
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = when (documento.estadoId) {
                        1L -> "Tu documento esta pendiente de revision."
                        2L -> "Tu documento ha sido aprobado."
                        4L -> "Tu documento esta siendo revisado por un administrador."
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = estadoColor
                )
            }
        }
    }
}

@Composable
private fun MisDocumentosEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No tienes documentos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Los documentos que subas apareceran aqui",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun MisDocumentosErrorState(
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

// ==================== HELPER FUNCTIONS ====================

private fun getEstadoNombreUsuario(estadoId: Long?): String {
    return when (estadoId) {
        1L -> "Pendiente"
        2L -> "Aprobado"
        3L -> "Rechazado"
        4L -> "En revision"
        else -> "Desconocido"
    }
}

private fun getEstadoColorUsuario(estadoId: Long?): Color {
    return when (estadoId) {
        1L -> Color(0xFFFFA000)
        2L -> Color(0xFF4CAF50)
        3L -> Color(0xFFF44336)
        4L -> Color(0xFF2196F3)
        else -> Color.Gray
    }
}

private fun getEstadoIconUsuario(estadoId: Long?): ImageVector {
    return when (estadoId) {
        1L -> Icons.Default.Schedule
        2L -> Icons.Default.CheckCircle
        3L -> Icons.Default.Cancel
        4L -> Icons.Default.Visibility
        else -> Icons.Default.Help
    }
}

private fun getIconForTipoDocUsuario(tipoDocId: Long?): ImageVector {
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

private fun formatearFechaUsuario(fecha: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(fecha)
}