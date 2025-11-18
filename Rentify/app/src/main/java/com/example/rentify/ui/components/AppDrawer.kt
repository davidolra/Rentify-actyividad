package com.example.rentify.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
 * Drawer lateral para Rentify con menú contextual según rol
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
 * Helper para construir la lista de ítems del drawer según el estado de autenticación y rol
 */
@Composable
fun buildDrawerItems(
    isLoggedIn: Boolean,
    userRole: String?,
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onPropiedades: () -> Unit,
    onPerfil: () -> Unit,
    onSolicitudes: () -> Unit,
    onMisPropiedades: () -> Unit,
    onAgregarPropiedad: () -> Unit,
    onSolicitudesRecibidas: () -> Unit,
    onAdminPanel: () -> Unit,
    onGestionUsuarios: () -> Unit,
    onGestionPropiedades: () -> Unit
): List<DrawerItem> {

    // Si NO está logueado, mostrar solo opciones públicas
    if (!isLoggedIn) {
        return listOf(
            DrawerItem("Bienvenida", Icons.Filled.Home, onHome),
            DrawerItem("Iniciar Sesión", Icons.Filled.Login, onLogin),
            DrawerItem("Registrarse", Icons.Filled.PersonAdd, onRegister)
        )
    }

    // Usuario autenticado - menú según rol
    val items = mutableListOf<DrawerItem>()

    // Opciones comunes para todos los usuarios autenticados
    items.add(DrawerItem("Inicio", Icons.Filled.Home, onHome))
    items.add(DrawerItem("Propiedades", Icons.Filled.LocationOn, onPropiedades))
    items.add(DrawerItem("Mi Perfil", Icons.Filled.Person, onPerfil))

    // Opciones específicas por rol
    when (userRole?.uppercase()) {
        "ADMINISTRADOR", "ADMIN" -> {
            items.add(DrawerItem("Panel Admin", Icons.Filled.AdminPanelSettings, onAdminPanel))
            items.add(DrawerItem("Gestión Usuarios", Icons.Filled.People, onGestionUsuarios))
            items.add(DrawerItem("Gestión Propiedades", Icons.Filled.Business, onGestionPropiedades))
        }

        "PROPIETARIO" -> {
            items.add(DrawerItem("Mis Propiedades", Icons.Filled.Business, onMisPropiedades))
            items.add(DrawerItem("Agregar Propiedad", Icons.Filled.AddBusiness, onAgregarPropiedad))
            items.add(DrawerItem("Solicitudes Recibidas", Icons.Filled.RequestPage, onSolicitudesRecibidas))
        }

        "INQUILINO", "ARRIENDATARIO" -> {
            items.add(DrawerItem("Mis Solicitudes", Icons.Filled.Assignment, onSolicitudes))
        }
    }

    return items
}