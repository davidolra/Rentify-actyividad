package com.example.rentify.ui.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.LoginResponseRemoteDTO // ✅ Importante
import com.example.rentify.data.remote.dto.UsuarioRemoteDTO
import com.example.rentify.data.repository.DocumentRemoteRepository
import com.example.rentify.data.repository.RentifyUserRepository
import com.example.rentify.data.repository.UserRemoteRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RentifyAuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var userRemoteRepository: UserRemoteRepository
    @Mock
    private lateinit var rentifyUserRepository: RentifyUserRepository
    @Mock
    private lateinit var documentRepository: DocumentRemoteRepository

    private lateinit var viewModel: RentifyAuthViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = RentifyAuthViewModel(userRemoteRepository, rentifyUserRepository, documentRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== TEST 1: EMAIL ====================
    @Test
    fun `registro email invalido genera error`() {
        viewModel.onRegisterEmailChange("usuario-sin-arroba")
        assertNotNull("Debe haber error si falta el @", viewModel.register.value.emailError)
    }

    @Test
    fun `registro email valido limpia el error`() {
        viewModel.onRegisterEmailChange("test@duocuc.cl")
        assertNull("No debe haber error si el email es correcto", viewModel.register.value.emailError)
    }

    // ==================== TEST 2: RUT ====================
    @Test
    fun `registro rut vacio genera error`() {
        viewModel.onRutChange("")
        assertNotNull("El RUT vacío debe dar error", viewModel.register.value.rutError)
    }

    @Test
    fun `registro rut con texto es valido`() {
        viewModel.onRutChange("12.345.678-5")
        assertNull("RUT con texto debería ser válido", viewModel.register.value.rutError)
    }

    // ==================== TEST 3: PASSWORD ====================
    @Test
    fun `password debil genera error`() {
        viewModel.onRegisterPassChange("12345")
        assertNotNull("Contraseña corta debe dar error", viewModel.register.value.passError)
    }
    // ====================  LOGIN ====================
    @Test
    fun `submitLogin activa y desactiva isLoading correctamente`() = runTest {
        // 1. ARRANGE
        viewModel.onLoginEmailChange("test@duocuc.cl")
        viewModel.onLoginPassChange("Password123")

        // Preparamos los datos de respuesta
        val usuarioFake = UsuarioRemoteDTO(
            id = 1, pnombre = "Test", snombre = "", papellido = "User",
            fnacimiento = "2000-01-01", email = "mail@test.com",
            rut = "12.345.678-5", ntelefono = "999", clave = "pass", rolId = 3
        )
        val respuestaCorrecta = LoginResponseRemoteDTO(mensaje = "Ok", usuario = usuarioFake)

        // 🧠 LA ESTRATEGIA: Validamos DENTRO de la llamada al repositorio.
        // Cuando el ViewModel llama a .login(), ya debería haber puesto isSubmitting = true.
        whenever(userRemoteRepository.login(any(), any())).thenAnswer {

            // ¡AQUÍ ATRAPAMOS AL VIEWMODEL CON LAS MANOS EN LA MASA!
            val estaCargando = viewModel.login.value.isSubmitting
            assertTrue("Cuando se llama al API, isSubmitting debería ser TRUE", estaCargando)

            // Devolvemos el éxito inmediatamente
            ApiResult.Success(respuestaCorrecta)
        }

        // 2. ACT
        viewModel.submitLogin()

        // 3. ASSERT FINAL
        advanceUntilIdle() // Dejamos que termine el ciclo

        // Al final, debe haber vuelto a false y tener éxito
        assertFalse("Al terminar, isSubmitting debería volver a false", viewModel.login.value.isSubmitting)
        assertTrue("El login debió terminar exitosamente", viewModel.login.value.success)
    }
}