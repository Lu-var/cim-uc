package cl.duocuc.cimuc.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "capacidad",
    foreignKeys = [
        ForeignKey(
            entity = AreaTematicaEntity::class,
            parentColumns = ["id_area"],
            childColumns = ["id_area"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_area"])
    ]
)
data class CapacidadEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_capacidad")
    val idCapacidad: Int,
    @ColumnInfo(name = "nombre")
    val nombre: String,
    @ColumnInfo(name = "descripcion")
    val descripcion: String,
    @ColumnInfo(name = "id_area")
    val idArea: Int
)
