package com.example.rentify.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rentify.data.remote.dto.UsuarioDTO

/**
 * Pantalla para la gestion de usuarios por parte de un administrador.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    users: List<UsuarioDTO>,
    isLoading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onUpdateUser: (UsuarioDTO) -> Unit,
    onDeleteUser: (UsuarioDTO) -> Unit,
    onRetry: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<UsuarioDTO?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestion de Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
            } else if (error != null) {
                ErrorState(message = error, onRetry = onRetry)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(users, key = { it.id!! }) { user ->
                        UserItem(
                            user = user,
                            onEditClick = {
                                selectedUser = user
                                showEditDialog = true
                            },
                            onDeleteClick = {
                                selectedUser = user
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogo para editar usuario
    if (showEditDialog && selectedUser != null) {
        EditUserDialog(
            user = selectedUser!!,
            onDismiss = { showEditDialog = false },
            onConfirm = {
                onUpdateUser(it)
                showEditDialog = false
            }
        )
    }

    // Dialogo para confirmar eliminacion
    if (showDeleteDialog && selectedUser != null) {
        val userName = "${selectedUser!!.pnombre} ${selectedUser!!.papellido}"
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar Eliminacion") },
            text = { Text("Estas seguro de que quieres eliminar a $userName?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(selectedUser!!)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun UserItem(
    user: UsuarioDTO,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val fullName = "${user.pnombre} ${user.papellido}"
    val rolNombre = user.rol?.nombre ?: when(user.rolId) {
        1 -> "ADMIN"
        2 -> "PROPIETARIO"
        3 -> "ARRIENDATARIO"
        else -> "N/A"
    }
    val estadoNombre = user.estado?.nombre ?: when(user.estadoId) {
        1 -> "ACTIVO"
        2 -> "INACTIVO"
        3 -> "SUSPENDIDO"
        else -> "N/A"
    }

    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.0f)) {
                Text(fullName, fontWeight = FontWeight.Bold)
                Text(user.email, style = MaterialTheme.typography.bodyMedium)
                Text("Rol: $rolNombre", style = MaterialTheme.typography.bodySmall)
                Text("Estado: $estadoNombre", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditUserDialog(
    user: UsuarioDTO,
    onDismiss: () -> Unit,
    onConfirm: (UsuarioDTO) -> Unit
) {
    var pnombre by remember { mutableStateOf(user.pnombre) }
    var snombre by remember { mutableStateOf(user.snombre) }
    var papellido by remember { mutableStateOf(user.papellido) }
    var email by remember { mutableStateOf(user.email) }
    var ntelefono by remember { mutableStateOf(user.ntelefono ?: "") }
    var selectedRolId by remember { mutableStateOf(user.rolId ?: user.rol?.id ?: 3) }
    var selectedEstadoId by remember { mutableStateOf(user.estadoId ?: user.estado?.id ?: 1) }

    var rolExpanded by remember { mutableStateOf(false) }
    var estadoExpanded by remember { mutableStateOf(false) }

    val roles = listOf(
        1 to "ADMIN",
        2 to "PROPIETARIO",
        3 to "ARRIENDATARIO"
    )

    val estados = listOf(
        1 to "ACTIVO",
        2 to "INACTIVO",
        3 to "SUSPENDIDO"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Usuario") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = pnombre,
                    onValueChange = { pnombre = it },
                    label = { Text("Primer Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = snombre,
                    onValueChange = { snombre = it },
                    label = { Text("Segundo Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = papellido,
                    onValueChange = { papellido = it },
                    label = { Text("Primer Apellido") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electronico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = ntelefono,
                    onValueChange = { ntelefono = it },
                    label = { Text("Telefono") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Selector de Rol
                ExposedDropdownMenuBox(
                    expanded = rolExpanded,
                    onExpandedChange = { rolExpanded = !rolExpanded }
                ) {
                    OutlinedTextField(
                        value = roles.find { it.first == selectedRolId }?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rolExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = rolExpanded,
                        onDismissRequest = { rolExpanded = false }
                    ) {
                        roles.forEach { (id, nombre) ->
                            DropdownMenuItem(
                                text = { Text(nombre) },
                                onClick = {
                                    selectedRolId = id
                                    rolExpanded = false
                                }
                            )
                        }
                    }
                }

                // Selector de Estado
                ExposedDropdownMenuBox(
                    expanded = estadoExpanded,
                    onExpandedChange = { estadoExpanded = !estadoExpanded }
                ) {
                    OutlinedTextField(
                        value = estados.find { it.first == selectedEstadoId }?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = estadoExpanded,
                        onDismissRequest = { estadoExpanded = false }
                    ) {
                        estados.forEach { (id, nombre) ->
                            DropdownMenuItem(
                                text = { Text(nombre) },
                                onClick = {
                                    selectedEstadoId = id
                                    estadoExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedUser = user.copy(
                    pnombre = pnombre,
                    snombre = snombre,
                    papellido = papellido,
                    email = email,
                    ntelefono = ntelefono,
                    rolId = selectedRolId,
                    estadoId = selectedEstadoId
                )
                onConfirm(updatedUser)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}