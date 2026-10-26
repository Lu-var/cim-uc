package cl.duocuc.cimuc.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "area_tematica")
data class AreaTematicaEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_area")
    val idArea: Int,
    @ColumnInfo(name = "nombre")
    val nombre: String,
    @ColumnInfo(name = "descripcion")
    val descripcion: String
)
