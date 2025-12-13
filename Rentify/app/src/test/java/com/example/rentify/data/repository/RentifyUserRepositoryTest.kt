package com.example.rentify.data.repository

import com.example.rentify.data.local.dao.CatalogDao
import com.example.rentify.data.local.dao.UsuarioDao
import com.example.rentify.data.local.entities.EstadoEntity
import com.example.rentify.data.local.entities.RolEntity
import com.example.rentify.data.local.entities.UsuarioEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RentifyUserRepositoryTest {

    @Mock
    private lateinit var usuarioDao: UsuarioDao
    @Mock
    private lateinit var catalogDao: CatalogDao

    private lateinit var repository: RentifyUserRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = RentifyUserRepository(usuarioDao, catalogDao)

        // Configuración global de Mocks para que no fallen las validaciones internas
        runBlocking {
            // Simulamos que el estado "Activo" existe en la BD
            whenever(catalogDao.getEstadoByNombre("Activo"))
                .thenReturn(EstadoEntity(1, "Activo"))

            // Simulamos que el rol "Arrendatario" existe
            whenever(catalogDao.getRolByNombre("Arrendatario"))
                .thenReturn(RolEntity(3, "Arrendatario"))
        }
    }

    // ==================== TEST 1: REGISTRO EXITOSO ====================
    @Test
    fun `register guarda usuario nuevo correctamente`() = runTest {
        // 1. ARRANGE
        // Simulamos que NO existe nadie con ese email ni rut
        whenever(usuarioDao.getByEmail(any())).thenReturn(null)
        whenever(usuarioDao.getByRut(any())).thenReturn(null)

        // Simulamos que al insertar, la BD devuelve el ID 100
        whenever(usuarioDao.insert(any())).thenReturn(100L)

        // 2. ACT
        val resultado = repository.register(
            pnombre = "Test",
            snombre = "",
            papellido = "User",
            fnacimiento = 1000L,
            email = "nuevo@gmail.com",
            rut = "11.111.111-1",
            ntelefono = "912345678",
            password = "Password123."
        )

        // 3. ASSERT
        assertTrue("El registro debería ser exitoso", resultado.isSuccess)
        assertEquals("Debe retornar el ID generado", 100L, resultado.getOrNull())
    }

    // ==================== TEST 2: LÓGICA VIP (DUOC) ====================
    @Test
    fun `register detecta correo DUOC y asigna puntos VIP`() = runTest {
        // 1. ARRANGE
        val emailDuoc = "alumno@duoc.cl"

        whenever(usuarioDao.getByEmail(any())).thenReturn(null)
        whenever(usuarioDao.getByRut(any())).thenReturn(null)

        // Capturamos el usuario que el repositorio intenta guardar
        var usuarioGuardado: UsuarioEntity? = null
        whenever(usuarioDao.insert(any())).thenAnswer { invocation ->
            usuarioGuardado = invocation.arguments[0] as UsuarioEntity
            200L
        }

        // 2. ACT
        repository.register(
            pnombre = "Alumno",
            snombre = "",
            papellido = "Duoc",
            fnacimiento = 1000L,
            email = emailDuoc, // <--- Correo VIP
            rut = "22.222.222-2",
            ntelefono = "912345678",
            password = "Pass."
        )

        // 3. ASSERT
        assertNotNull(usuarioGuardado)
        assertTrue("Debe marcarse como VIP", usuarioGuardado!!.duoc_vip)
        assertEquals("Debe tener 100 puntos de regalo", 100, usuarioGuardado!!.puntos)
    }

    // ==================== TEST 3: VALIDACIÓN DE DUPLICADOS ====================
    @Test
    fun `register falla si el email ya existe`() = runTest {
        // 1. ARRANGE
        val emailExistente = "yaexiste@gmail.com"

        // Simulamos que la BD encuentra un usuario con ese email
        whenever(usuarioDao.getByEmail(emailExistente)).thenReturn(UsuarioEntity(
            id = 1,
            email = emailExistente,
            pnombre = "Antiguo",
            snombre = "",
            papellido = "Duenio",
            rut = "111",
            ntelefono = "1",
            clave = "1",
            fnacimiento = 1000L,
            duoc_vip = false,
            puntos = 0,
            codigo_ref = "REF123",
            fcreacion = 1000L,
            factualizacion = 1000L,
            estado_id = 1,
            rol_id = 1
        ))

        // 2. ACT
        val resultado = repository.register(
            pnombre = "Nuevo",
            snombre = "",
            papellido = "Intento",
            fnacimiento = 1000L,
            email = emailExistente,
            rut = "33.333.333-3",
            ntelefono = "912345678",
            password = "Pass."
        )

        // 3. ASSERT
        assertTrue("Debe fallar", resultado.isFailure)
        assertEquals("El email ya está registrado", resultado.exceptionOrNull()?.message)
    }
}