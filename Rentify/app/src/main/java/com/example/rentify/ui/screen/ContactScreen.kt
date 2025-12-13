package com.example.rentify.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rentify.ui.viewmodel.ContactViewModel

/**
 * Pantalla de Contacto
 * Permite a usuarios enviar mensajes de contacto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    contactViewModel: ContactViewModel,
    usuarioId: Long? = null,
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var asunto by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var numeroTelefono by remember { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorEmail by remember { mutableStateOf<String?>(null) }
    var errorAsunto by remember { mutableStateOf<String?>(null) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    val isLoading by contactViewModel.isLoading.collectAsState()
    val errorMessage by contactViewModel.errorMessage.collectAsState()
    val successMessage by contactViewModel.successMessage.collectAsState()

    val scrollState = rememberScrollState()

    // Limpiar formulario después de éxito
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            nombre = ""
            email = ""
            asunto = ""
            mensaje = ""
            numeroTelefono = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contáctanos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "¿Tienes alguna consulta?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Escríbenos y te responderemos pronto",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Mensajes de error/éxito
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
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Éxito",
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = success, color = Color(0xFF4CAF50))
                    }
                }
            }

            // Formulario
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nombre
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            errorNombre = if (it.isBlank()) "El nombre es obligatorio" else null
                        },
                        label = { Text("Nombre completo *") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorNombre != null,
                        supportingText = errorNombre?.let { { Text(it) } }
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            val (isValid, error) = contactViewModel.validarEmail(it)
                            errorEmail = error
                        },
                        label = { Text("Email *") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorEmail != null,
                        supportingText = errorEmail?.let { { Text(it) } }
                    )

                    // Teléfono (opcional)
                    OutlinedTextField(
                        value = numeroTelefono,
                        onValueChange = { numeroTelefono = it },
                        label = { Text("Teléfono (opcional)") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Asunto
                    OutlinedTextField(
                        value = asunto,
                        onValueChange = {
                            asunto = it
                            val (isValid, error) = contactViewModel.validarAsunto(it)
                            errorAsunto = error
                        },
                        label = { Text("Asunto *") },
                        leadingIcon = {
                            Icon(Icons.Default.Info, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorAsunto != null,
                        supportingText = errorAsunto?.let { { Text(it) } }
                    )

                    // Mensaje
                    OutlinedTextField(
                        value = mensaje,
                        onValueChange = {
                            mensaje = it
                            val (isValid, error) = contactViewModel.validarMensaje(it)
                            errorMensaje = error
                        },
                        label = { Text("Mensaje *") },
                        placeholder = { Text("Escribe tu consulta aquí...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        maxLines = 10,
                        isError = errorMensaje != null,
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = errorMensaje ?: "",
                                    color = if (errorMensaje != null) Color.Red else Color.Gray
                                )
                                Text(
                                    text = "${mensaje.length}/5000",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    )

                    Text(
                        text = "* Campos obligatorios",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Botón enviar
            Button(
                onClick = {
                    // Validar todo
                    var hasErrors = false

                    if (nombre.isBlank()) {
                        errorNombre = "El nombre es obligatorio"
                        hasErrors = true
                    }

                    val (emailValid, emailError) = contactViewModel.validarEmail(email)
                    if (!emailValid) {
                        errorEmail = emailError
                        hasErrors = true
                    }

                    val (asuntoValid, asuntoError) = contactViewModel.validarAsunto(asunto)
                    if (!asuntoValid) {
                        errorAsunto = asuntoError
                        hasErrors = true
                    }

                    val (mensajeValid, mensajeError) = contactViewModel.validarMensaje(mensaje)
                    if (!mensajeValid) {
                        errorMensaje = mensajeError
                        hasErrors = true
                    }

                    if (!hasErrors) {
                        contactViewModel.crearMensaje(
                            nombre = nombre,
                            email = email,
                            asunto = asunto,
                            mensaje = mensaje,
                            numeroTelefono = numeroTelefono.ifBlank { null },
                            usuarioId = usuarioId
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar Mensaje")
                }
            }

            // Info adicional
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Otros medios de contacto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("support@rentify.com", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+56 9 1234 5678", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lun - Vie: 9:00 - 18:00", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}