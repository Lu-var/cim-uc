package cl.duocuc.cimuc.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duocuc.cimuc.data.local.dao.ActividadDao
import cl.duocuc.cimuc.data.local.dao.AreaTematicaDao
import cl.duocuc.cimuc.data.local.dao.CapacidadDao
import cl.duocuc.cimuc.data.local.dao.IntegranteDao
import cl.duocuc.cimuc.data.local.dao.OrganizacionDao
import cl.duocuc.cimuc.data.local.dao.ProyectoDao
import cl.duocuc.cimuc.data.local.dao.PublicacionDao
import cl.duocuc.cimuc.data.local.dao.SolicitudContactoDao
import cl.duocuc.cimuc.data.local.entity.ActividadEntity
import cl.duocuc.cimuc.data.local.entity.AreaTematicaEntity
import cl.duocuc.cimuc.data.local.entity.CapacidadEntity
import cl.duocuc.cimuc.data.local.entity.IntegranteEntity
import cl.duocuc.cimuc.data.local.entity.OrganizacionEntity
import cl.duocuc.cimuc.data.local.entity.ProyectoCapacidadCrossRef
import cl.duocuc.cimuc.data.local.entity.ProyectoEntity
import cl.duocuc.cimuc.data.local.entity.ProyectoIntegranteCrossRef
import cl.duocuc.cimuc.data.local.entity.ProyectoPublicacionCrossRef
import cl.duocuc.cimuc.data.local.entity.PublicacionEntity
import cl.duocuc.cimuc.data.local.entity.SolicitudContactoEntity
import cl.duocuc.cimuc.data.local.seed.CimSeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AreaTematicaEntity::class,
        OrganizacionEntity::class,
        ProyectoEntity::class,
        CapacidadEntity::class,
        PublicacionEntity::class,
        IntegranteEntity::class,
        ActividadEntity::class,
        SolicitudContactoEntity::class,
        ProyectoIntegranteCrossRef::class,
        ProyectoCapacidadCrossRef::class,
        ProyectoPublicacionCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun proyectoDao(): ProyectoDao
    abstract fun areaTematicaDao(): AreaTematicaDao
    abstract fun organizacionDao(): OrganizacionDao
    abstract fun capacidadDao(): CapacidadDao
    abstract fun publicacionDao(): PublicacionDao
    abstract fun integranteDao(): IntegranteDao
    abstract fun actividadDao(): ActividadDao
    abstract fun solicitudContactoDao(): SolicitudContactoDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE solicitud_contacto ADD COLUMN organizacion TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE solicitud_contacto ADD COLUMN motivo TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cim_uc.db"
                ).addMigrations(MIGRATION_1_2).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = get(context)
                            // Precarga idempotente en orden de dependencias FK
                            database.areaTematicaDao().insertAll(CimSeedData.areasTematicas)
                            database.organizacionDao().insertAll(CimSeedData.organizaciones)
                            database.proyectoDao().insertAll(CimSeedData.proyectos)
                            database.capacidadDao().insertAll(CimSeedData.capacidades)
                            database.publicacionDao().insertAll(CimSeedData.publicaciones)
                            database.integranteDao().insertAll(CimSeedData.integrantes)
                            database.actividadDao().insertAll(CimSeedData.actividades)

                            database.proyectoDao().insertCrossRefIntegrantes(CimSeedData.proyectoIntegrantes)
                            database.proyectoDao().insertCrossRefCapacidades(CimSeedData.proyectoCapacidades)
                            database.proyectoDao().insertCrossRefPublicaciones(CimSeedData.proyectoPublicaciones)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
