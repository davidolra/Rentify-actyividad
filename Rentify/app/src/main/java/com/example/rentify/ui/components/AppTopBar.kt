package com.example.rentify.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow

/**
 * Barra superior para Rentify
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onOpenDrawer: () -> Unit,
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onPropiedades: () -> Unit,
    onPerfil: () -> Unit,          // ✅ NUEVO
    onSolicitudes: () -> Unit      // ✅ NUEVO
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
            IconButton(onClick = onHome) {
                Icon(Icons.Filled.Home, contentDescription = "Home")
            }
            IconButton(onClick = onPropiedades) {
                Icon(Icons.Filled.LocationOn, contentDescription = "Propiedades")
            }
            IconButton(onClick = onPerfil) {  // ✅ NUEVO
                Icon(Icons.Filled.Person, contentDescription = "Perfil")
            }
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Más")
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Home") },
                    onClick = { showMenu = false; onHome() }
                )
                DropdownMenuItem(
                    text = { Text("Propiedades") },
                    onClick = { showMenu = false; onPropiedades() }
                )
                DropdownMenuItem(
                    text = { Text("Mi Perfil") },  // ✅ NUEVO
                    onClick = { showMenu = false; onPerfil() }
                )
                DropdownMenuItem(
                    text = { Text("Mis Solicitudes") },  // ✅ NUEVO
                    onClick = { showMenu = false; onSolicitudes() }
                )
                DropdownMenuItem(
                    text = { Text("Login") },
                    onClick = { showMenu = false; onLogin() }
                )
                DropdownMenuItem(
                    text = { Text("Registro") },
                    onClick = { showMenu = false; onRegister() }
                )
            }
        }
    )
}