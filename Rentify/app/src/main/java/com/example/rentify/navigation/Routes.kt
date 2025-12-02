package com.example.rentify.navigation

/**
 * Rutas simplificadas + rutas ADMIN
 */
sealed class Route(val path: String) {

    // ----- PUBLICAS -----
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

    // ----- PROPIETARIO -----
    data object AgregarPropiedad : Route("agregar_propiedad")

    // ----- PROPIETARIO -----
    data object MisPropiedades : Route("mis_propiedades")
}