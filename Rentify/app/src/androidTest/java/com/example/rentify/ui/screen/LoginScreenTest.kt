package com.example.rentify.ui.screen

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_muestraElementosCorrectamente() {
        // 1. ARRANGE
        composeTestRule.setContent {
            LoginScreen(
                email = "",
                pass = "",
                emailError = null,
                passError = null,
                canSubmit = false,
                isSubmitting = false,
                errorMsg = null,
                onEmailChange = {},
                onPassChange = {},
                onSubmit = {},
                onGoRegister = {}
            )
        }

        // 2. ASSERT
        // Verificamos el Título Principal
        composeTestRule.onNodeWithText("Rentify").assertIsDisplayed()

        // Verificamos Campos
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()

        // Verificamos "Iniciar Sesión" (Aparece en Título de la Card y en el Botón)
        val nodosLogin = composeTestRule.onAllNodesWithText("Iniciar Sesión")
        nodosLogin.assertCountEquals(2) // Debe haber 2
        nodosLogin[0].assertIsDisplayed()
        nodosLogin[1].assertIsDisplayed()
    }

    @Test
    fun loginScreen_muestraErrorCuandoSeLePasa() {
        // 1. ARRANGE
        composeTestRule.setContent {
            LoginScreen(
                email = "correo@mal",
                pass = "123",
                emailError = "Formato inválido",
                passError = null,
                canSubmit = false,
                isSubmitting = false,
                errorMsg = "Credenciales incorrectas",
                onEmailChange = {},
                onPassChange = {},
                onSubmit = {},
                onGoRegister = {}
            )
        }

        // 2. ASSERT
        composeTestRule.onNodeWithText("Formato inválido").assertIsDisplayed()
        composeTestRule.onNodeWithText("Credenciales incorrectas").assertIsDisplayed()
    }
}