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
import com.example.rentify.data.remote.api.DocumentServiceApi
import com.example.rentify.data.repository.ApplicationRemoteRepository
import com.example.rentify.data.repository.DocumentRepository
import com.example.rentify.data.repository.PropertyRemoteRepository
import com.example.rentify.data.repository.ReviewRemoteRepository
import com.example.rentify.data.repository.UserRemoteRepository
import com.example.rentify.data.repository.RentifyUserRepository
import com.example.rentify.navigation.AppNavGraph
import com.example.rentify.ui.theme.RentifyTheme
import com.example.rentify.ui.viewmodel.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

                    val documentServiceApi = Retrofit.Builder()
                        .baseUrl("http://10.0.2.2:8083/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(DocumentServiceApi::class.java)

                    val rentifyUserRepository = RentifyUserRepository(
                        usuarioDao = db.usuarioDao(),
                        catalogDao = db.catalogDao()
                    )

                    val userRemoteRepository = UserRemoteRepository()

                    val applicationRemoteRepository = ApplicationRemoteRepository(
                        solicitudDao = db.solicitudDao(),
                        catalogDao = db.catalogDao()
                    )

                    val propertyRemoteRepository = PropertyRemoteRepository()

                    val reviewRemoteRepository = ReviewRemoteRepository()

                    val documentRepository = DocumentRepository(documentServiceApi)

                    val authViewModel: RentifyAuthViewModel = viewModel(
                        factory = RentifyAuthViewModelFactory(
                            userRemoteRepository = userRemoteRepository,
                            rentifyUserRepository = rentifyUserRepository
                        )
                    )

                    val propiedadViewModel: PropiedadViewModel = viewModel(
                        factory = PropiedadViewModelFactory(
                            db.propiedadDao(),
                            db.catalogDao(),
                            propertyRemoteRepository
                        )
                    )

                    val propiedadDetalleViewModel: PropiedadDetalleViewModel = viewModel(
                        factory = PropiedadDetalleViewModelFactory(
                            db.propiedadDao(),
                            db.catalogDao()
                        )
                    )

                    val solicitudesViewModel: SolicitudesViewModel = viewModel(
                        factory = SolicitudesViewModelFactory(
                            db.solicitudDao(),
                            db.propiedadDao(),
                            db.catalogDao(),
                            applicationRemoteRepository
                        )
                    )

                    val perfilViewModel: PerfilUsuarioViewModel = viewModel(
                        factory = PerfilUsuarioViewModelFactory(
                            db.usuarioDao(),
                            db.catalogDao(),
                            db.solicitudDao(),
                            documentRepository,
                            userRemoteRepository
                        )
                    )

                    val reviewViewModel: ReviewViewModel = viewModel(
                        factory = ReviewViewModelFactory(reviewRemoteRepository)
                    )

                    val gestionUsuariosViewModel: GestionUsuariosViewModel = viewModel(
                        factory = GestionUsuariosViewModelFactory(userRemoteRepository)
                    )

                    AppNavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        propiedadViewModel = propiedadViewModel,
                        propiedadDetalleViewModel = propiedadDetalleViewModel,
                        solicitudesViewModel = solicitudesViewModel,
                        perfilViewModel = perfilViewModel,
                        reviewViewModel = reviewViewModel,
                        gestionUsuariosViewModel = gestionUsuariosViewModel
                    )
                }
            }
        }
    }
}
