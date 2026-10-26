package cl.duocuc.cimuc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import cl.duocuc.cimuc.data.local.entity.SolicitudContactoEntity
import cl.duocuc.cimuc.data.local.relation.SolicitudConProyecto
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudContactoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(solicitud: SolicitudContactoEntity): Long

    @Query("SELECT * FROM solicitud_contacto WHERE id_solicitud = :id")
    fun getById(id: Int): Flow<SolicitudContactoEntity?>

    @Transaction
    @Query("SELECT * FROM solicitud_contacto WHERE id_solicitud = :id")
    fun getWithProyectoById(id: Int): Flow<SolicitudConProyecto?>

    @Query("SELECT * FROM solicitud_contacto ORDER BY id_solicitud DESC")
    fun getAll(): Flow<List<SolicitudContactoEntity>>
}
