package com.example.rentify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.rentify.data.local.database.RentifyDatabase
import com.example.rentify.data.repository.RentifyUserRepository
import com.example.rentify.navigation.AppNavGraph
import com.example.rentify.ui.theme.RentifyTheme
import com.example.rentify.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRoot()
        }
    }
}

@Composable
fun AppRoot() {
    val context = LocalContext.current.applicationContext

    // ====== Construcción de dependencias (DI manual) ======
    val db = RentifyDatabase.getInstance(context)
    val usuarioDao = db.usuarioDao()
    val catalogDao = db.catalogDao()
    val propiedadDao = db.propiedadDao()
    val solicitudDao = db.solicitudDao()

    val userRepository = RentifyUserRepository(usuarioDao, catalogDao)

    // ViewModel de autenticación
    val authViewModel: RentifyAuthViewModel = viewModel(
        factory = RentifyAuthViewModelFactory(userRepository)
    )

    // ViewModel de propiedades (GPS)
    val propiedadViewModel: PropiedadViewModel = viewModel(
        factory = PropiedadViewModelFactory(propiedadDao, catalogDao)
    )

    // ViewModel de detalle de propiedad
    val propiedadDetalleViewModel: PropiedadDetalleViewModel = viewModel(
        factory = PropiedadDetalleViewModelFactory(propiedadDao, catalogDao)
    )

    // ViewModel de solicitudes
    val solicitudesViewModel: SolicitudesViewModel = viewModel(
        factory = SolicitudesViewModelFactory(solicitudDao, propiedadDao, catalogDao)
    )

    // ViewModel de perfil de usuario
    val perfilViewModel: PerfilUsuarioViewModel = viewModel(
        factory = PerfilUsuarioViewModelFactory(usuarioDao, catalogDao, solicitudDao)
    )

    // ====== Navegación ======
    val navController = rememberNavController()

    RentifyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppNavGraph(
                navController = navController,
                authViewModel = authViewModel,
                propiedadViewModel = propiedadViewModel,
                propiedadDetalleViewModel = propiedadDetalleViewModel,
                solicitudesViewModel = solicitudesViewModel,
                perfilViewModel = perfilViewModel
            )
        }
    }
}