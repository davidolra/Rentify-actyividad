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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rentify.data.remote.dto.UsuarioRemoteDTO
import com.example.rentify.ui.viewmodel.GestionUsuariosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    vm: GestionUsuariosViewModel,
    currentUserRol: Long?,
    onBack: () -> Unit
) {
    val users by vm.users.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()

    var editingUser by remember { mutableStateOf<UsuarioRemoteDTO?>(null) }
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.loadUsers()
    }

    if (editingUser != null) {
        AlertDialog(
            onDismissRequest = { editingUser = null },
            title = { Text("Editar Usuario") },
            text = {
                Column {
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("Nombre completo") }
                    )
                    OutlinedTextField(
                        value = userEmail,
                        onValueChange = { userEmail = it },
                        label = { Text("Email") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    editingUser?.let {
                        val updatedUser = it.copy(
                            pnombre = userName.split(" ").getOrElse(0) { "" },
                            snombre = userName.split(" ").getOrElse(1) { "" },
                            papellido = userName.split(" ").getOrElse(2) { "" },
                            email = userEmail
                        )
                        vm.updateUser(it.id!!, updatedUser)
                    }
                    editingUser = null
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingUser = null }) {
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
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (users.isEmpty()) {
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
                    items(users) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* opcional */ }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${user.pnombre} ${user.snombre} ${user.papellido}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Email: ${user.email}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(8.dp))

                                if (currentUserRol == 1L) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedButton(onClick = {
                                            editingUser = user
                                            userName = "${user.pnombre} ${user.snombre} ${user.papellido}"
                                            userEmail = user.email
                                        }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                                            Spacer(Modifier.width(4.dp))
                                            Text("Editar")
                                        }
                                        OutlinedButton(onClick = { vm.deleteUser(user.id!!) }) {
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
