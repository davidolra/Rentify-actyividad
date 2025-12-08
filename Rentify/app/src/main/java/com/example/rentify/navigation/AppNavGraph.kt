package com.example.rentify.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import com.example.rentify.data.local.RentifyDatabase
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.remote.dto.*
import com.example.rentify.data.repository.ApplicationRemoteRepository
import com.example.rentify.data.repository.DocumentRemoteRepository
import com.example.rentify.data.repository.UserRepository
import com.example.rentify.ui.components.*
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

    val goMisDocumentos: () -> Unit = {
        navController.navigate(Route.MisDocumentos.path)
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

    fun mapRoleNameToId(roleName: String?): Long? {
        return when (roleName?.uppercase()) {
            "ADMINISTRADOR" -> 1L
            "PROPIETARIO" -> 2L
            "ARRENDATARIO" -> 3L
            else -> null
        }
    }

    // Menu drawer
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
            add(DrawerItem("Mis Documentos", Icons.Filled.Description) {
                scope.launch { drawerState.close() }
                goMisDocumentos()
            })

            when (userRole?.uppercase()) {
                "ADMINISTRADOR" -> {
                    add(DrawerItem("Gestion Usuarios", Icons.Filled.People) {
                        scope.launch { drawerState.close() }
                        navController.navigate(Route.GestionUsuarios.path)
                    })
                    add(DrawerItem("Gestion Propiedades", Icons.Filled.Business) {
                        scope.launch { drawerState.close() }
                        navController.navigate(Route.GestionPropiedades.path)
                    })
                    add(DrawerItem("Gestion Documentos", Icons.Filled.Folder) {
                        scope.launch { drawerState.close() }
                        navController.navigate(Route.GestionDocumentos.path)
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
                "ARRENDATARIO" -> {
                    add(DrawerItem("Mis Solicitudes", Icons.Filled.Assignment) {
                        scope.launch { drawerState.close() }
                        goSolicitudes()
                    })
                }
            }
        }
    } else {
        listOf(
            DrawerItem("Bienvenida", Icons.Filled.Home) {
                scope.launch { drawerState.close() }
                goHome()
            },
            DrawerItem("Iniciar Sesion", Icons.Filled.Login) {
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

                composable(Route.Welcome.path) {
                    WelcomeScreen(
                        onGoLogin = goLogin,
                        onGoRegister = goRegister
                    )
                }

                composable(Route.Home.path) {
                    HomeScreen(
                        onGoPropiedades = goPropiedades,
                        onGoLogin = goLogin,
                        onGoRegister = goRegister
                    )
                }

                composable(Route.Login.path) {
                    LoginScreenVm(
                        vm = authViewModel,
                        onLoginOkNavigateHome = goHomeAfterLogin,
                        onGoRegister = goRegister
                    )
                }

                composable(Route.Register.path) {
                    RegisterScreenVm(
                        vm = authViewModel,
                        onRegisteredNavigateLogin = goLogin,
                        onGoLogin = goLogin
                    )
                }

                composable(Route.Propiedades.path) {
                    CatalogoPropiedadesScreen(
                        vm = propiedadViewModel,
                        onVerDetalle = { propiedadId ->
                            goPropiedadDetalle(propiedadId)
                        }
                    )
                }

                composable(
                    route = Route.PropiedadDetalle.path,
                    arguments = listOf(navArgument("propiedadId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val propiedadId = backStackEntry.arguments?.getLong("propiedadId") ?: 0L
                    val userId by userPrefs.userId.collectAsStateWithLifecycle(initialValue = 0L)
                    val actualUserId = userId ?: 0L
                    val rolString by userPrefs.userRole.collectAsStateWithLifecycle(initialValue = null)
                    val rolId = mapRoleNameToId(rolString)

                    PropiedadDetalleScreen(
                        propiedadId = propiedadId,
                        vm = propiedadDetalleViewModel,
                        reviewViewModel = reviewViewModel,
                        currentUserId = actualUserId,
                        onBack = { navController.popBackStack() },
                        onSolicitar = { idPropiedad ->
                            rolId?.let {
                                solicitudesViewModel.crearSolicitud(
                                    usuarioId = actualUserId,
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
                        onLogout = goWelcome
                    )
                }

                composable(Route.Solicitudes.path) {
                    val database = RentifyDatabase.getInstance(context)
                    val applicationRepository = ApplicationRemoteRepository(
                        solicitudDao = database.solicitudDao(),
                        catalogDao = database.catalogDao()
                    )
                    val solicitudesViewModelFactory = SolicitudesViewModelFactory(
                        solicitudDao = database.solicitudDao(),
                        propiedadDao = database.propiedadDao(),
                        catalogDao = database.catalogDao(),
                        remoteRepository = applicationRepository
                    )

                    SolicitudesScreen(
                        userPreferences = userPrefs,
                        viewModelFactory = solicitudesViewModelFactory,
                        onNavigateToDetalle = goPropiedadDetalle
                    )
                }

                // Mis Documentos (Usuario)
                composable(Route.MisDocumentos.path) {
                    val documentRepository = DocumentRemoteRepository()
                    val misDocumentosViewModel: MisDocumentosViewModel = viewModel(
                        factory = MisDocumentosViewModelFactory(documentRepository)
                    )

                    MisDocumentosScreen(
                        viewModel = misDocumentosViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Admin: Gestion Usuarios
                composable(Route.GestionUsuarios.path) {
                    val userRepository = UserRepository(RetrofitClient.userServiceApi)
                    val userManagementViewModel: UserManagementViewModel = viewModel(
                        factory = UserManagementViewModelFactory(userRepository)
                    )

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

                // Admin: Gestion Propiedades
                composable(Route.GestionPropiedades.path) {
                    GestionPropiedadesScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                // Admin: Gestion Documentos
                composable(Route.GestionDocumentos.path) {
                    val documentRepository = DocumentRemoteRepository()
                    val gestionDocumentosViewModel: GestionDocumentosViewModel = viewModel(
                        factory = GestionDocumentosViewModelFactory(documentRepository)
                    )

                    GestionDocumentosScreen(
                        viewModel = gestionDocumentosViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Propietario: Agregar Propiedad
                composable(Route.AgregarPropiedad.path) {
                    AgregarPropiedadScreen(
                        onBack = { navController.popBackStack() },
                        onPropiedadCreada = { navController.popBackStack() }
                    )
                }

                // Propietario: Mis Propiedades
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