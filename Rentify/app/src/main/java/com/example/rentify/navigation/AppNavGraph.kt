package com.example.rentify.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.example.rentify.ui.components.AppTopBar
import com.example.rentify.ui.components.AppDrawer
import com.example.rentify.ui.components.defaultDrawerItems
import com.example.rentify.ui.screen.*
import com.example.rentify.ui.viewmodel.*

/**
 * Grafo de navegación completo para Rentify
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Helpers de navegación
    val goHome: () -> Unit = {
        navController.navigate(Route.Home.path) {
            popUpTo(Route.Home.path) { inclusive = true }
        }
    }
    val goLogin: () -> Unit = { navController.navigate(Route.Login.path) }
    val goRegister: () -> Unit = { navController.navigate(Route.Register.path) }
    val goPropiedades: () -> Unit = { navController.navigate(Route.Propiedades.path) }
    val goSolicitudes: () -> Unit = { navController.navigate(Route.Solicitudes.path) }
    val goPerfil: () -> Unit = { navController.navigate(Route.Perfil.path) }
    val goPropiedadDetalle: (Long) -> Unit = { propiedadId ->
        navController.navigate(Route.PropiedadDetalle.createRoute(propiedadId))
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = null,
                items = defaultDrawerItems(
                    onHome = {
                        scope.launch { drawerState.close() }
                        goHome()
                    },
                    onLogin = {
                        scope.launch { drawerState.close() }
                        goLogin()
                    },
                    onRegister = {
                        scope.launch { drawerState.close() }
                        goRegister()
                    },
                    onPropiedades = {
                        scope.launch { drawerState.close() }
                        goPropiedades()
                    },
                    onPerfil = {  // ✅ AGREGADO
                        scope.launch { drawerState.close() }
                        goPerfil()
                    },
                    onSolicitudes = {  // ✅ AGREGADO
                        scope.launch { drawerState.close() }
                        goSolicitudes()
                    }
                )
            )
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onHome = goHome,
                    onLogin = goLogin,
                    onRegister = goRegister,
                    onPropiedades = goPropiedades,
                    onPerfil = goPerfil,          // ✅ AGREGADO
                    onSolicitudes = goSolicitudes  // ✅ AGREGADO
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Route.Home.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                // ========== HOME ==========
                composable(Route.Home.path) {
                    HomeScreen(
                        onGoLogin = goLogin,
                        onGoRegister = goRegister,
                        onGoPropiedades = goPropiedades
                    )
                }

                // ========== LOGIN ==========
                composable(Route.Login.path) {
                    LoginScreenVm(
                        vm = authViewModel,
                        onLoginOkNavigateHome = goHome,
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
                        onVerDetalle = { propiedadId ->
                            goPropiedadDetalle(propiedadId)
                        }
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
                        onSolicitar = { propId ->
                            // TODO: Implementar diálogo de confirmación
                            // Por ahora solo navegamos a solicitudes
                            goSolicitudes()
                        }
                    )
                }

                // ========== MIS SOLICITUDES ==========
                composable(Route.Solicitudes.path) {
                    SolicitudesScreen(
                        vm = solicitudesViewModel,
                        onBack = { navController.popBackStack() },
                        onVerPropiedad = { propiedadId ->
                            goPropiedadDetalle(propiedadId)
                        }
                    )
                }

                // ========== PERFIL ==========
                composable(Route.Perfil.path) {
                    PerfilUsuarioScreen(
                        vm = perfilViewModel,
                        onBack = { navController.popBackStack() },
                        onVerSolicitudes = goSolicitudes,
                        onLogout = goHome
                    )
                }
            }
        }
    }
}