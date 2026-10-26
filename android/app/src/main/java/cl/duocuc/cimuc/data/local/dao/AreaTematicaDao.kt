package cl.duocuc.cimuc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duocuc.cimuc.data.local.entity.AreaTematicaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaTematicaDao {

    @Query("SELECT * FROM area_tematica ORDER BY nombre ASC")
    fun getAll(): Flow<List<AreaTematicaEntity>>

    @Query("SELECT * FROM area_tematica WHERE id_area = :id")
    fun getById(id: Int): Flow<AreaTematicaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(areas: List<AreaTematicaEntity>)
}
