package com.example.rentify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.rentify.data.local.RentifyDatabase
import com.example.rentify.data.repository.ApplicationRemoteRepository
import com.example.rentify.data.repository.PropertyRemoteRepository
import com.example.rentify.data.repository.UserRemoteRepository
import com.example.rentify.navigation.AppNavGraph
import com.example.rentify.ui.theme.RentifyTheme
import com.example.rentify.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentifyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val db = RentifyDatabase.getInstance(applicationContext)

                    // ==================== REPOSITORIOS ====================

                    // User Remote Repository (para autenticación)
                    val userRemoteRepository = UserRemoteRepository()

                    // ✅ CORREGIDO: Application Remote Repository
                    val applicationRemoteRepository = ApplicationRemoteRepository(
                        solicitudDao = db.solicitudDao(),
                        catalogDao = db.catalogDao()
                    )

                    val propertyRemoteRepository = PropertyRemoteRepository()

                    // ==================== VIEWMODELS ====================

                    // Auth ViewModel
                    val authViewModel: RentifyAuthViewModel = viewModel(
                        factory = RentifyAuthViewModelFactory(userRemoteRepository)
                    )

                    // Propiedad ViewModel
                    val propiedadViewModel: PropiedadViewModel = viewModel(
                        factory = PropiedadViewModelFactory(
                            db.propiedadDao(),
                            db.catalogDao(),
                            propertyRemoteRepository  // ✅ AGREGAR
                        )
                    )

                    // Propiedad Detalle ViewModel
                    val propiedadDetalleViewModel: PropiedadDetalleViewModel = viewModel(
                        factory = PropiedadDetalleViewModelFactory(
                            db.propiedadDao(),
                            db.catalogDao()
                        )
                    )

                    // ✅ CORREGIDO: Solicitudes ViewModel con remote repository
                    val solicitudesViewModel: SolicitudesViewModel = viewModel(
                        factory = SolicitudesViewModelFactory(
                            db.solicitudDao(),
                            db.propiedadDao(),
                            db.catalogDao(),
                            applicationRemoteRepository  // ✅ AÑADIDO
                        )
                    )

                    // Perfil ViewModel
                    val perfilViewModel: PerfilUsuarioViewModel = viewModel(
                        factory = PerfilUsuarioViewModelFactory(
                            db.usuarioDao(),
                            db.catalogDao(),
                            db.solicitudDao()
                        )
                    )

                    // ==================== NAVEGACIÓN ====================

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
    }
}