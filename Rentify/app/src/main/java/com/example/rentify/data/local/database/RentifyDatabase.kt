package com.example.rentify.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.rentify.data.local.dao.*
import com.example.rentify.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos Room para Rentify
 * Incluye todas las entidades del modelo de datos
 */
@Database(
    entities = [
        // Catálogos
        RolEntity::class,
        EstadoEntity::class,
        RegionEntity::class,
        ComunaEntity::class,
        TipoEntity::class,
        CategoriaEntity::class,
        TipoDocEntity::class,
        TipoResenaEntity::class,
        // Entidades principales
        UsuarioEntity::class,
        PropiedadEntity::class,
        DocumentoEntity::class,
        FotoEntity::class,
        MasAtributosEntity::class,
        RegistroEntity::class,
        ResenaEntity::class,
        SolicitudEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class RentifyDatabase : RoomDatabase() {

    // DAOs
    abstract fun usuarioDao(): UsuarioDao
    abstract fun propiedadDao(): PropiedadDao
    abstract fun catalogDao(): CatalogDao
    abstract fun solicitudDao(): SolicitudDao

    companion object {
        @Volatile
        private var INSTANCE: RentifyDatabase? = null
        private const val DB_NAME = "rentify.db"

        fun getInstance(context: Context): RentifyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RentifyDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Poblar datos iniciales en IO
                            CoroutineScope(Dispatchers.IO).launch {
                                poblarDatosIniciales(getInstance(context))
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Pobla la base de datos con datos iniciales (seed data)
         */
        private suspend fun poblarDatosIniciales(db: RentifyDatabase) {
            val catalogDao = db.catalogDao()
            val usuarioDao = db.usuarioDao()
            val propiedadDao = db.propiedadDao()

            // Verificar si ya hay datos
            if (catalogDao.getAllEstados().isNotEmpty()) return

            // ========== ESTADOS ==========
            val estadoActivo = catalogDao.insertEstado(EstadoEntity(nombre = "Activo"))
            val estadoInactivo = catalogDao.insertEstado(EstadoEntity(nombre = "Inactivo"))
            val estadoPendiente = catalogDao.insertEstado(EstadoEntity(nombre = "Pendiente"))
            val estadoAprobado = catalogDao.insertEstado(EstadoEntity(nombre = "Aprobado"))
            val estadoRechazado = catalogDao.insertEstado(EstadoEntity(nombre = "Rechazado"))

            // ========== ROLES ==========
            val rolAdmin = catalogDao.insertRol(RolEntity(nombre = "Administrador"))
            val rolPropietario = catalogDao.insertRol(RolEntity(nombre = "Propietario"))
            val rolInquilino = catalogDao.insertRol(RolEntity(nombre = "Inquilino"))

            // ========== REGIONES ==========
            val regionRM = catalogDao.insertRegion(RegionEntity(nombre = "Región Metropolitana"))
            val regionValpo = catalogDao.insertRegion(RegionEntity(nombre = "Región de Valparaíso"))
            val regionBiobio = catalogDao.insertRegion(RegionEntity(nombre = "Región del Biobío"))

            // ========== COMUNAS ==========
            val comunaSantiago = catalogDao.insertComuna(ComunaEntity(nombre = "Santiago", region_id = regionRM))
            val comunaProvidencia = catalogDao.insertComuna(ComunaEntity(nombre = "Providencia", region_id = regionRM))
            val comunaNunoa = catalogDao.insertComuna(ComunaEntity(nombre = "Ñuñoa", region_id = regionRM))
            val comunaMaipu = catalogDao.insertComuna(ComunaEntity(nombre = "Maipú", region_id = regionRM))
            val comunaVinaDelMar = catalogDao.insertComuna(ComunaEntity(nombre = "Viña del Mar", region_id = regionValpo))

            // ========== TIPOS DE PROPIEDAD ==========
            val tipoDepartamento = catalogDao.insertTipo(TipoEntity(nombre = "Departamento"))
            val tipoCasa = catalogDao.insertTipo(TipoEntity(nombre = "Casa"))
            val tipoEstudio = catalogDao.insertTipo(TipoEntity(nombre = "Estudio/Loft"))
            val tipoHabitacion = catalogDao.insertTipo(TipoEntity(nombre = "Habitación"))

            // ========== CATEGORÍAS ==========
            val catAmoblado = catalogDao.insertCategoria(CategoriaEntity(nombre = "Amoblado"))
            val catPetFriendly = catalogDao.insertCategoria(CategoriaEntity(nombre = "Pet-Friendly"))
            val catTerraza = catalogDao.insertCategoria(CategoriaEntity(nombre = "Con Terraza"))
            val catEstacionamiento = catalogDao.insertCategoria(CategoriaEntity(nombre = "Con Estacionamiento"))
            val catTemporal = catalogDao.insertCategoria(CategoriaEntity(nombre = "Arriendo Temporal"))

            // ========== TIPOS DE DOCUMENTOS ==========
            catalogDao.insertTipoDoc(TipoDocEntity(nombre = "Cédula Identidad"))
            catalogDao.insertTipoDoc(TipoDocEntity(nombre = "Liquidación Sueldo"))
            catalogDao.insertTipoDoc(TipoDocEntity(nombre = "Certificado Antecedentes"))
            catalogDao.insertTipoDoc(TipoDocEntity(nombre = "Contrato Trabajo"))

            // ========== TIPOS DE RESEÑA ==========
            catalogDao.insertTipoResena(TipoResenaEntity(nombre = "Reseña Propiedad"))
            catalogDao.insertTipoResena(TipoResenaEntity(nombre = "Reseña Propietario"))
            catalogDao.insertTipoResena(TipoResenaEntity(nombre = "Reseña Inquilino"))

            // ========== USUARIOS DEMO ==========
            val now = System.currentTimeMillis()

            // Usuario Admin DUOC
            usuarioDao.insert(
                UsuarioEntity(
                    pnombre = "Admin",
                    snombre = "Sistema",
                    papellido = "Rentify",
                    fnacimiento = now - (25L * 365 * 24 * 60 * 60 * 1000), // 25 años
                    email = "admin@duoc.cl",
                    rut = "11111111-1",
                    ntelefono = "+56911111111",
                    clave = "Admin123!",
                    duoc_vip = true,
                    puntos = 1000,
                    codigo_ref = "ADMIN2024",
                    fcreacion = now,
                    factualizacion = now,
                    estado_id = estadoActivo,
                    rol_id = rolAdmin
                )
            )

            // Usuario Propietario
            usuarioDao.insert(
                UsuarioEntity(
                    pnombre = "María",
                    snombre = "Elena",
                    papellido = "González",
                    fnacimiento = now - (35L * 365 * 24 * 60 * 60 * 1000), // 35 años
                    email = "maria.gonzalez@gmail.com",
                    rut = "22222222-2",
                    ntelefono = "+56922222222",
                    clave = "Maria123!",
                    duoc_vip = false,
                    puntos = 150,
                    codigo_ref = "MARIA2024",
                    fcreacion = now,
                    factualizacion = now,
                    estado_id = estadoActivo,
                    rol_id = rolPropietario
                )
            )

            // Usuario Inquilino DUOC VIP
            usuarioDao.insert(
                UsuarioEntity(
                    pnombre = "Carlos",
                    snombre = "Andrés",
                    papellido = "Soto",
                    fnacimiento = now - (22L * 365 * 24 * 60 * 60 * 1000), // 22 años
                    email = "carlos.soto@duocuc.cl",
                    rut = "33333333-3",
                    ntelefono = "+56933333333",
                    clave = "Carlos123!",
                    duoc_vip = true, // 20% descuento
                    puntos = 50,
                    codigo_ref = "CARLOS2024",
                    fcreacion = now,
                    factualizacion = now,
                    estado_id = estadoActivo,
                    rol_id = rolInquilino
                )
            )

            // ========== PROPIEDADES DEMO ==========

            // Dpto 1D/1B Amoblado - Providencia
            propiedadDao.insert(
                PropiedadEntity(
                    codigo = "DP001",
                    titulo = "Dpto 1D/1B Amoblado – Providencia",
                    precio_mensual = 650000,
                    divisa = "CLP",
                    m2 = 45.5,
                    n_habit = 1,
                    n_banos = 1,
                    pet_friendly = false,
                    direccion = "Av. Providencia 1234, Providencia",
                    fcreacion = now,
                    estado_id = estadoActivo,
                    tipo_id = tipoDepartamento,
                    comuna_id = comunaProvidencia
                )
            )

            // Dpto 2D/2B - Ñuñoa
            propiedadDao.insert(
                PropiedadEntity(
                    codigo = "DP002",
                    titulo = "Dpto 2D/2B – Ñuñoa",
                    precio_mensual = 720000,
                    divisa = "CLP",
                    m2 = 65.0,
                    n_habit = 2,
                    n_banos = 2,
                    pet_friendly = true,
                    direccion = "Av. Irarrázaval 2500, Ñuñoa",
                    fcreacion = now,
                    estado_id = estadoActivo,
                    tipo_id = tipoDepartamento,
                    comuna_id = comunaNunoa
                )
            )

            // Casa 3D/2B - Maipú
            propiedadDao.insert(
                PropiedadEntity(
                    codigo = "CASA001",
                    titulo = "Casa 3D/2B – Maipú",
                    precio_mensual = 850000,
                    divisa = "CLP",
                    m2 = 120.0,
                    n_habit = 3,
                    n_banos = 2,
                    pet_friendly = true,
                    direccion = "Calle Los Aromos 789, Maipú",
                    fcreacion = now,
                    estado_id = estadoActivo,
                    tipo_id = tipoCasa,
                    comuna_id = comunaMaipu
                )
            )

            // Studio - Santiago Centro
            propiedadDao.insert(
                PropiedadEntity(
                    codigo = "ST001",
                    titulo = "Studio 28 m² – Santiago Centro",
                    precio_mensual = 420000,
                    divisa = "CLP",
                    m2 = 28.0,
                    n_habit = 1,
                    n_banos = 1,
                    pet_friendly = false,
                    direccion = "Morandé 456, Santiago Centro",
                    fcreacion = now,
                    estado_id = estadoActivo,
                    tipo_id = tipoEstudio,
                    comuna_id = comunaSantiago
                )
            )

            // Dpto Temporal - Viña del Mar
            propiedadDao.insert(
                PropiedadEntity(
                    codigo = "AT001",
                    titulo = "Depto 1D – Viña del Mar (mensual flexible)",
                    precio_mensual = 820000,
                    divisa = "CLP",
                    m2 = 50.0,
                    n_habit = 1,
                    n_banos = 1,
                    pet_friendly = false,
                    direccion = "Av. San Martín 321, Viña del Mar",
                    fcreacion = now,
                    estado_id = estadoActivo,
                    tipo_id = tipoDepartamento,
                    comuna_id = comunaVinaDelMar
                )
            )
        }
    }
}