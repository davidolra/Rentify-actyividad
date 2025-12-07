package com.example.rentify.navigation

/**
 * Rutas de navegación de la aplicación.
 * Incluye rutas públicas, generales, admin y propietario.
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
    data object Contacto : Route("contacto")

    // ----- ADMIN -----
    data object AdminPanel : Route("admin_panel")
    data object GestionUsuarios : Route("gestion_usuarios")
    data object GestionPropiedades : Route("gestion_propiedades")
    data object GestionDocumentos : Route("gestion_documentos")  // ← NUEVA RUTA

    // ----- PROPIETARIO -----
    data object AgregarPropiedad : Route("agregar_propiedad")
    data object MisPropiedades : Route("mis_propiedades")

    // ----- MIS DOCUMENTOS (Usuario) -----
    data object MisDocumentos : Route("mis_documentos")
}