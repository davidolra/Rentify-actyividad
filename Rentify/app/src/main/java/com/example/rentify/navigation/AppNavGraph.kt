package com.example.rentify.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
 * ✅ Grafo de navegación SIN ROLES
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

    // ✅ Observar estado de autenticación Y ROL
    val isLoggedIn by userPrefs.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    val userRole by userPrefs.userRole.collectAsStateWithLifecycle(initialValue = null)

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

    val goPropiedadDetalle: (Long) -> Unit = { propiedadId ->
        navController.navigate(Route.PropiedadDetalle.createRoute(propiedadId))
    }

    val goHomeAfterLogin: () -> Unit = {
        navController.navigate(Route.Home.path) {
            popUpTo(Route.Welcome.path) { inclusive = true }
            launchSingleTop = true
        }
    }

    // ================== MAPEO NOMBRE → ID ==================
    fun mapRoleNameToId(roleName: String?): Long? {
        return when (roleName?.uppercase()) {
            "ADMINISTRADOR" -> 1L
            "PROPIETARIO" -> 2L
            "ARRENDATARIO" -> 3L
            else -> null
        }
    }


    // ========== MENÚ DRAWER CONTEXTUAL POR ROL ==========
    val drawerItems = if (isLoggedIn) {
        buildList {
            add(DrawerItem("Inicio", Icons.Filled.Home) {
                scope.launch { drawerState.close() }
                goHome()
            })
            add(DrawerItem("Propiedades", Icons.Filled.LocationOn) {
                scope.launch { drawerState.close() }
                goPropiedades()
            })
            add(DrawerItem("Mi Perfil", Icons.Filled.Person) {
                scope.launch { drawerState.close() }
                goPerfil()
            })

            // Opciones según rol
            when (userRole?.uppercase()) {
                "ADMINISTRADOR" -> {
                    add(DrawerItem("Gestión Usuarios", Icons.Filled.People) {
                        scope.launch { drawerState.close() }
                        navController.navigate(Route.GestionUsuarios.path)
                    })

                    add(DrawerItem("Gestión Propiedades", Icons.Filled.Business) {
                        scope.launch { drawerState.close() }
                        navController.navigate(Route.GestionPropiedades.path)
                    })
                }

                "PROPIETARIO" -> {
                    add(DrawerItem("Mis Propiedades", Icons.Filled.Business) {
                        scope.launch { drawerState.close() }
                        navController.navigate(Route.MisPropiedades.path)
                    })
                    add(DrawerItem("Crear Propiedad", Icons.Filled.Add) {
                        scope.launch { drawerState.close() }
                        navController.navigate(Route.AgregarPropiedad.path)
                    })
                }

                "ARRENDATARIO" -> add(DrawerItem("Mis Solicitudes", Icons.Filled.Assignment) {
                    scope.launch { drawerState.close() }
                    goSolicitudes()
                })
            }
        }
    } else {
        listOf(
            DrawerItem("Bienvenida", Icons.Filled.Home) {
                scope.launch { drawerState.close() }
                goHome()
            },
            DrawerItem("Iniciar Sesión", Icons.Filled.Login) {
                scope.launch { drawerState.close() }
                goLogin()
            },
            DrawerItem("Registrarse", Icons.Filled.PersonAdd) {
                scope.launch { drawerState.close() }
                goRegister()
            }
        )
    }


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
                    onSolicitudes = goSolicitudes
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

                // ========== MIS SOLICITUDES ==========
                composable(Route.Solicitudes.path) {
                    SolicitudesScreen(
                        vm = solicitudesViewModel,
                        onBack = { navController.popBackStack() },
                        onVerPropiedad = goPropiedadDetalle
                    )
                }

                // ======== ADMIN: GESTIÓN DE USUARIOS ========
                composable(Route.GestionUsuarios.path) {
                    UserManagementScreen(
                        currentUserRol = mapRoleNameToId(userRole),
                        onBack = { navController.popBackStack() }
                    )
                }

                // ======== ADMIN: GESTIÓN DE PROPIEDADES ========
                composable(Route.GestionPropiedades.path) {
                    GestionPropiedadesScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                // ======== PROPIETARIO: AGREGAR PROPIEDAD ========
                composable(Route.AgregarPropiedad.path) {
                    AgregarPropiedadScreen(
                        onBack = { navController.popBackStack() },
                        onPropiedadCreada = { navController.popBackStack() } // vuelve atrás tras crear
                    )
                }


                // ======== PROPIETARIO: MIS PROPIEDADES ========
                composable(Route.MisPropiedades.path) {
                    MisPropiedadesScreen(
                        onBack = { navController.popBackStack() },
                        onAgregarPropiedad = { navController.navigate(Route.AgregarPropiedad.path) },
                    )
                }



            }
        }
    }
}