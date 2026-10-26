package cl.duocuc.cimuc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import cl.duocuc.cimuc.data.local.entity.ActividadEntity
import cl.duocuc.cimuc.data.local.relation.ActividadConArea
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {

    @Query("SELECT * FROM actividad ORDER BY fecha DESC, titulo ASC")
    fun getAll(): Flow<List<ActividadEntity>>

    @Transaction
    @Query("SELECT * FROM actividad ORDER BY fecha DESC, titulo ASC")
    fun getAllWithArea(): Flow<List<ActividadConArea>>

    @Query("SELECT * FROM actividad WHERE id_actividad = :id")
    fun getById(id: Int): Flow<ActividadEntity?>

    @Query("SELECT * FROM actividad WHERE id_area = :idArea ORDER BY fecha DESC")
    fun getByArea(idArea: Int): Flow<List<ActividadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(actividades: List<ActividadEntity>)
}
