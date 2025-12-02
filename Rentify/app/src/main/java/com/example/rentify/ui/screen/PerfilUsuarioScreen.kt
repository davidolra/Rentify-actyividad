package com.example.rentify.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.ui.viewmodel.PerfilUsuarioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioScreen(
    vm: PerfilUsuarioViewModel,
    onBack: () -> Unit,
    onVerSolicitudes: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userPrefs = remember { UserPreferences(context) }

    val userId by userPrefs.userId.collectAsStateWithLifecycle(initialValue = null)
    val usuario by vm.usuario.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val updateSuccess by vm.updateSuccess.collectAsStateWithLifecycle()

    // Cargar datos al iniciar
    LaunchedEffect(userId) {
        userId?.let { vm.cargarDatosUsuario(it) }
    }

    // Mostrar mensaje de exito
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            Toast.makeText(context, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
            vm.clearUpdateSuccess()
        }
    }

    // Mostrar mensaje de error
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            vm.clearError()
        }
    }

    val scrollState = rememberScrollState()

    // Estados editables
    var pnombre by rememberSaveable { mutableStateOf("") }
    var snombre by rememberSaveable { mutableStateOf("") }
    var papellido by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var isEditing by rememberSaveable { mutableStateOf(false) }

    // Inicializar campos editables cuando se cargue usuario
    LaunchedEffect(usuario) {
        usuario?.let {
            pnombre = it.pnombre
            snombre = it.snombre
            papellido = it.papellido
            telefono = it.ntelefono ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                userPrefs.clearUserSession()
                                onLogout()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Filled.Logout,
                            contentDescription = "Cerrar sesion",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                usuario == null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No se pudo cargar el perfil",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { userId?.let { vm.cargarDatosUsuario(it) } }) {
                            Text("Reintentar")
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp)
                    ) {
                        // Header con avatar
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Avatar
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${usuario!!.pnombre.firstOrNull() ?: ""}${usuario!!.papellido.firstOrNull() ?: ""}",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                // Nombre completo
                                Text(
                                    text = "${usuario!!.pnombre} ${usuario!!.snombre} ${usuario!!.papellido}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                // Rol
                                usuario!!.rol?.let { rol ->
                                    Spacer(Modifier.height(4.dp))
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(rol.nombre) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.Badge,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                }

                                // Badge DUOC VIP
                                if (usuario!!.duocVip == true) {
                                    Spacer(Modifier.height(8.dp))
                                    AssistChip(
                                        onClick = { },
                                        label = { Text("DUOC VIP - 20% descuento") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.Star,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Informacion de contacto (no editable)
                        Text(
                            "Informacion de Contacto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(12.dp))

                        // Email (solo lectura)
                        OutlinedTextField(
                            value = usuario!!.email,
                            onValueChange = { },
                            label = { Text("Correo electronico") },
                            leadingIcon = { Icon(Icons.Filled.Email, null) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = false
                        )

                        Spacer(Modifier.height(24.dp))

                        // Datos editables
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Datos Personales",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { isEditing = !isEditing }) {
                                Text(if (isEditing) "Cancelar" else "Editar")
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = pnombre,
                            onValueChange = { pnombre = it },
                            label = { Text("Primer nombre") },
                            leadingIcon = { Icon(Icons.Filled.Person, null) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = !isEditing,
                            singleLine = true
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = snombre,
                            onValueChange = { snombre = it },
                            label = { Text("Segundo nombre") },
                            leadingIcon = { Icon(Icons.Filled.Person, null) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = !isEditing,
                            singleLine = true
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = papellido,
                            onValueChange = { papellido = it },
                            label = { Text("Apellido") },
                            leadingIcon = { Icon(Icons.Filled.Person, null) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = !isEditing,
                            singleLine = true
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text("Telefono") },
                            leadingIcon = { Icon(Icons.Filled.Phone, null) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = !isEditing,
                            singleLine = true
                        )

                        if (isEditing) {
                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    vm.actualizarPerfil(
                                        pnombre = pnombre,
                                        snombre = snombre,
                                        papellido = papellido,
                                        telefono = telefono
                                    )
                                    isEditing = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Guardar cambios")
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}