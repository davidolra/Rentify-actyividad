package com.example.rentify.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.ui.components.*
import com.example.rentify.ui.screen.*
import com.example.rentify.ui.viewmodel.*

/**
 * Grafo de navegación completo para Rentify con sistema de roles
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: RentifyAuthViewModel,
    propiedadViewModel: PropiedadViewModel,
    propiedadDetalleViewModel: PropiedadDetalleViewModel,
    solicitudesViewModel: SolicitudesViewModel,
    perfilViewModel: PerfilUsuarioViewModel
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()

    // Observar estado de autenticación y rol
    val isLoggedIn by userPrefs.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    val userRole by userPrefs.userRole.collectAsStateWithLifecycle(initialValue = null)  // ✅ OBTENER ROL

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // ========== FUNCIONES DE NAVEGACIÓN ==========

    val goWelcome: () -> Unit = {
        navController.navigate(Route.Welcome.path) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    val goHome: () -> Unit = {
        navController.navigate(Route.Home.path) {
            popUpTo(Route.Home.path) { inclusive = true }
            launchSingleTop = true
        }
    }

    val goLogin: () -> Unit = {
        navController.navigate(Route.Login.path)
    }

    val goRegister: () -> Unit = {
        navController.navigate(Route.Register.path)
    }

    val goPropiedades: () -> Unit = {
        navController.navigate(Route.Propiedades.path)
    }

    val goPerfil: () -> Unit = {
        navController.navigate(Route.Perfil.path)
    }

    val goSolicitudes: () -> Unit = {
        navController.navigate(Route.Solicitudes.path)
    }

    val goMisPropiedades: () -> Unit = {
        navController.navigate(Route.MisPropiedades.path)
    }

    val goAgregarPropiedad: () -> Unit = {
        navController.navigate(Route.AgregarPropiedad.path)
    }

    val goSolicitudesRecibidas: () -> Unit = {
        navController.navigate(Route.SolicitudesRecibidas.path)
    }

    val goAdminPanel: () -> Unit = {
        navController.navigate(Route.AdminPanel.path)
    }

    val goGestionUsuarios: () -> Unit = {
        navController.navigate(Route.GestionUsuarios.path)
    }

    val goGestionPropiedades: () -> Unit = {
        navController.navigate(Route.GestionPropiedades.path)
    }

    val goPropiedadDetalle: (Long) -> Unit = { propiedadId ->
        navController.navigate(Route.PropiedadDetalle.createRoute(propiedadId))
    }

    val goHomeAfterLogin: () -> Unit = {
        navController.navigate(Route.Home.path) {
            popUpTo(Route.Welcome.path) { inclusive = true }
            launchSingleTop = true
        }
    }

    // ========== MENÚ DRAWER CONTEXTUAL ==========
    val drawerItems = buildDrawerItems(
        isLoggedIn = isLoggedIn,
        userRole = userRole,
        onHome = { scope.launch { drawerState.close() }; goHome() },
        onLogin = { scope.launch { drawerState.close() }; goLogin() },
        onRegister = { scope.launch { drawerState.close() }; goRegister() },
        onPropiedades = { scope.launch { drawerState.close() }; goPropiedades() },
        onPerfil = { scope.launch { drawerState.close() }; goPerfil() },
        onSolicitudes = { scope.launch { drawerState.close() }; goSolicitudes() },
        onMisPropiedades = { scope.launch { drawerState.close() }; goMisPropiedades() },
        onAgregarPropiedad = { scope.launch { drawerState.close() }; goAgregarPropiedad() },
        onSolicitudesRecibidas = { scope.launch { drawerState.close() }; goSolicitudesRecibidas() },
        onAdminPanel = { scope.launch { drawerState.close() }; goAdminPanel() },
        onGestionUsuarios = { scope.launch { drawerState.close() }; goGestionUsuarios() },
        onGestionPropiedades = { scope.launch { drawerState.close() }; goGestionPropiedades() }
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = null,
                items = drawerItems
            )
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    isLoggedIn = isLoggedIn,
                    userRole = userRole,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onHome = goHome,
                    onLogin = goLogin,
                    onRegister = goRegister,
                    onPropiedades = goPropiedades,
                    onPerfil = goPerfil,
                    onSolicitudes = goSolicitudes,
                    onMisPropiedades = goMisPropiedades,
                    onAgregarPropiedad = goAgregarPropiedad,
                    onAdminPanel = goAdminPanel
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (isLoggedIn) Route.Home.path else Route.Welcome.path,
                modifier = Modifier.padding(innerPadding)
            ) {

                // ========== WELCOME (NO AUTENTICADO) ==========
                composable(Route.Welcome.path) {
                    WelcomeScreen(
                        onGoLogin = goLogin,
                        onGoRegister = goRegister
                    )
                }

                // ========== HOME (AUTENTICADO/NO AUTENTICADO) ==========
                composable(Route.Home.path) {
                    HomeScreen(
                        onGoPropiedades = goPropiedades,
                        onGoLogin = goLogin,
                        onGoRegister = goRegister
                    )
                }

                // ========== LOGIN ==========
                composable(Route.Login.path) {
                    LoginScreenVm(
                        vm = authViewModel,
                        onLoginOkNavigateHome = goHomeAfterLogin,
                        onGoRegister = goRegister
                    )
                }

                // ========== REGISTRO ==========
                composable(Route.Register.path) {
                    RegisterScreenVm(
                        vm = authViewModel,
                        onRegisteredNavigateLogin = goLogin,
                        onGoLogin = goLogin
                    )
                }

                // ========== CATÁLOGO PROPIEDADES ==========
                composable(Route.Propiedades.path) {
                    CatalogoPropiedadesScreen(
                        vm = propiedadViewModel,
                        onVerDetalle = goPropiedadDetalle
                    )
                }

                // ========== DETALLE PROPIEDAD ==========
                composable(
                    route = Route.PropiedadDetalle.path,
                    arguments = listOf(
                        navArgument("propiedadId") { type = NavType.LongType }
                    )
                ) { backStackEntry ->
                    val propiedadId = backStackEntry.arguments?.getLong("propiedadId") ?: 0L

                    PropiedadDetalleScreen(
                        propiedadId = propiedadId,
                        vm = propiedadDetalleViewModel,
                        onBack = { navController.popBackStack() },
                        onSolicitar = { goSolicitudes() }
                    )
                }

                // ========== PERFIL ==========
                composable(Route.Perfil.path) {
                    PerfilUsuarioScreen(
                        vm = perfilViewModel,
                        onBack = { navController.popBackStack() },
                        onVerSolicitudes = goSolicitudes,
                        onLogout = goWelcome
                    )
                }

                // ========== MIS SOLICITUDES (INQUILINO) ==========
                composable(Route.Solicitudes.path) {
                    SolicitudesScreen(
                        vm = solicitudesViewModel,
                        onBack = { navController.popBackStack() },
                        onVerPropiedad = goPropiedadDetalle
                    )
                }

                // ========== MIS PROPIEDADES (PROPIETARIO) ==========
                composable(Route.MisPropiedades.path) {
                    MisPropiedadesScreen(
                        onBack = { navController.popBackStack() },
                        onAgregarPropiedad = goAgregarPropiedad,
                        onEditarPropiedad = { /* TODO */ },
                        onVerDetalle = goPropiedadDetalle
                    )
                }

                // ========== AGREGAR PROPIEDAD (PROPIETARIO) ==========
                composable(Route.AgregarPropiedad.path) {
                    AgregarPropiedadScreen(
                        onBack = { navController.popBackStack() },
                        onPropiedadCreada = goMisPropiedades
                    )
                }


                // ========== SOLICITUDES RECIBIDAS (PROPIETARIO) ==========
                composable(Route.SolicitudesRecibidas.path) {
                    // TODO: Implementar SolicitudesRecibidasScreen
                    Text("Solicitudes Recibidas")
                }

                // ========== PANEL ADMIN ==========
                composable(Route.AdminPanel.path) {
                    AdminPanelScreen(
                        onBack = { navController.popBackStack() },
                        onGestionUsuarios = goGestionUsuarios,
                        onGestionPropiedades = goGestionPropiedades
                    )
                }

                // ========== GESTIÓN USUARIOS (ADMIN) ==========
                composable(Route.GestionUsuarios.path) {
                    GestionUsuariosScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                // ========== GESTIÓN PROPIEDADES (ADMIN) ==========
                composable(Route.GestionPropiedades.path) {
                    GestionPropiedadesScreen(
                        onBack = { navController.popBackStack() },
                        onVerDetalle = goPropiedadDetalle
                    )
                }
            }
        }
    }
}