package com.example.rentify.ui.viewmodel

import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.ResenaDTO
import com.example.rentify.data.remote.dto.TipoResenaDTO
import com.example.rentify.data.repository.ReviewRemoteRepository
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
import org.mockito.kotlin.anyOrNull // ✅ IMPORTANTE
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var reviewRepository: ReviewRemoteRepository

    private lateinit var viewModel: ReviewViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Mockeamos los tipos de reseña para que el init del ViewModel no falle
        runBlocking {
            whenever(reviewRepository.listarTiposResena())
                .thenReturn(ApiResult.Success(listOf(
                    TipoResenaDTO(1, "Propiedad"),
                    TipoResenaDTO(2, "Usuario")
                )))
        }

        viewModel = ReviewViewModel(reviewRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== TEST 1: CREAR RESEÑA ====================
    @Test
    fun `crearResenaPropiedad guarda y recarga la lista exitosamente`() = runTest {
        // 1. ARRANGE
        val propiedadId = 100L
        val usuarioId = 10L

        val nuevaResena = ResenaDTO(
            id = 1,
            usuarioId = usuarioId,
            propiedadId = propiedadId,
            puntuacion = 5,
            tipoResenaId = 1,
            comentario = "Excelente propiedad",
            estado = "ACTIVA"
        )



        whenever(reviewRepository.crearResena(any(), anyOrNull(), anyOrNull(), any(), anyOrNull(), any()))
            .thenAnswer {
                assertTrue("Debe estar cargando", viewModel.isLoading.value)
                ApiResult.Success(nuevaResena)
            }

        // Mock de recarga de lista
        whenever(reviewRepository.obtenerResenasPorPropiedad(eq(propiedadId), any()))
            .thenReturn(ApiResult.Success(listOf(nuevaResena)))

        // Mock de promedio
        whenever(reviewRepository.calcularPromedioPorPropiedad(propiedadId))
            .thenReturn(ApiResult.Success(5.0))

        // 2. ACT
        viewModel.crearResenaPropiedad(
            usuarioId = usuarioId,
            propiedadId = propiedadId,
            puntuacion = 5,
            comentario = "Excelente propiedad"
        )

        advanceUntilIdle()

        // 3. ASSERT
        // mensaje de error del ViewModel
        if (viewModel.successMessage.value == null) {
            println("ERROR DEL VM: ${viewModel.errorMessage.value}")
        }

        assertEquals("Resena creada exitosamente", viewModel.successMessage.value)
        assertEquals(1, viewModel.resenas.value.size)
    }

    // ==================== TEST 2: ELIMINAR RESEÑA ====================
    @Test
    fun `eliminarResena quita el item de la lista localmente`() = runTest {
        // 1. ARRANGE
        val resenaId = 55L

        val resenaExistente = ResenaDTO(
            id = resenaId,
            usuarioId = 1,
            puntuacion = 4,
            tipoResenaId = 1,
            comentario = "A borrar"
        )

        // Simulamos carga inicial
        whenever(reviewRepository.obtenerResenasPorPropiedad(any(), any()))
            .thenReturn(ApiResult.Success(listOf(resenaExistente)))
        whenever(reviewRepository.calcularPromedioPorPropiedad(any()))
            .thenReturn(ApiResult.Success(4.0))

        viewModel.cargarResenasPorPropiedad(1L)
        advanceUntilIdle()

        assertEquals(1, viewModel.resenas.value.size)

        // Forzamos el tipo Void para que no falle
        @Suppress("UNCHECKED_CAST")
        val respuestaVoid = ApiResult.Success(null) as ApiResult<Void>

        whenever(reviewRepository.eliminarResena(resenaId))
            .thenReturn(respuestaVoid)

        // 2. ACT
        viewModel.eliminarResena(resenaId)
        advanceUntilIdle()

        // 3. ASSERT
        assertEquals("La lista debe quedar vacía tras eliminar", 0, viewModel.resenas.value.size)
        assertEquals("Resena eliminada exitosamente", viewModel.successMessage.value)
    }

    // ==================== TEST 3: VALIDACIONES ====================
    @Test
    fun `validarComentario detecta textos muy cortos`() {
        val comentarioCorto = "Hola"
        val (esValido, mensaje) = viewModel.validarComentario(comentarioCorto)

        assertFalse(esValido)
        assertEquals("El comentario debe tener al menos 10 caracteres", mensaje)
    }
}