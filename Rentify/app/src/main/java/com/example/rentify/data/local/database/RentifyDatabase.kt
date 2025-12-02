package com.example.rentify.data.local

import android.content.Context
import android.util.Log
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
 * Base de datos con poblado automatico GARANTIZADO
 */
@Database(
    entities = [
        RolEntity::class,
        EstadoEntity::class,
        RegionEntity::class,
        ComunaEntity::class,
        TipoEntity::class,
        CategoriaEntity::class,
        TipoDocEntity::class,
        TipoResenaEntity::class,
        UsuarioEntity::class,
        PropiedadEntity::class,
        DocumentoEntity::class,
        FotoEntity::class,
        MasAtributosEntity::class,
        RegistroEntity::class,
        ResenaEntity::class,
        SolicitudEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class RentifyDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun propiedadDao(): PropiedadDao
    abstract fun catalogDao(): CatalogDao
    abstract fun solicitudDao(): SolicitudDao
    abstract fun documentoDao(): DocumentoDao

    companion object {
        private const val TAG = "RentifyDatabase"

        @Volatile
        private var INSTANCE: RentifyDatabase? = null

        fun getInstance(context: Context): RentifyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RentifyDatabase::class.java,
                    "rentify_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()

                INSTANCE = instance

                // FORZAR POBLADO INMEDIATO EN PRIMER ACCESO
                CoroutineScope(Dispatchers.IO).launch {
                    verificarYPoblarDatos(instance)
                }

                instance
            }
        }

        /**
         * Callback que se ejecuta cuando la BD se crea
         */
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d(TAG, "Base de datos creada - Iniciando poblado automatico")

                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d(TAG, "Poblando datos iniciales...")
                        poblarDatosIniciales(database)
                    }
                }
            }
        }

        /**
         * Verificar si la BD esta vacia y poblar si es necesario
         */
        private suspend fun verificarYPoblarDatos(db: RentifyDatabase) {
            try {
                val catalogDao = db.catalogDao()
                val estadosExistentes = catalogDao.getAllEstados()

                if (estadosExistentes.isEmpty()) {
                    Log.d(TAG, "Base de datos vacia detectada - Poblando datos...")
                    poblarDatosIniciales(db)
                } else {
                    Log.d(TAG, "Base de datos ya contiene datos (${estadosExistentes.size} estados)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al verificar datos: ${e.message}", e)
            }
        }

        /**
         * Poblado de datos con mejor manejo de errores
         */
        private suspend fun poblarDatosIniciales(db: RentifyDatabase) {
            val catalogDao = db.catalogDao()
            val usuarioDao = db.usuarioDao()
            val propiedadDao = db.propiedadDao()

            try {
                Log.d(TAG, "Insertando Estados...")
                val estadoActivo = catalogDao.insertEstado(EstadoEntity(nombre = "Activo"))
                catalogDao.insertEstado(EstadoEntity(nombre = "Inactivo"))
                catalogDao.insertEstado(EstadoEntity(nombre = "Pendiente"))
                catalogDao.insertEstado(EstadoEntity(nombre = "Aprobado"))
                catalogDao.insertEstado(EstadoEntity(nombre = "Rechazado"))
                Log.d(TAG, "Estados insertados")

                Log.d(TAG, "Insertando Roles...")
                val rolAdmin = catalogDao.insertRol(RolEntity(nombre = "Administrador"))
                val rolPropietario = catalogDao.insertRol(RolEntity(nombre = "Propietario"))
                val rolArrendatario = catalogDao.insertRol(RolEntity(nombre = "Arrendatario"))
                Log.d(TAG, "Roles insertados")

                Log.d(TAG, "Insertando Regiones...")
                val regionRM = catalogDao.insertRegion(RegionEntity(nombre = "Region Metropolitana"))
                val regionValpo = catalogDao.insertRegion(RegionEntity(nombre = "Region de Valparaiso"))
                Log.d(TAG, "Regiones insertadas")

                Log.d(TAG, "Insertando Comunas...")
                val comunaSantiago = catalogDao.insertComuna(ComunaEntity(nombre = "Santiago", region_id = regionRM))
                val comunaNunoa = catalogDao.insertComuna(ComunaEntity(nombre = "Nunoa", region_id = regionRM))
                val comunaMaipu = catalogDao.insertComuna(ComunaEntity(nombre = "Maipu", region_id = regionRM))
                val comunaVinaDelMar = catalogDao.insertComuna(ComunaEntity(nombre = "Vina del Mar", region_id = regionValpo))
                val comunaProvidencia = catalogDao.insertComuna(ComunaEntity(nombre = "Providencia", region_id = regionRM))
                val comunaLasCondes = catalogDao.insertComuna(ComunaEntity(nombre = "Las Condes", region_id = regionRM))
                Log.d(TAG, "Comunas insertadas")

                Log.d(TAG, "Insertando Tipos de Propiedad...")
                val tipoDepartamento = catalogDao.insertTipo(TipoEntity(nombre = "Departamento"))
                val tipoCasa = catalogDao.insertTipo(TipoEntity(nombre = "Casa"))
                val tipoEstudio = catalogDao.insertTipo(TipoEntity(nombre = "Studio"))
                Log.d(TAG, "Tipos insertados")

                Log.d(TAG, "Insertando Categorias...")
                catalogDao.insertCategoria(CategoriaEntity(nombre = "Amoblado"))
                catalogDao.insertCategoria(CategoriaEntity(nombre = "Pet-Friendly"))
                catalogDao.insertCategoria(CategoriaEntity(nombre = "Con Estacionamiento"))
                catalogDao.insertCategoria(CategoriaEntity(nombre = "Con Terraza"))
                Log.d(TAG, "Categorias insertadas")

                Log.d(TAG, "Insertando Tipos de Documentos...")
                catalogDao.insertTipoDoc(TipoDocEntity(nombre = "Cedula Identidad"))
                catalogDao.insertTipoDoc(TipoDocEntity(nombre = "Liquidacion Sueldo"))
                catalogDao.insertTipoDoc(TipoDocEntity(nombre = "Certificado Antecedentes"))
                catalogDao.insertTipoDoc(TipoDocEntity(nombre = "Certificado AFP"))
                catalogDao.insertTipoDoc(TipoDocEntity(nombre = "Contrato Trabajo"))
                Log.d(TAG, "Tipos de documentos insertados")

                Log.d(TAG, "Insertando Tipos de Resena...")
                catalogDao.insertTipoResena(TipoResenaEntity(nombre = "Resena Propiedad"))
                catalogDao.insertTipoResena(TipoResenaEntity(nombre = "Resena Usuario"))
                Log.d(TAG, "Tipos de resena insertados")

                val now = System.currentTimeMillis()

                // USUARIOS DE PRUEBA
                Log.d(TAG, "Insertando Usuarios de prueba...")

                // Admin
                val adminId = usuarioDao.insert(
                    UsuarioEntity(
                        pnombre = "Admin",
                        snombre = "Sistema",
                        papellido = "Rentify",
                        fnacimiento = now - (30L * 365 * 24 * 60 * 60 * 1000),
                        email = "admin@rentify.cl",
                        rut = "11111111-1",
                        ntelefono = "+56911111111",
                        clave = "Admin123!",
                        duoc_vip = false,
                        puntos = 1000,
                        codigo_ref = "ADMIN2024",
                        fcreacion = now,
                        factualizacion = now,
                        estado_id = estadoActivo,
                        rol_id = rolAdmin
                    )
                )
                Log.d(TAG, "Admin creado: admin@rentify.cl / Admin123!")

                // Propietario
                val propietarioId = usuarioDao.insert(
                    UsuarioEntity(
                        pnombre = "Maria",
                        snombre = "Elena",
                        papellido = "Gonzalez",
                        fnacimiento = now - (35L * 365 * 24 * 60 * 60 * 1000),
                        email = "maria@gmail.com",
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
                Log.d(TAG, "Propietario creado: maria@gmail.com / Maria123!")

                // Arrendatario
                val arrendatarioId = usuarioDao.insert(
                    UsuarioEntity(
                        pnombre = "Carlos",
                        snombre = "Andres",
                        papellido = "Soto",
                        fnacimiento = now - (22L * 365 * 24 * 60 * 60 * 1000),
                        email = "carlos@duocuc.cl",
                        rut = "33333333-3",
                        ntelefono = "+56933333333",
                        clave = "Carlos123!",
                        duoc_vip = true,
                        puntos = 100,
                        codigo_ref = "CARLOS2024",
                        fcreacion = now,
                        factualizacion = now,
                        estado_id = estadoActivo,
                        rol_id = rolArrendatario
                    )
                )
                Log.d(TAG, "Arrendatario creado: carlos@duocuc.cl / Carlos123!")

                // PROPIEDADES DE PRUEBA
                Log.d(TAG, "Insertando Propiedades de prueba...")

                propiedadDao.insert(
                    PropiedadEntity(
                        codigo = "DP001",
                        titulo = "Dpto 2D/1B Providencia",
                        precio_mensual = 650000,
                        divisa = "CLP",
                        m2 = 60.0,
                        n_habit = 2,
                        n_banos = 1,
                        pet_friendly = false,
                        direccion = "Av. Providencia 1234",
                        descripcion = "Departamento moderno en el corazon de Providencia",
                        fcreacion = now,
                        estado_id = estadoActivo,
                        tipo_id = tipoDepartamento,
                        comuna_id = comunaProvidencia,
                        propietario_id = propietarioId
                    )
                )

                propiedadDao.insert(
                    PropiedadEntity(
                        codigo = "DP002",
                        titulo = "Dpto 2D/2B Nunoa",
                        precio_mensual = 750000,
                        divisa = "CLP",
                        m2 = 70.0,
                        n_habit = 2,
                        n_banos = 2,
                        pet_friendly = true,
                        direccion = "Av. Irarrazaval 2500, Nunoa",
                        descripcion = "Hermoso departamento pet-friendly",
                        fcreacion = now,
                        estado_id = estadoActivo,
                        tipo_id = tipoDepartamento,
                        comuna_id = comunaNunoa,
                        propietario_id = propietarioId
                    )
                )

                propiedadDao.insert(
                    PropiedadEntity(
                        codigo = "CASA001",
                        titulo = "Casa 3D/2B Maipu",
                        precio_mensual = 950000,
                        divisa = "CLP",
                        m2 = 120.0,
                        n_habit = 3,
                        n_banos = 2,
                        pet_friendly = true,
                        direccion = "Calle Los Pinos 1234, Maipu",
                        descripcion = "Casa amplia con patio y quincho",
                        fcreacion = now,
                        estado_id = estadoActivo,
                        tipo_id = tipoCasa,
                        comuna_id = comunaMaipu,
                        propietario_id = propietarioId
                    )
                )

                propiedadDao.insert(
                    PropiedadEntity(
                        codigo = "ST001",
                        titulo = "Studio Santiago Centro",
                        precio_mensual = 450000,
                        divisa = "CLP",
                        m2 = 35.0,
                        n_habit = 1,
                        n_banos = 1,
                        pet_friendly = false,
                        direccion = "Calle Alameda 1000, Santiago Centro",
                        descripcion = "Studio moderno ideal para estudiantes",
                        fcreacion = now,
                        estado_id = estadoActivo,
                        tipo_id = tipoEstudio,
                        comuna_id = comunaSantiago,
                        propietario_id = propietarioId
                    )
                )

                propiedadDao.insert(
                    PropiedadEntity(
                        codigo = "AT001",
                        titulo = "Dpto Temporal Vina del Mar",
                        precio_mensual = 500000,
                        divisa = "CLP",
                        m2 = 50.0,
                        n_habit = 1,
                        n_banos = 1,
                        pet_friendly = false,
                        direccion = "Av. Libertad 200, Vina del Mar",
                        descripcion = "Departamento cerca de la playa",
                        fcreacion = now,
                        estado_id = estadoActivo,
                        tipo_id = tipoDepartamento,
                        comuna_id = comunaVinaDelMar,
                        propietario_id = propietarioId
                    )
                )

                Log.d(TAG, "Propiedades insertadas")
                Log.d(TAG, "POBLADO COMPLETO - Base de datos lista para usar")

            } catch (e: Exception) {
                Log.e(TAG, "Error al poblar datos: ${e.message}", e)
                e.printStackTrace()
            }
        }

        /**
         * METODO PARA DEBUGGING: Verificar estado de la BD
         */
        suspend fun debugDatabaseState(context: Context) {
            val db = getInstance(context)
            try {
                val estados = db.catalogDao().getAllEstados()
                val roles = db.catalogDao().getAllRoles()
                val usuarios = db.usuarioDao().getAll()
                val propiedades = db.propiedadDao().getAll()

                Log.d(TAG, "=== ESTADO DE LA BASE DE DATOS ===")
                Log.d(TAG, "Estados: ${estados.size}")
                Log.d(TAG, "Roles: ${roles.size}")
                Log.d(TAG, "Usuarios: ${usuarios.size}")
                Log.d(TAG, "Propiedades: ${propiedades.size}")
                Log.d(TAG, "==================================")
            } catch (e: Exception) {
                Log.e(TAG, "Error al verificar estado de BD: ${e.message}", e)
            }
        }
    }
}