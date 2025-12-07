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
import com.example.rentify.data.remote.RetrofitClient
import com.example.rentify.data.repository.ApplicationRemoteRepository
import com.example.rentify.data.repository.PropertyRemoteRepository
import com.example.rentify.data.repository.ReviewRemoteRepository
import com.example.rentify.data.repository.UserRemoteRepository
import com.example.rentify.data.repository.UserRepository
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

                    // Repositorio local de usuarios (para sincronizacion)
                    val rentifyUserRepository = RentifyUserRepository(
                        usuarioDao = db.usuarioDao(),
                        catalogDao = db.catalogDao()
                    )

                    // User Remote Repository (para autenticacion)
                    val userRemoteRepository = UserRemoteRepository()

                    // UserRepository para operaciones de perfil (usa API remota)
                    val userRepository = UserRepository(RetrofitClient.userServiceApi)

                    // Application Remote Repository
                    val applicationRemoteRepository = ApplicationRemoteRepository(
                        solicitudDao = db.solicitudDao(),
                        catalogDao = db.catalogDao()
                    )

                    // Property Remote Repository
                    val propertyRemoteRepository = PropertyRemoteRepository()

                    // Review Remote Repository
                    val reviewRemoteRepository = ReviewRemoteRepository()

                    // ==================== VIEWMODELS ====================

                    // Auth ViewModel
                    val authViewModel: RentifyAuthViewModel = viewModel(
                        factory = RentifyAuthViewModelFactory(
                            remoteRepository = userRemoteRepository,
                            localRepository = rentifyUserRepository
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

                    // Perfil ViewModel - Usa UserRepository (API remota)
                    val perfilViewModel: PerfilUsuarioViewModel = viewModel(
                        factory = PerfilUsuarioViewModelFactory(userRepository)
                    )

                    // Review ViewModel
                    val reviewViewModel: ReviewViewModel = viewModel(
                        factory = ReviewViewModelFactory(reviewRemoteRepository)
                    )

                    // ==================== NAVEGACION ====================

                    AppNavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        propiedadViewModel = propiedadViewModel,
                        propiedadDetalleViewModel = propiedadDetalleViewModel,
                        solicitudesViewModel = solicitudesViewModel,
                        perfilViewModel = perfilViewModel,
                        reviewViewModel = reviewViewModel
                    )
                }
            }
        }
    }
}