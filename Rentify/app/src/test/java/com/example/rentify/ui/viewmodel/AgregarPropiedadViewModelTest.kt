package com.example.rentify.ui.viewmodel

import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.*
import com.example.rentify.data.repository.PropertyRemoteRepository
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
class AgregarPropiedadViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // ⚠️ ASEGÚRATE QUE ESTA LÍNEA TENGA EL @Mock ARRIBA
    @Mock
    private lateinit var propertyRepository: PropertyRemoteRepository

    private lateinit var viewModel: AgregarPropiedadViewModel

    @Before
    fun setUp() {
        // 1. INICIALIZACIÓN SÍNCRONA (Sin corrutinas aquí)
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // 2. CONFIGURACIÓN DE MOCKS (Aquí usamos runBlocking para las funciones suspendidas)
        runBlocking {
            // Configuramos las respuestas vacías para que el init del ViewModel no falle
            whenever(propertyRepository.listarTipos()).thenReturn(ApiResult.Success(emptyList()))
            whenever(propertyRepository.listarRegiones()).thenReturn(ApiResult.Success(emptyList()))
            whenever(propertyRepository.listarComunas()).thenReturn(ApiResult.Success(emptyList()))
            whenever(propertyRepository.listarCategorias()).thenReturn(ApiResult.Success(emptyList()))
        }

        // 3. INSTANCIAR VIEWMODEL
        viewModel = AgregarPropiedadViewModel(propertyRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== TEST 1: FILTRADO DE COMUNAS ====================
    @Test
    fun `cargarComunasPorRegion filtra correctamente`() = runTest {
        // 1. ARRANGE
        val regionIdTarget = 5L
        val comunaCorrecta = ComunaRemoteDTO(id = 1, nombre = "Comuna 1", regionId = 5, region = null)

        whenever(propertyRepository.obtenerComunasPorRegion(regionIdTarget))
            .thenReturn(ApiResult.Success(listOf(comunaCorrecta)))

        // 2. ACT
        viewModel.cargarComunasPorRegion(regionIdTarget)
        advanceUntilIdle()

        // 3. ASSERT
        assertEquals("Debería haber 1 comuna filtrada", 1, viewModel.comunasFiltradas.value.size)
        assertEquals("Comuna 1", viewModel.comunasFiltradas.value[0].nombre)
    }

    // ==================== TEST 2: CREAR PROPIEDAD ====================
    @Test
    fun `crearPropiedad activa loading y guarda exitosamente`() = runTest {
        // 1. ARRANGE
        val propiedadCreadaFake = PropertyRemoteDTO(
            id = 777,
            codigo = "COD-TEST",
            titulo = "Casa Test",
            precioMensual = 500000.0,
            divisa = "CLP",
            m2 = 80.0,
            nHabit = 3,
            nBanos = 2,
            petFriendly = true,
            direccion = "Calle Test 123",
            tipoId = 1,
            comunaId = 1,
            propietarioId = 1,
            fotos = emptyList(),
            tipo = null,
            comuna = null
        )

        // EL TEST INFILTRADO: Verificamos 'isSaving' dentro del mock
        whenever(propertyRepository.crearPropiedad(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenAnswer {
            assertTrue("El ViewModel debería estar guardando (isSaving=true)", viewModel.isSaving.value)
            ApiResult.Success(propiedadCreadaFake)
        }

        // 2. ACT
        viewModel.crearPropiedad(
            "COD-TEST", "Casa Test", 500000.0, "CLP", 80.0,
            3, 2, true, "Calle Test 123", 1, 1, 1
        )

        advanceUntilIdle()

        // 3. ASSERT FINAL
        assertFalse("Al terminar, isSaving debe ser false", viewModel.isSaving.value)
        assertNotNull("La propiedad creada no debe ser nula", viewModel.propiedadCreada.value)
        assertEquals(777L, viewModel.propiedadCreada.value?.id)
        assertNotNull("Debe haber un mensaje de éxito", viewModel.successMsg.value)
    }

    // ==================== TEST 3: ERROR AL CREAR ====================
    @Test
    fun `crearPropiedad maneja errores del servidor`() = runTest {
        // 1. ARRANGE
        val mensajeError = "Error de conexión"

        whenever(propertyRepository.crearPropiedad(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(ApiResult.Error(mensajeError))

        // 2. ACT
        viewModel.crearPropiedad(
            "X", "X", 0.0, "CLP", 0.0, 0, 0, false, "X", 1, 1, 1
        )
        advanceUntilIdle()

        // 3. ASSERT
        assertNull("No se debió crear la propiedad", viewModel.propiedadCreada.value)
        assertEquals(mensajeError, viewModel.errorMsg.value)
        assertFalse("isSaving debe apagarse tras el error", viewModel.isSaving.value)
    }
}