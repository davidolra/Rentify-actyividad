package com.example.rentify.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Description
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.UsuarioDTO
import com.example.rentify.data.repository.DocumentRemoteRepository
import com.example.rentify.data.repository.UserRepository
import com.example.rentify.ui.components.AppDrawer
import com.example.rentify.ui.components.AppTopBar
import com.example.rentify.ui.components.DrawerItem
import com.example.rentify.ui.screen.*
import com.example.rentify.ui.viewmodel.*
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: RentifyAuthViewModel,
    propiedadViewModel: PropiedadViewModel,
    propiedadDetalleViewModel: PropiedadDetalleViewModel,
    solicitudesViewModel: SolicitudesViewModel,
    perfilViewModel: PerfilUsuarioViewModel,
    reviewViewModel: ReviewViewModel
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()

    val isLoggedIn by userPrefs.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    val userRole by userPrefs.userRole.collectAsStateWithLifecycle(initialValue = null)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Repositorios y ViewModels que se creaban internamente en las rutas
    val documentRepository = remember { DocumentRemoteRepository() }
    val userRepository = remember { UserRepository(RetrofitClient.userServiceApi) }

    val misDocumentosViewModel: MisDocumentosViewModel = viewModel(
        factory = MisDocumentosViewModelFactory(documentRepository, userPrefs)
    )
    val gestionDocumentosViewModel: GestionDocumentosViewModel = viewModel(
        factory = GestionDocumentosViewModelFactory(documentRepository)
    )
    val userManagementViewModel: UserManagementViewModel = viewModel(
        factory = UserManagementViewModelFactory(userRepository)
    )

    // Funciones de navegacion
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

    val goLogin: () -> Unit = { navController.navigate(Route.Login.path) }
    val goRegister: () -> Unit = { navController.navigate(Route.Register.path) }
    val goPropiedades: () -> Unit = { navController.navigate(Route.Propiedades.path) }
    val goPerfil: () -> Unit = { navController.navigate(Route.Perfil.path) }
    val goSolicitudes: () -> Unit = { navController.navigate(Route.Solicitudes.path) }
    val goMisDocumentos: () -> Unit = { navController.navigate(Route.MisDocumentos.path) }

    val goPropiedadDetalle: (Long) -> Unit = { propiedadId ->
        navController.navigate(Route.PropiedadDetalle.createRoute(propiedadId))
    }

    val goHomeAfterLogin: () -> Unit = {
        navController.navigate(Route.Home.path) {
            popUpTo(Route.Welcome.path) { inclusive = true }
            launchSingleTop = true
        }
    }

    val logout: () -> Unit = {
        authViewModel.logout()
        goWelcome()
    }

    fun mapRoleNameToId(roleName: String?): Long? {
        return when (roleName?.uppercase()) {
            "ADMINISTRADOR" -> 1L
            "PROPIETARIO" -> 2L
            "ARRENDATARIO" -> 3L
            else -> null
        }
    }

    fun closeDrawerAndNavigate(navigate: () -> Unit) {
        scope.launch {
            drawerState.close()
            navigate()
        }
    }

    val drawerItems = if (isLoggedIn) {
        buildList {
            add(DrawerItem("Inicio", Icons.Filled.Home) { closeDrawerAndNavigate(goHome) })
            add(DrawerItem("Propiedades", Icons.Filled.LocationOn) { closeDrawerAndNavigate(goPropiedades) })
            add(DrawerItem("Mi Perfil", Icons.Filled.Person) { closeDrawerAndNavigate(goPerfil) })
            add(DrawerItem("Mis Documentos", Icons.AutoMirrored.Filled.Description) { closeDrawerAndNavigate(goMisDocumentos) })

            when (userRole?.uppercase()) {
                "ADMINISTRADOR" -> {
                    add(DrawerItem("Solicitudes", Icons.AutoMirrored.Filled.Assignment) { closeDrawerAndNavigate(goSolicitudes) })
                    add(DrawerItem("Gestión Usuarios", Icons.Filled.People) { closeDrawerAndNavigate { navController.navigate(Route.GestionUsuarios.path) } })
                    add(DrawerItem("Gestión Propiedades", Icons.Filled.Business) { closeDrawerAndNavigate { navController.navigate(Route.GestionPropiedades.path) } })
                    add(DrawerItem("Gestión Documentos", Icons.Filled.Folder) { closeDrawerAndNavigate { navController.navigate(Route.GestionDocumentos.path) } })
                }
                "PROPIETARIO" -> {
                    add(DrawerItem("Mis Propiedades", Icons.Filled.Business) { closeDrawerAndNavigate { navController.navigate(Route.MisPropiedades.path) } })
                    add(DrawerItem("Crear Propiedad", Icons.Filled.Add) { closeDrawerAndNavigate { navController.navigate(Route.AgregarPropiedad.path) } })
                    add(DrawerItem("Solicitudes", Icons.AutoMirrored.Filled.Assignment) { closeDrawerAndNavigate(goSolicitudes) })
                }
                "ARRENDATARIO" -> {
                    add(DrawerItem("Mis Solicitudes", Icons.AutoMirrored.Filled.Assignment) { closeDrawerAndNavigate(goSolicitudes) })
                }
            }
        }
    } else {
        listOf(
            DrawerItem("Bienvenida", Icons.Filled.Home) { closeDrawerAndNavigate(goHome) },
            DrawerItem("Iniciar Sesión", Icons.AutoMirrored.Filled.Login) { closeDrawerAndNavigate(goLogin) },
            DrawerItem("Registrarse", Icons.Filled.PersonAdd) { closeDrawerAndNavigate(goRegister) }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { AppDrawer(items = drawerItems, onLogout = { closeDrawerAndNavigate(logout) }) }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    isLoggedIn = isLoggedIn,
                    userRole = userRole,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (isLoggedIn) Route.Home.path else Route.Welcome.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Route.Welcome.path) { WelcomeScreen(onGoLogin = goLogin, onGoRegister = goRegister) }
                composable(Route.Home.path) { HomeScreen(onGoPropiedades = goPropiedades, onGoLogin = goLogin, onGoRegister = goRegister) }
                composable(Route.Login.path) { LoginScreenVm(vm = authViewModel, onLoginOkNavigateHome = goHomeAfterLogin, onGoRegister = goRegister) }
                composable(Route.Register.path) { RegisterScreenVm(vm = authViewModel, onRegisteredNavigateLogin = goLogin, onGoLogin = goLogin) }
                composable(Route.Propiedades.path) { CatalogoPropiedadesScreen(vm = propiedadViewModel, onVerDetalle = goPropiedadDetalle) }

                composable(
                    route = Route.PropiedadDetalle.path,
                    arguments = listOf(navArgument("propiedadId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val propiedadId = backStackEntry.arguments?.getLong("propiedadId") ?: 0L
                    val userId by userPrefs.userId.collectAsStateWithLifecycle(initialValue = 0L)
                    val rolId = mapRoleNameToId(userRole)

                    PropiedadDetalleScreen(
                        propiedadId = propiedadId,
                        vm = propiedadDetalleViewModel,
                        reviewViewModel = reviewViewModel,
                        currentUserId = userId,
                        onBack = { navController.popBackStack() },
                        onSolicitar = { idPropiedad ->
                            rolId?.let {
                                solicitudesViewModel.crearSolicitud(
                                    usuarioId = userId,
                                    propiedadId = idPropiedad,
                                    rolId = it.toInt()
                                )
                            }
                            goSolicitudes()
                        }
                    )
                }

                composable(Route.Perfil.path) {
                    PerfilUsuarioScreen(
                        vm = perfilViewModel,
                        onBack = { navController.popBackStack() },
                        onVerSolicitudes = goSolicitudes,
                        onVerDocumentos = goMisDocumentos,
                        onLogout = logout
                    )
                }

                composable(Route.Solicitudes.path) {
                    SolicitudesScreen(
                        viewModel = solicitudesViewModel,
                        userPreferences = userPrefs,
                        onNavigateToDetalle = goPropiedadDetalle
                    )
                }

                composable(Route.MisDocumentos.path) {
                    MisDocumentosScreen(
                        viewModel = misDocumentosViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Route.GestionUsuarios.path) {
                    val users by userManagementViewModel.users.collectAsStateWithLifecycle()
                    val isLoading by userManagementViewModel.isLoading.collectAsStateWithLifecycle()
                    val error by userManagementViewModel.error.collectAsStateWithLifecycle()

                    UserManagementScreen(
                        users = users,
                        isLoading = isLoading,
                        error = error,
                        onBack = { navController.popBackStack() },
                        onUpdateUser = { user: UsuarioDTO -> userManagementViewModel.updateUser(user.id!!, user) },
                        onDeleteUser = { user: UsuarioDTO -> userManagementViewModel.deleteUser(user.id!!) },
                        onRetry = { userManagementViewModel.loadUsers() }
                    )
                }

                composable(Route.GestionPropiedades.path) { GestionPropiedadesScreen(onBack = { navController.popBackStack() }) }

                composable(Route.GestionDocumentos.path) {
                    GestionDocumentosScreen(
                        viewModel = gestionDocumentosViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Route.AgregarPropiedad.path) {
                    AgregarPropiedadScreen(
                        onBack = { navController.popBackStack() },
                        onPropiedadCreada = { navController.popBackStack() }
                    )
                }

                composable(Route.MisPropiedades.path) {
                    MisPropiedadesScreen(
                        onBack = { navController.popBackStack() },
                        onAgregarPropiedad = { navController.navigate(Route.AgregarPropiedad.path) }
                    )
                }
            }
        }
    }
}
