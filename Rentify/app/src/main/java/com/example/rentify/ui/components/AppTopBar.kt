package com.example.rentify.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow

/**
 * Barra superior para Rentify con menú contextual
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    isLoggedIn: Boolean,
    userRole: String?,
    onOpenDrawer: () -> Unit,
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onPropiedades: () -> Unit,
    onPerfil: () -> Unit,
    onSolicitudes: () -> Unit,
    onMisPropiedades: () -> Unit,
    onAgregarPropiedad: () -> Unit,
    onAdminPanel: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        title = {
            Text(
                text = "Rentify",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menú")
            }
        },
        actions = {
            // Mostrar acciones rápidas según estado de autenticación
            if (isLoggedIn) {
                IconButton(onClick = onHome) {
                    Icon(Icons.Filled.Home, contentDescription = "Inicio")
                }
                IconButton(onClick = onPropiedades) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Propiedades")
                }
                IconButton(onClick = onPerfil) {
                    Icon(Icons.Filled.Person, contentDescription = "Perfil")
                }
            }

            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Más")
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                if (isLoggedIn) {
                    // Menú autenticado
                    DropdownMenuItem(
                        text = { Text("Inicio") },
                        onClick = { showMenu = false; onHome() }
                    )
                    DropdownMenuItem(
                        text = { Text("Propiedades") },
                        onClick = { showMenu = false; onPropiedades() }
                    )
                    DropdownMenuItem(
                        text = { Text("Mi Perfil") },
                        onClick = { showMenu = false; onPerfil() }
                    )

                    // Opciones específicas por rol
                    when (userRole?.uppercase()) {
                        "ADMINISTRADOR", "ADMIN" -> {
                            DropdownMenuItem(
                                text = { Text("Panel Admin") },
                                onClick = { showMenu = false; onAdminPanel() }
                            )
                        }
                        "PROPIETARIO" -> {
                            DropdownMenuItem(
                                text = { Text("Mis Propiedades") },
                                onClick = { showMenu = false; onMisPropiedades() }
                            )
                            DropdownMenuItem(
                                text = { Text("Agregar Propiedad") },
                                onClick = { showMenu = false; onAgregarPropiedad() }
                            )
                        }
                        "INQUILINO", "ARRIENDATARIO" -> {
                            DropdownMenuItem(
                                text = { Text("Mis Solicitudes") },
                                onClick = { showMenu = false; onSolicitudes() }
                            )
                        }
                    }
                } else {
                    // Menú no autenticado
                    DropdownMenuItem(
                        text = { Text("Bienvenida") },
                        onClick = { showMenu = false; onHome() }
                    )
                    DropdownMenuItem(
                        text = { Text("Iniciar Sesión") },
                        onClick = { showMenu = false; onLogin() }
                    )
                    DropdownMenuItem(
                        text = { Text("Registrarse") },
                        onClick = { showMenu = false; onRegister() }
                    )
                }
            }
        }
    )
}