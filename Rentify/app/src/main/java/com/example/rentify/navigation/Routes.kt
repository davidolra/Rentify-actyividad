package com.example.rentify.navigation

sealed class Route(val path: String) {

    // Publicas
    data object Welcome : Route("welcome")
    data object Home : Route("home")
    data object Login : Route("login")
    data object Register : Route("register")

    // Generales
    data object Propiedades : Route("propiedades")

    data object PropiedadDetalle : Route("propiedad/{propiedadId}") {
        fun createRoute(propiedadId: Long) = "propiedad/$propiedadId"
    }

    data object Perfil : Route("perfil")
    data object Solicitudes : Route("solicitudes")
    data object Contacto : Route("contacto")
    data object MisDocumentos : Route("mis_documentos")

    // Admin
    data object AdminPanel : Route("admin_panel")
    data object GestionUsuarios : Route("gestion_usuarios")
    data object GestionPropiedades : Route("gestion_propiedades")
    data object GestionDocumentos : Route("gestion_documentos")

    // Propietario
    data object AgregarPropiedad : Route("agregar_propiedad")
    data object MisPropiedades : Route("mis_propiedades")
}