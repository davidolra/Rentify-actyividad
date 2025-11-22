package com.example.rentify.data.local

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
    version = 1,
    exportSchema = false
)
abstract class RentifyDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun propiedadDao(): PropiedadDao
    abstract fun catalogDao(): CatalogDao
    abstract fun solicitudDao(): SolicitudDao

    companion object {
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
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    poblarDatosIniciales(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun poblarDatosIniciales(db: RentifyDatabase) {
            val catalogDao = db.catalogDao()
            val usuarioDao = db.usuarioDao()
            val propiedadDao = db.propiedadDao()

            if (catalogDao.getAllEstados().isNotEmpty()) return

            try {
                val estadoActivo = catalogDao.insertEstado(EstadoEntity(nombre = "Activo"))
                catalogDao.insertEstado(EstadoEntity(nombre = "Inactivo"))
                catalogDao.insertEstado(EstadoEntity(nombre = "Pendiente"))
                catalogDao.insertEstado(EstadoEntity(nombre = "Aprobado"))
                catalogDao.insertEstado(EstadoEntity(nombre = "Rechazado"))

                val rolAdmin = catalogDao.insertRol(RolEntity(nombre = "Administrador"))
                val rolPropietario = catalogDao.insertRol(RolEntity(nombre = "Propietario"))
                val rolArrendatario = catalogDao.insertRol(RolEntity(nombre = "Arrendatario"))

                val regionRM = catalogDao.insertRegion(RegionEntity(nombre = "Región Metropolitana"))

                // ====== COMUNAS ======
                val comunaSantiago = catalogDao.insertComuna(ComunaEntity(nombre = "Santiago", region_id = regionRM))
                val comunaNunoa = catalogDao.insertComuna(ComunaEntity(nombre = "Ñuñoa", region_id = regionRM))
                val comunaMaipu = catalogDao.insertComuna(ComunaEntity(nombre = "Maipú", region_id = regionRM))
                val comunaVinaDelMar = catalogDao.insertComuna(ComunaEntity(nombre = "Viña del Mar", region_id = regionRM))

                val comunaProvidencia = catalogDao.insertComuna(ComunaEntity(nombre = "Providencia", region_id = regionRM))

                // ====== TIPOS ======
                val tipoDepartamento = catalogDao.insertTipo(TipoEntity(nombre = "Departamento"))
                val tipoCasa = catalogDao.insertTipo(TipoEntity(nombre = "Casa"))
                val tipoEstudio = catalogDao.insertTipo(TipoEntity(nombre = "Studio"))



                catalogDao.insertCategoria(CategoriaEntity(nombre = "Amoblado"))
                catalogDao.insertCategoria(CategoriaEntity(nombre = "Pet-Friendly"))

                catalogDao.insertTipoDoc(TipoDocEntity(nombre = "Cédula Identidad"))
                catalogDao.insertTipoDoc(TipoDocEntity(nombre = "Liquidación Sueldo"))

                catalogDao.insertTipoResena(TipoResenaEntity(nombre = "Reseña Propiedad"))

                val now = System.currentTimeMillis()

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
                        duoc_vip = true,
                        puntos = 1000,
                        codigo_ref = "ADMIN2024",
                        fcreacion = now,
                        factualizacion = now,
                        estado_id = estadoActivo,
                        rol_id = rolAdmin
                    )
                )

                val propietarioId = usuarioDao.insert(
                    UsuarioEntity(
                        pnombre = "María",
                        snombre = "Elena",
                        papellido = "González",
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

                usuarioDao.insert(
                    UsuarioEntity(
                        pnombre = "Carlos",
                        snombre = "Andrés",
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
                        fcreacion = now,
                        estado_id = estadoActivo,
                        tipo_id = tipoDepartamento,
                        comuna_id = comunaProvidencia,
                        propietario_id = propietarioId
                    )
                )

                // Dpto 2D/2B - Ñuñoa
                propiedadDao.insert(
                    PropiedadEntity(
                        codigo = "DP002",
                        titulo = "Dpto 2D/2B Ñuñoa",
                        precio_mensual = 750000,
                        divisa = "CLP",
                        m2 = 70.0,
                        n_habit = 2,
                        n_banos = 2,
                        pet_friendly = true,
                        direccion = "Av. Irarrázaval 2500, Ñuñoa",
                        fcreacion = now,
                        estado_id = estadoActivo,
                        tipo_id = tipoDepartamento,
                        comuna_id = comunaNunoa,
                        propietario_id = propietarioId
                    )
                )

                // Casa 3D/2B - Maipú
                propiedadDao.insert(
                    PropiedadEntity(
                        codigo = "CASA001",
                        titulo = "Casa 3D/2B Maipú",
                        precio_mensual = 950000,
                        divisa = "CLP",
                        m2 = 120.0,
                        n_habit = 3,
                        n_banos = 2,
                        pet_friendly = true,
                        direccion = "Calle Los Pinos 1234, Maipú",
                        fcreacion = now,
                        estado_id = estadoActivo,
                        tipo_id = tipoCasa,
                        comuna_id = comunaMaipu,
                        propietario_id = propietarioId
                    )
                )

              // Studio - Santiago Centro
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
                        fcreacion = now,
                        estado_id = estadoActivo,
                        tipo_id = tipoEstudio,
                        comuna_id = comunaSantiago,
                        propietario_id = propietarioId
                    )
                )

               // Dpto Temporal - Viña del Mar
                propiedadDao.insert(
                    PropiedadEntity(
                        codigo = "AT001",
                        titulo = "Dpto Temporal Viña del Mar",
                        precio_mensual = 500000,
                        divisa = "CLP",
                        m2 = 50.0,
                        n_habit = 1,
                        n_banos = 1,
                        pet_friendly = false,
                        direccion = "Av. Libertad 200, Viña del Mar",
                        fcreacion = now,
                        estado_id = estadoActivo,
                        tipo_id = tipoDepartamento,
                        comuna_id = comunaVinaDelMar,
                        propietario_id = propietarioId
                    )
                )


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
