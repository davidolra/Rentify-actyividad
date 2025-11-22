package com.example.rentify.navigation

/**
 * ✅ Rutas simplificadas SIN ROLES
 */
sealed class Route(val path: String) {
    data object Welcome : Route("welcome")
    data object Home : Route("home")
    data object Login : Route("login")
    data object Register : Route("register")

    // Rutas generales (autenticadas)
    data object Propiedades : Route("propiedades")
    data object PropiedadDetalle : Route("propiedad/{propiedadId}") {
        fun createRoute(propiedadId: Long) = "propiedad/$propiedadId"
    }
    data object Perfil : Route("perfil")
    data object Solicitudes : Route("solicitudes")
}