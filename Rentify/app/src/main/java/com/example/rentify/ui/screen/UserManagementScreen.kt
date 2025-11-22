package com.example.rentify.ui.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import com.example.rentify.data.local.RentifyDatabase
import com.example.rentify.data.local.entities.UsuarioEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    currentUser: UsuarioEntity?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = RentifyDatabase.getInstance(context)
    val coroutineScope = rememberCoroutineScope()

    var usuarios by remember { mutableStateOf<List<UsuarioEntity>>(emptyList()) }
    var editarUsuario by remember { mutableStateOf<UsuarioEntity?>(null) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoEmail by remember { mutableStateOf("") }

    fun cargarUsuarios() {
        coroutineScope.launch {
            usuarios = db.usuarioDao().getAll()
        }
    }

    LaunchedEffect(Unit) {
        cargarUsuarios()
    }

    if (editarUsuario != null) {
        AlertDialog(
            onDismissRequest = { editarUsuario = null },
            title = { Text("Editar Usuario") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nuevoNombre,
                        onValueChange = { nuevoNombre = it },
                        label = { Text("Nombre completo") }
                    )
                    OutlinedTextField(
                        value = nuevoEmail,
                        onValueChange = { nuevoEmail = it },
                        label = { Text("Email") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        editarUsuario?.let {
                            db.usuarioDao().update(
                                it.copy(
                                    pnombre = nuevoNombre.split(" ").firstOrNull() ?: it.pnombre,
                                    snombre = nuevoNombre.split(" ").getOrNull(1) ?: it.snombre,
                                    papellido = nuevoNombre.split(" ").getOrNull(2) ?: it.papellido,
                                    email = nuevoEmail
                                )
                            )
                        }
                        editarUsuario = null
                        cargarUsuarios()
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editarUsuario = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
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
            if (usuarios.isEmpty()) {
                Text(
                    text = "No hay usuarios registrados.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(usuarios) { usuario ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* opcional */ }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${usuario.pnombre} ${usuario.snombre} ${usuario.papellido}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Email: ${usuario.email}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(8.dp))

                                // --- Solo el admin ve los botones ---
                                if (currentUser?.rol_id == 1L) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedButton(onClick = {
                                            editarUsuario = usuario
                                            nuevoNombre = "${usuario.pnombre} ${usuario.snombre} ${usuario.papellido}"
                                            nuevoEmail = usuario.email
                                        }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                                            Spacer(Modifier.width(4.dp))
                                            Text("Editar")
                                        }
                                        OutlinedButton(onClick = {
                                            coroutineScope.launch {
                                                db.usuarioDao().delete(usuario)
                                                cargarUsuarios()
                                            }
                                        }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                            Spacer(Modifier.width(4.dp))
                                            Text("Eliminar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
