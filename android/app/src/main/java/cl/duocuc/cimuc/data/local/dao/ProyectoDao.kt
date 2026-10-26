package cl.duocuc.cimuc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import cl.duocuc.cimuc.data.local.entity.ProyectoCapacidadCrossRef
import cl.duocuc.cimuc.data.local.entity.ProyectoEntity
import cl.duocuc.cimuc.data.local.entity.ProyectoIntegranteCrossRef
import cl.duocuc.cimuc.data.local.entity.ProyectoPublicacionCrossRef
import cl.duocuc.cimuc.data.local.relation.ProyectoConDetalles
import kotlinx.coroutines.flow.Flow

@Dao
interface ProyectoDao {

    @Query("SELECT * FROM proyecto ORDER BY anio DESC, titulo ASC")
    fun getAll(): Flow<List<ProyectoEntity>>

    @Transaction
    @Query("SELECT * FROM proyecto ORDER BY anio DESC, titulo ASC")
    fun getAllWithDetalles(): Flow<List<ProyectoConDetalles>>

    @Query("SELECT * FROM proyecto WHERE id_proyecto = :id")
    fun getById(id: Int): Flow<ProyectoEntity?>

    @Transaction
    @Query("SELECT * FROM proyecto WHERE id_proyecto = :id")
    fun getWithDetallesById(id: Int): Flow<ProyectoConDetalles?>

    @Transaction
    @Query("""
        SELECT * FROM proyecto
        WHERE (:query IS NULL OR :query = '' OR LOWER(titulo) LIKE '%' || LOWER(:query) || '%' OR LOWER(descripcion) LIKE '%' || LOWER(:query) || '%')
          AND (:idArea IS NULL OR id_area = :idArea)
          AND (:anio IS NULL OR anio = :anio)
          AND (:estado IS NULL OR :estado = '' OR LOWER(estado) = LOWER(:estado))
        ORDER BY anio DESC, titulo ASC
    """)
    fun searchAndFilterWithDetalles(
        query: String?,
        idArea: Int?,
        anio: Int?,
        estado: String?
    ): Flow<List<ProyectoConDetalles>>

    @Query("SELECT DISTINCT anio FROM proyecto ORDER BY anio DESC")
    fun getAniosDisponibles(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(proyectos: List<ProyectoEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefIntegrantes(crossRefs: List<ProyectoIntegranteCrossRef>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefCapacidades(crossRefs: List<ProyectoCapacidadCrossRef>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefPublicaciones(crossRefs: List<ProyectoPublicacionCrossRef>)
}
