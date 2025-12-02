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
import com.example.rentify.data.repository.ReviewRemoteRepository
import com.example.rentify.data.repository.UserRemoteRepository
import com.example.rentify.data.repository.RentifyUserRepository
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

                    // ✅ NUEVO: Repositorio local de usuarios (para sincronización)
                    val rentifyUserRepository = RentifyUserRepository(
                        usuarioDao = db.usuarioDao(),
                        catalogDao = db.catalogDao()
                    )

                    // User Remote Repository (para autenticación)
                    val userRemoteRepository = UserRemoteRepository()

                    // Application Remote Repository
                    val applicationRemoteRepository = ApplicationRemoteRepository(
                        solicitudDao = db.solicitudDao(),
                        catalogDao = db.catalogDao()
                    )

                    // Property Remote Repository
                    val propertyRemoteRepository = PropertyRemoteRepository()

                    // ✅ NUEVO: Review Remote Repository
                    val reviewRemoteRepository = ReviewRemoteRepository()

                    // ==================== VIEWMODELS ====================

                    // Auth ViewModel - ✅ ACTUALIZADO: ahora recibe ambos repositorios
                    val authViewModel: RentifyAuthViewModel = viewModel(
                        factory = RentifyAuthViewModelFactory(
                            userRemoteRepository = userRemoteRepository,
                            rentifyUserRepository = rentifyUserRepository
                        )
                    )

                    // Propiedad ViewModel
                    val propiedadViewModel: PropiedadViewModel = viewModel(
                        factory = PropiedadViewModelFactory(
                            db.propiedadDao(),
                            db.catalogDao(),
                            propertyRemoteRepository
                        )
                    )

                    // Propiedad Detalle ViewModel
                    val propiedadDetalleViewModel: PropiedadDetalleViewModel = viewModel(
                        factory = PropiedadDetalleViewModelFactory(
                            db.propiedadDao(),
                            db.catalogDao()
                        )
                    )

                    // Solicitudes ViewModel
                    val solicitudesViewModel: SolicitudesViewModel = viewModel(
                        factory = SolicitudesViewModelFactory(
                            db.solicitudDao(),
                            db.propiedadDao(),
                            db.catalogDao(),
                            applicationRemoteRepository
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

                    // ✅ NUEVO: Review ViewModel
                    val reviewViewModel: ReviewViewModel = viewModel(
                        factory = ReviewViewModelFactory(reviewRemoteRepository)
                    )

                    // ==================== NAVEGACIÓN ====================

                    AppNavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        propiedadViewModel = propiedadViewModel,
                        propiedadDetalleViewModel = propiedadDetalleViewModel,
                        solicitudesViewModel = solicitudesViewModel,
                        perfilViewModel = perfilViewModel,
                        reviewViewModel = reviewViewModel  // ✅ NUEVO PARÁMETRO
                    )
                }
            }
        }
    }
}