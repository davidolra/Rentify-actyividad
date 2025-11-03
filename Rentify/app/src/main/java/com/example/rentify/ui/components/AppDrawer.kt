package com.example.rentify.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Estructura de un ítem de menú lateral
 */
data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/**
 * Drawer lateral para Rentify
 */
@Composable
fun AppDrawer(
    currentRoute: String?,
    items: List<DrawerItem>,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                selected = false,
                onClick = item.onClick,
                icon = { Icon(item.icon, contentDescription = item.label) },
                modifier = Modifier,
                colors = NavigationDrawerItemDefaults.colors()
            )
        }
    }
}

/**
 * Helper para construir la lista de ítems del drawer
 */
@Composable
fun defaultDrawerItems(
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onPropiedades: () -> Unit,
    onPerfil: () -> Unit,          // ✅ NUEVO
    onSolicitudes: () -> Unit      // ✅ NUEVO
): List<DrawerItem> = listOf(
    DrawerItem("Home", Icons.Filled.Home, onHome),
    DrawerItem("Propiedades", Icons.Filled.LocationOn, onPropiedades),
    DrawerItem("Mi Perfil", Icons.Filled.Person, onPerfil),                    // ✅ NUEVO
    DrawerItem("Mis Solicitudes", Icons.Filled.Assignment, onSolicitudes),     // ✅ NUEVO
    DrawerItem("Login", Icons.Filled.AccountCircle, onLogin),
    DrawerItem("Registro", Icons.Filled.PersonAdd, onRegister)
)