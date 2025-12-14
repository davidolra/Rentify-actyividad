package com.example.rentify.ui.viewmodel

import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.dao.SolicitudDao
import com.example.rentify.data.local.dao.UsuarioDao
import com.example.rentify.data.local.entities.EstadoEntity
import com.example.rentify.data.local.entities.RolEntity
import com.example.rentify.data.local.entities.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class PerfilUsuarioViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var usuarioDao: UsuarioDao
    @Mock
    private lateinit var catalogDao: CatalogDao
    @Mock
    private lateinit var solicitudDao: SolicitudDao

    private lateinit var viewModel: PerfilUsuarioViewModel

    // Datos Falsos Comunes
    private val FAKE_FNACIMIENTO = 1000L
    private val FAKE_FECHA = 1000L
    private val FAKE_CODIGO = "TESTCODE"
    private val FAKE_SNOMBRE = "Segundo"

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        viewModel = PerfilUsuarioViewModel(usuarioDao, catalogDao, solicitudDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cargarDatosUsuario obtiene usuario, nombre de rol y conteo de solicitudes correctamente`() = runTest {
        // 1. ARRANGE
        val userId = 10L
        val rolId = 2L

       
        val usuarioFake = UsuarioEntity(
            id = userId,
            pnombre = "Juan",
            snombre = FAKE_SNOMBRE,
            papellido = "Pérez",
            fnacimiento = FAKE_FNACIMIENTO,
            email = "juan@test.com",
            rut = "12345678-9",
            ntelefono = "987654321",
            clave = "clave123",
            duoc_vip = false,
            codigo_ref = FAKE_CODIGO,
            fcreacion = FAKE_FECHA,
            factualizacion = FAKE_FECHA,
            estado_id = 1,
            rol_id = rolId
        )

        val rolReal = RolEntity(id = rolId, nombre = "Propietario")
        val estadoPendienteReal = EstadoEntity(id = 5L, nombre = "Pendiente")

        // Enseñamos a los Mocks qué responder
        whenever(usuarioDao.getById(userId)).thenReturn(usuarioFake)
        whenever(catalogDao.getRolById(rolId)).thenReturn(rolReal)
        whenever(catalogDao.getEstadoByNombre("Pendiente")).thenReturn(estadoPendienteReal)

        // Simulamos que hay 3 solicitudes pendientes
        whenever(solicitudDao.countSolicitudesActivas(eq(userId), eq(estadoPendienteReal.id))).thenReturn(3)

        // 2. ACT
        viewModel.cargarDatosUsuario(userId)

        // 3. ASSERT
        assertNotNull(viewModel.usuario.value)
        assertEquals("Juan", viewModel.usuario.value?.pnombre)
        assertEquals("Propietario", viewModel.nombreRol.value)
        assertEquals(3, viewModel.cantidadSolicitudes.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `actualizarPerfil modifica los datos en memoria y llama a update en la BD`() = runTest {
        // 1. ARRANGE
        // Usuario Original (CON TODOS LOS CAMPOS OBLIGATORIOS Y GUION BAJO)
        val usuarioOriginal = UsuarioEntity(
            id = 1L,
            pnombre = "Original",
            snombre = FAKE_SNOMBRE,
            papellido = "Original",
            fnacimiento = FAKE_FNACIMIENTO,
            email = "test@rentify.cl",
            rut = "1-9",
            ntelefono = "111111111",
            clave = "clave123",
            duoc_vip = false,
            codigo_ref = FAKE_CODIGO,
            fcreacion = FAKE_FECHA,
            factualizacion = FAKE_FECHA,
            estado_id = 1,
            rol_id = 1
        )

        whenever(usuarioDao.getById(1L)).thenReturn(usuarioOriginal)
        viewModel.cargarDatosUsuario(1L) // Cargar datos iniciales

        // 2. ACT
        viewModel.actualizarPerfil(
            pnombre = "Editado",
            snombre = "Segundo",
            papellido = "NuevoApellido",
            telefono = "999999999",
            direccion = "Calle Nueva 123",
            comuna = "Santiago"
        )

        // 3. ASSERT
        // Verificamos actualización en StateFlow
        val usuarioActual = viewModel.usuario.value
        assertEquals("Editado", usuarioActual?.pnombre)

        // Verificamos llamada a la Base de Datos
        verify(usuarioDao).update(argThat { usuario ->
            usuario.pnombre == "Editado" &&
                    usuario.papellido == "NuevoApellido"
        })
    }
}