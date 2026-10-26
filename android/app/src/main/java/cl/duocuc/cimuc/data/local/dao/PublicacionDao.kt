package cl.duocuc.cimuc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duocuc.cimuc.data.local.entity.PublicacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PublicacionDao {

    @Query("SELECT * FROM publicacion ORDER BY anio DESC, titulo ASC")
    fun getAll(): Flow<List<PublicacionEntity>>

    @Query("SELECT * FROM publicacion WHERE id_area = :idArea ORDER BY anio DESC")
    fun getByArea(idArea: Int): Flow<List<PublicacionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(publicaciones: List<PublicacionEntity>)
}
