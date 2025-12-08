package com.example.rentify.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rentify.data.local.RentifyDatabase

@Composable
fun AppDrawer(
    navController: NavController,
    onCloseDrawer: () -> Unit,
    context: android.content.Context
) {
    val db = RentifyDatabase.getInstance(context)
    val usuario = db.usuarioDao().obtenerUsuarioActual()

    val esArrendatario = usuario?.rolId == 1L
    val esPropietario = usuario?.rolId == 2L
    val esAdmin = usuario?.rolId == 3L

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Text(
                text = "Rentify",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            if (usuario != null) {
                Text(
                    text = "${usuario.nombre} ${usuario.apellido}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = usuario.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            DrawerItem(
                icon = Icons.Default.Home,
                label = "Inicio",
                onClick = {
                    navController.navigate(Routes.HOME)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.Person,
                label = "Mi Perfil",
                onClick = {
                    navController.navigate(Routes.PERFIL)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.Search,
                label = "Buscar Propiedades",
                onClick = {
                    navController.navigate(Routes.CATALOGO_PROPIEDADES)
                    onCloseDrawer()
                }
            )

            if (esArrendatario || esAdmin) {
                DrawerItem(
                    icon = Icons.Default.Assignment,
                    label = "Mis Solicitudes",
                    onClick = {
                        navController.navigate(Routes.SOLICITUDES)
                        onCloseDrawer()
                    }
                )

                DrawerItem(
                    icon = Icons.Default.Description,
                    label = "Mis Documentos",
                    onClick = {
                        navController.navigate(Routes.MIS_DOCUMENTOS)
                        onCloseDrawer()
                    }
                )
            }

            if (esPropietario || esAdmin) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Propietario",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                DrawerItem(
                    icon = Icons.Default.HomeWork,
                    label = "Mis Propiedades",
                    onClick = {
                        navController.navigate(Routes.MIS_PROPIEDADES)
                        onCloseDrawer()
                    }
                )

                DrawerItem(
                    icon = Icons.Default.Add,
                    label = "Agregar Propiedad",
                    onClick = {
                        navController.navigate(Routes.AGREGAR_PROPIEDAD)
                        onCloseDrawer()
                    }
                )

                DrawerItem(
                    icon = Icons.Default.Assignment,
                    label = "Solicitudes Recibidas",
                    onClick = {
                        navController.navigate(Routes.SOLICITUDES)
                        onCloseDrawer()
                    }
                )
            }

            if (esAdmin) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Administración",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                DrawerItem(
                    icon = Icons.Default.Dashboard,
                    label = "Panel Admin",
                    onClick = {
                        navController.navigate(Routes.ADMIN_PANEL)
                        onCloseDrawer()
                    }
                )

                DrawerItem(
                    icon = Icons.Default.People,
                    label = "Gestión Usuarios",
                    onClick = {
                        navController.navigate(Routes.GESTION_USUARIOS)
                        onCloseDrawer()
                    }
                )

                DrawerItem(
                    icon = Icons.Default.Business,
                    label = "Gestión Propiedades",
                    onClick = {
                        navController.navigate(Routes.GESTION_PROPIEDADES)
                        onCloseDrawer()
                    }
                )

                DrawerItem(
                    icon = Icons.Default.Description,
                    label = "Gestión Documentos",
                    onClick = {
                        navController.navigate(Routes.GESTION_DOCUMENTOS)
                        onCloseDrawer()
                    }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            DrawerItem(
                icon = Icons.Default.ContactMail,
                label = "Contacto",
                onClick = {
                    navController.navigate(Routes.CONTACT)
                    onCloseDrawer()
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            DrawerItem(
                icon = Icons.Default.ExitToApp,
                label = "Cerrar Sesión",
                onClick = {
                    db.usuarioDao().cerrarSesion()
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                    onCloseDrawer()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}