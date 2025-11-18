package com.example.rentify.navigation

// Clase sellada para rutas: evita "strings mágicos" y facilita refactors
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

    // Rutas de CLIENTE/ARRIENDATARIO
    data object Solicitudes : Route("solicitudes")

    // Rutas de PROPIETARIO
    data object MisPropiedades : Route("mis-propiedades")
    data object AgregarPropiedad : Route("agregar-propiedad")
    data object SolicitudesRecibidas : Route("solicitudes-recibidas")

    // Rutas de ADMIN
    data object AdminPanel : Route("admin-panel")
    data object GestionUsuarios : Route("gestion-usuarios")
    data object GestionPropiedades : Route("gestion-propiedades")
}