package com.example.rentify.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class AppTopBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appTopBar_muestraTituloYMenu() {
        // 1. ARRANGE: Cargamos la barra en modo "No logueado"
        composeTestRule.setContent {
            AppTopBar(
                isLoggedIn = false,
                userRole = null,
                onOpenDrawer = {},
                onHome = {},
                onLogin = {},
                onRegister = {},
                onPropiedades = {},
                onPerfil = {},
                onSolicitudes = {}
            )
        }

        // 2. ASSERT: Verificamos lo básico
        // ¿Aparece el título de la app?
        composeTestRule.onNodeWithText("Rentify").assertIsDisplayed()

        // ¿Aparece el ícono del menú de 3 puntos? (Buscamos por su descripción)
        composeTestRule.onNodeWithContentDescription("Más").assertIsDisplayed()
    }

    @Test
    fun appTopBar_despliegaOpcionesAlHacerClick() {
        // 1. ARRANGE
        composeTestRule.setContent {
            AppTopBar(
                isLoggedIn = false, // Simulamos usuario visitante
                userRole = null,
                onOpenDrawer = {},
                onHome = {},
                onLogin = {},
                onRegister = {},
                onPropiedades = {},
                onPerfil = {},
                onSolicitudes = {}
            )
        }

        // 2. ACT: Hacemos click en el menú de 3 puntos
        composeTestRule.onNodeWithContentDescription("Más").performClick()

        // 3. ASSERT: Verificamos que aparezcan las opciones ocultas
        composeTestRule.onNodeWithText("Iniciar Sesión").assertIsDisplayed()
        composeTestRule.onNodeWithText("Registrarse").assertIsDisplayed()
    }
}