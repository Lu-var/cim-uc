package cl.duocuc.cimuc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duocuc.cimuc.data.local.entity.CapacidadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CapacidadDao {

    @Query("SELECT * FROM capacidad ORDER BY nombre ASC")
    fun getAll(): Flow<List<CapacidadEntity>>

    @Query("SELECT * FROM capacidad WHERE id_area = :idArea ORDER BY nombre ASC")
    fun getByArea(idArea: Int): Flow<List<CapacidadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(capacidades: List<CapacidadEntity>)
}
