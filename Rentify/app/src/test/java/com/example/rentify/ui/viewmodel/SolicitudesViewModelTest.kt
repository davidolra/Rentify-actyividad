package com.example.rentify.ui.viewmodel

import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.dao.PropiedadDao
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.SolicitudArriendoDTO
import com.example.rentify.data.repository.ApplicationRemoteRepository
import com.example.rentify.data.repository.PropertyRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class SolicitudesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    @Mock lateinit var solicitudDao: SolicitudDao
    @Mock lateinit var propiedadDao: PropiedadDao
    @Mock lateinit var catalogDao: CatalogDao
    @Mock lateinit var remoteRepository: ApplicationRemoteRepository
    @Mock lateinit var propertyRepository: PropertyRemoteRepository

    private lateinit var viewModel: SolicitudesViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Configuración inicial síncrona
        runBlocking {
            whenever(solicitudDao.getAll()).thenReturn(flowOf(emptyList()))
        }

        viewModel = SolicitudesViewModel(
            solicitudDao,
            propiedadDao,
            catalogDao,
            remoteRepository,
            propertyRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== TEST 1: CARGAR SOLICITUDES ====================
    @Test
    fun `cargarSolicitudesArrendatario carga exitosamente`() = runTest {
        // 1. ARRANGE
        val solicitudFake = SolicitudArriendoDTO(
            id = 1,
            usuarioId = 10,
            propiedadId = 20,
            estado = "PENDIENTE"
        )

        whenever(remoteRepository.obtenerSolicitudesUsuario(any()))
            .thenReturn(ApiResult.Success(listOf(solicitudFake)))

        // 2. ACT
        viewModel.cargarSolicitudesArrendatario(10L)
        advanceUntilIdle()

        // 3. ASSERT
        assertEquals("Debe cargar 1 solicitud", 1, viewModel.solicitudes.value.size)
        assertEquals("PENDIENTE", viewModel.solicitudes.value[0].nombreEstado)
        assertFalse("No debe estar cargando", viewModel.isLoading.value)
    }

    // ==================== TEST 2: APROBAR SOLICITUD====================
    @Test
    fun `aprobarSolicitud llama al repo y recarga la lista`() = runTest {
        // 1. ARRANGE
        val solicitudId = 55L

        val solicitudAceptada = SolicitudArriendoDTO(
            id = solicitudId,
            usuarioId = 99,       // Obligatorio
            propiedadId = 88,     // Obligatorio
            estado = "ACEPTADA"
        )

        // Simulamos respuesta al actualizar
        whenever(remoteRepository.actualizarEstadoSolicitud(solicitudId, "ACEPTADA"))
            .thenAnswer {
                assertTrue("Debe estar cargando", viewModel.isLoading.value)
                ApiResult.Success(solicitudAceptada)
            }

        // Simulamos que al recargar la lista, trae la solicitud ya aceptada
        whenever(remoteRepository.listarTodasSolicitudes())
            .thenReturn(ApiResult.Success(listOf(solicitudAceptada)))

        // 2. ACT
        viewModel.aprobarSolicitud(solicitudId)

        advanceUntilIdle()

        // 3. ASSERT
        assertNotNull("Debe haber mensaje de éxito", viewModel.successMsg.value)
        assertEquals("Solicitud aceptada", viewModel.successMsg.value)

        // Verificamos que la lista en el ViewModel se actualizó
        assertEquals("ACEPTADA", viewModel.solicitudes.value[0].nombreEstado)
    }

    // ==================== TEST 3: MANEJO DE ERRORES ====================
    @Test
    fun `crearSolicitud maneja error correctamente`() = runTest {
        // 1. ARRANGE
        val errorMsg = "Error al crear"

        whenever(remoteRepository.crearSolicitudRemota(any(), any()))
            .thenReturn(ApiResult.Error(errorMsg))

        // 2. ACT
        viewModel.crearSolicitud(1L, 1L)
        advanceUntilIdle()

        // 3. ASSERT
        assertEquals(errorMsg, viewModel.errorMsg.value)
        assertFalse(viewModel.isLoading.value)
    }
}