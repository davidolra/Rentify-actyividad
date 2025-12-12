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
import com.example.rentify.data.repository.DocumentRemoteRepository
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

                    val rentifyUserRepository = RentifyUserRepository(
                        usuarioDao = db.usuarioDao(),
                        catalogDao = db.catalogDao()
                    )

                    val userRemoteRepository = UserRemoteRepository()

                    val documentRemoteRepository = DocumentRemoteRepository()

                    val applicationRemoteRepository = ApplicationRemoteRepository(
                        solicitudDao = db.solicitudDao(),
                        catalogDao = db.catalogDao()
                    )

                    val propertyRemoteRepository = PropertyRemoteRepository()

                    val reviewRemoteRepository = ReviewRemoteRepository()

                    // ==================== VIEWMODELS ====================

                    val authViewModel: RentifyAuthViewModel = viewModel(
                        factory = RentifyAuthViewModelFactory(
                            remoteRepository = userRemoteRepository,
                            localRepository = rentifyUserRepository,
                            documentRepository = documentRemoteRepository
                        )
                    )

                    val propiedadViewModel: PropiedadViewModel = viewModel(
                        factory = PropiedadViewModelFactory(
                            propiedadDao = db.propiedadDao(),
                            catalogDao = db.catalogDao(),
                            remoteRepository = propertyRemoteRepository
                        )
                    )

                    val propiedadDetalleViewModel: PropiedadDetalleViewModel = viewModel(
                        factory = PropiedadDetalleViewModelFactory(
                            propiedadDao = db.propiedadDao(),
                            catalogDao = db.catalogDao(),
                            propertyRepository = propertyRemoteRepository,
                            applicationRepository = applicationRemoteRepository
                        )
                    )

                    val solicitudesViewModel: SolicitudesViewModel = viewModel(
                        factory = SolicitudesViewModelFactory(
                            solicitudDao = db.solicitudDao(),
                            propiedadDao = db.propiedadDao(),
                            catalogDao = db.catalogDao(),
                            remoteRepository = applicationRemoteRepository,
                            propertyRepository = propertyRemoteRepository
                        )
                    )

                    val perfilViewModel: PerfilUsuarioViewModel = viewModel(
                        factory = PerfilUsuarioViewModelFactory(
                            usuarioDao = db.usuarioDao(),
                            catalogDao = db.catalogDao(),
                            solicitudDao = db.solicitudDao()
                        )
                    )

                    val reviewViewModel: ReviewViewModel = viewModel(
                        factory = ReviewViewModelFactory(reviewRemoteRepository)
                    )

                    // ==================== NAVEGACION ====================

                    AppNavGraph(
                        navController = navController,
                        context = applicationContext,
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