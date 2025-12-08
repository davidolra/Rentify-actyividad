package com.example.rentify.navigation

import android.content.Context
import androidx.compose.runtime.*
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
import com.example.rentify.data.repository.*
import com.example.rentify.ui.screens.*
import com.example.rentify.ui.viewmodel.*

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    context: Context
) {
    val db = RentifyDatabase.getInstance(context)
    val userPrefs = remember { UserPreferences(context) }
    val userId by userPrefs.userId.collectAsStateWithLifecycle(initialValue = null)

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(navController)
        }

        composable(Routes.LOGIN) {
            LoginScreen(navController, context)
        }

        composable(Routes.REGISTER) {
            RegisterScreen(navController, context)
        }

        composable(Routes.HOME) {
            HomeScreen(navController, context)
        }

        composable(Routes.PERFIL) {
            PerfilUsuarioScreen(navController, context)
        }

        composable(Routes.CATALOGO_PROPIEDADES) {
            CatalogoPropiedadesScreen(navController, context)
        }

        composable(
            route = "${Routes.PROPIEDAD_DETALLE}/{propiedadId}",
            arguments = listOf(navArgument("propiedadId") { type = NavType.LongType })
        ) { backStackEntry ->
            val propiedadId = backStackEntry.arguments?.getLong("propiedadId") ?: 0L
            PropiedadDetalleScreen(navController, propiedadId, context)
        }

        composable(Routes.SOLICITUDES) {
            SolicitudesScreen(navController, context, esArrendatario = true)
        }

        composable(
            route = "${Routes.SOLICITUD_DETALLE}/{solicitudId}",
            arguments = listOf(navArgument("solicitudId") { type = NavType.LongType })
        ) { backStackEntry ->
            val solicitudId = backStackEntry.arguments?.getLong("solicitudId") ?: 0L
            SolicitudDetalleScreen(navController, solicitudId, context)
        }

        composable(Routes.MIS_PROPIEDADES) {
            MisPropiedadesScreen(navController, context)
        }

        composable(Routes.AGREGAR_PROPIEDAD) {
            AgregarPropiedadScreen(navController, context)
        }

        composable(Routes.MIS_DOCUMENTOS) {
            MisDocumentosScreen(navController, context)
        }

        composable(Routes.ADMIN_PANEL) {
            AdminPanelScreen(navController, context)
        }

        composable(Routes.GESTION_USUARIOS) {
            GestionUsuariosScreen(navController, context)
        }

        composable(Routes.GESTION_PROPIEDADES) {
            GestionPropiedadesScreen(navController, context)
        }

        composable(Routes.GESTION_DOCUMENTOS) {
            GestionDocumentosScreen(navController, context)
        }

        composable(Routes.CONTACT) {
            val contactViewModel: ContactViewModel = viewModel(
                factory = ContactViewModelFactory(
                    ContactRemoteRepository(db.catalogDao())
                )
            )
            ContactScreen(
                contactViewModel = contactViewModel,
                usuarioId = userId,
                onBack = { navController.navigateUp() }
            )
        }
    }
}