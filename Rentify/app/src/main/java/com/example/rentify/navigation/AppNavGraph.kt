package com.example.rentify.navigation

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import com.example.rentify.data.remote.dto.UsuarioDTO
import com.example.rentify.data.repository.ApplicationRemoteRepository
import com.example.rentify.data.repository.ContactRemoteRepository
import com.example.rentify.data.repository.DocumentRemoteRepository
import com.example.rentify.data.repository.PropertyRemoteRepository
import com.example.rentify.data.repository.UserRepository
import com.example.rentify.ui.components.AppTopBar
import com.example.rentify.ui.screen.*
import com.example.rentify.ui.screen.ContactScreen
import com.example.rentify.ui.viewmodel.*
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    navController: NavHostController,
    context: Context,
    authViewModel: RentifyAuthViewModel,
    propiedadViewModel: PropiedadViewModel,
    propiedadDetalleViewModel: PropiedadDetalleViewModel,
    solicitudesViewModel: SolicitudesViewModel,
    perfilViewModel: PerfilUsuarioViewModel,
    reviewViewModel: ReviewViewModel
) {
    val userPrefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()
    val database = RentifyDatabase.getInstance(context)

    val isLoggedIn by userPrefs.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    val userRole by userPrefs.userRole.collectAsStateWithLifecycle(initialValue = null)
    val userId by userPrefs.userId.collectAsStateWithLifecycle(initialValue = null)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Repositorios
    val propertyRepository = remember { PropertyRemoteRepository() }
    val applicationRepository = remember {
        ApplicationRemoteRepository(
            solicitudDao = database.solicitudDao(),
            catalogDao = database.catalogDao()
        )
    }

    // Funciones de navegacion
    val goWelcome: () -> Unit = {
        navController.navigate(Routes.WELCOME) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    val goHome: () -> Unit = {
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.HOME) { inclusive = true }
            launchSingleTop = true
        }
    }

    val goLogin: () -> Unit = {
        navController.navigate(Routes.LOGIN)
    }

    val goRegister: () -> Unit = {
        navController.navigate(Routes.REGISTER)
    }

    val goPropiedades: () -> Unit = {
        navController.navigate(Routes.CATALOGO_PROPIEDADES)
    }

    val goPerfil: () -> Unit = {
        navController.navigate(Routes.PERFIL)
    }

    val goSolicitudes: () -> Unit = {
        navController.navigate(Routes.SOLICITUDES)
    }

    val goMisDocumentos: () -> Unit = {
        navController.navigate(Routes.MIS_DOCUMENTOS)
    }

    val goPropiedadDetalle: (Long) -> Unit = { propiedadId ->
        navController.navigate("${Routes.PROPIEDAD_DETALLE}/$propiedadId")
    }

    val goHomeAfterLogin: () -> Unit = {
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.WELCOME) { inclusive = true }
            launchSingleTop = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                navController = navController,
                onCloseDrawer = { scope.launch { drawerState.close() } },
                context = context
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
                startDestination = if (isLoggedIn) Routes.HOME else Routes.WELCOME,
                modifier = Modifier.padding(innerPadding)
            ) {

                composable(Routes.WELCOME) {
                    WelcomeScreen(
                        onGoLogin = goLogin,
                        onGoRegister = goRegister
                    )
                }

                composable(Routes.HOME) {
                    HomeScreen(
                        onGoPropiedades = goPropiedades,
                        onGoLogin = goLogin,
                        onGoRegister = goRegister
                    )
                }

                composable(Routes.LOGIN) {
                    LoginScreenVm(
                        vm = authViewModel,
                        onLoginOkNavigateHome = goHomeAfterLogin,
                        onGoRegister = goRegister
                    )
                }

                composable(Routes.REGISTER) {
                    RegisterScreenVm(
                        vm = authViewModel,
                        onRegisteredNavigateLogin = goLogin,
                        onGoLogin = goLogin
                    )
                }

                // CATALOGO PROPIEDADES
                composable(Routes.CATALOGO_PROPIEDADES) {
                    val propiedadViewModelFactory = PropiedadViewModelFactory(
                        propiedadDao = database.propiedadDao(),
                        catalogDao = database.catalogDao(),
                        remoteRepository = propertyRepository
                    )

                    CatalogoPropiedadesScreen(
                        viewModelFactory = propiedadViewModelFactory,
                        onVerDetalle = { propiedadId ->
                            goPropiedadDetalle(propiedadId)
                        }
                    )
                }

                // PROPIEDAD DETALLE
                composable(
                    route = "${Routes.PROPIEDAD_DETALLE}/{propiedadId}",
                    arguments = listOf(navArgument("propiedadId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val propiedadId = backStackEntry.arguments?.getLong("propiedadId") ?: 0L

                    val propiedadDetalleViewModelFactory = PropiedadDetalleViewModelFactory(
                        propiedadDao = database.propiedadDao(),
                        catalogDao = database.catalogDao(),
                        propertyRepository = propertyRepository,
                        applicationRepository = applicationRepository
                    )

                    PropiedadDetalleScreen(
                        propiedadId = propiedadId,
                        userPreferences = userPrefs,
                        viewModelFactory = propiedadDetalleViewModelFactory,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSolicitudes = goSolicitudes
                    )
                }

                // PERFIL
                composable(Routes.PERFIL) {
                    PerfilUsuarioScreen(
                        vm = perfilViewModel,
                        onBack = { navController.popBackStack() },
                        onVerSolicitudes = goSolicitudes,
                        onVerDocumentos = goMisDocumentos,
                        onLogout = goWelcome
                    )
                }

                // SOLICITUDES
                composable(Routes.SOLICITUDES) {
                    val solicitudesViewModelFactory = SolicitudesViewModelFactory(
                        solicitudDao = database.solicitudDao(),
                        propiedadDao = database.propiedadDao(),
                        catalogDao = database.catalogDao(),
                        remoteRepository = applicationRepository,
                        propertyRepository = propertyRepository
                    )

                    SolicitudesScreen(
                        userPreferences = userPrefs,
                        viewModelFactory = solicitudesViewModelFactory,
                        onNavigateToDetalle = goPropiedadDetalle
                    )
                }

                // MIS DOCUMENTOS
                composable(Routes.MIS_DOCUMENTOS) {
                    val documentRepository = DocumentRemoteRepository()
                    val misDocumentosViewModel: MisDocumentosViewModel = viewModel(
                        factory = MisDocumentosViewModelFactory(documentRepository)
                    )

                    MisDocumentosScreen(
                        viewModel = misDocumentosViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // ADMIN PANEL
                composable(Routes.ADMIN_PANEL) {
                    AdminPanelScreen(
                        onBack = { navController.popBackStack() },
                        onGestionPropiedades = { navController.navigate(Routes.GESTION_PROPIEDADES) },
                        currentUser = null
                    )
                }

                // GESTION USUARIOS
                composable(Routes.GESTION_USUARIOS) {
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

                // GESTION PROPIEDADES
                composable(Routes.GESTION_PROPIEDADES) {
                    GestionPropiedadesScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                // GESTION DOCUMENTOS
                composable(Routes.GESTION_DOCUMENTOS) {
                    val documentRepository = DocumentRemoteRepository()
                    val gestionDocumentosViewModel: GestionDocumentosViewModel = viewModel(
                        factory = GestionDocumentosViewModelFactory(documentRepository)
                    )

                    GestionDocumentosScreen(
                        viewModel = gestionDocumentosViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // AGREGAR PROPIEDAD
                composable(Routes.AGREGAR_PROPIEDAD) {
                    val agregarPropiedadViewModelFactory = AgregarPropiedadViewModelFactory(
                        propertyRepository = propertyRepository
                    )

                    AgregarPropiedadScreen(
                        userPreferences = userPrefs,
                        viewModelFactory = agregarPropiedadViewModelFactory,
                        onNavigateBack = { navController.popBackStack() },
                        onPropiedadCreada = { navController.popBackStack() }
                    )
                }

                // MIS PROPIEDADES
                composable(Routes.MIS_PROPIEDADES) {
                    val misPropiedadesViewModelFactory = MisPropiedadesViewModelFactory(
                        propiedadDao = database.propiedadDao(),
                        catalogDao = database.catalogDao(),
                        propertyRepository = propertyRepository
                    )

                    MisPropiedadesScreen(
                        userPreferences = userPrefs,
                        viewModelFactory = misPropiedadesViewModelFactory,
                        onNavigateToAgregar = { navController.navigate(Routes.AGREGAR_PROPIEDAD) },
                        onNavigateToDetalle = { propiedadId -> goPropiedadDetalle(propiedadId) }
                    )
                }

                // CONTACT
                composable(Routes.CONTACT) {
                    val contactRepository = ContactRemoteRepository()
                    val contactViewModel: ContactViewModel = viewModel(
                        factory = ContactViewModelFactory(contactRepository)
                    )

                    ContactScreen(
                        contactViewModel = contactViewModel,
                        usuarioId = userId,
                        onBack = { navController.popBackStack() }
                    )
                }

                // SOLICITUD DETALLE
                composable(
                    route = "${Routes.SOLICITUD_DETALLE}/{solicitudId}",
                    arguments = listOf(navArgument("solicitudId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val solicitudId = backStackEntry.arguments?.getLong("solicitudId") ?: 0L

                    SolicitudDetalleScreen(
                        solicitudId = solicitudId,
                        userPreferences = userPrefs,
                        onBack = { navController.popBackStack() },
                        onVerPropiedad = { propiedadId -> goPropiedadDetalle(propiedadId) }
                    )
                }
            }
        }
    }
}