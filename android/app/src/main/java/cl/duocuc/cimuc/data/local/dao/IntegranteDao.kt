package cl.duocuc.cimuc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duocuc.cimuc.data.local.entity.IntegranteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IntegranteDao {

    @Query("SELECT * FROM integrante ORDER BY nombre ASC")
    fun getAll(): Flow<List<IntegranteEntity>>

    @Query("SELECT * FROM integrante WHERE id_integrante = :id")
    fun getById(id: Int): Flow<IntegranteEntity?>

    @Query("""
        SELECT i.* FROM integrante i
        INNER JOIN proyecto_integrante pi ON i.id_integrante = pi.id_integrante
        WHERE pi.id_proyecto = :proyectoId
        ORDER BY i.nombre ASC
    """)
    fun getByProyecto(proyectoId: Int): Flow<List<IntegranteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(integrantes: List<IntegranteEntity>)
}
