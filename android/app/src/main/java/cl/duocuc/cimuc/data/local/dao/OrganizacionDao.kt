package cl.duocuc.cimuc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duocuc.cimuc.data.local.entity.OrganizacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizacionDao {

    @Query("SELECT * FROM organizacion ORDER BY nombre ASC")
    fun getAll(): Flow<List<OrganizacionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(organizaciones: List<OrganizacionEntity>)
}
