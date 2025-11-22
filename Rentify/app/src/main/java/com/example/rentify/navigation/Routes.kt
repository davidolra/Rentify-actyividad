package com.example.rentify.navigation

/**
 * ✅ Rutas simplicadas + rutas ADMIN
 */
sealed class Route(val path: String) {

    // ----- PÚBLICAS -----
    data object Welcome : Route("welcome")
    data object Home : Route("home")
    data object Login : Route("login")
    data object Register : Route("register")

    // ----- GENERALES -----
    data object Propiedades : Route("propiedades")

    data object PropiedadDetalle : Route("propiedad/{propiedadId}") {
        fun createRoute(propiedadId: Long) = "propiedad/$propiedadId"
    }

    data object Perfil : Route("perfil")
    data object Solicitudes : Route("solicitudes")

    // ----- ADMIN -----
    data object GestionUsuarios : Route("gestion_usuarios")
    data object GestionPropiedades : Route("gestion_propiedades")
}
